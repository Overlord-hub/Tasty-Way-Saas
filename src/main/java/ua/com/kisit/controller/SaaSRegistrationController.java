package ua.com.kisit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ua.com.kisit.dto.RegistrationForm;
import ua.com.kisit.entity.ActivationKey;
import ua.com.kisit.entity.Restaurant;
import ua.com.kisit.entity.User;
import ua.com.kisit.repository.ActivationKeyRepository;
import ua.com.kisit.service.RestaurantService;
import ua.com.kisit.service.UserService;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class SaaSRegistrationController {

    private final RestaurantService restaurantService;
    private final UserService userService;
    private final ActivationKeyRepository activationKeyRepository;

    public SaaSRegistrationController(RestaurantService restaurantService,
                                      UserService userService,
                                      ActivationKeyRepository activationKeyRepository) {
        this.restaurantService = restaurantService;
        this.userService = userService;
        this.activationKeyRepository = activationKeyRepository;
    }

    /**
     * Показ сторінки реєстрації нового закладу у платформі Tasty Way
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("regForm", new RegistrationForm());
        return "register";
    }

    /**
     * Обробка створення ресторану та акаунта власника
     */
    @PostMapping("/register")
    public String registerNewRestaurantAndAdmin(@ModelAttribute("regForm") RegistrationForm form, Model model) {

        // 1. Шукаємо наданий токен у базі даних
        Optional<ActivationKey> keyOptional = activationKeyRepository.findByToken(form.getActivationCode().trim());

        // БЕЗПЕКА: Перевірка на існування ключа
        if (keyOptional.isEmpty()) {
            model.addAttribute("error", "Наданий ключ активації не існує в системі");
            model.addAttribute("regForm", form);
            return "register";
        }

        ActivationKey activationKey = keyOptional.get();

        // БЕЗПЕКА: Перевірка, чи не був цей ключ уже використаний кимось іншим
        if (activationKey.isUsed()) {
            model.addAttribute("error", "Цей ключ активації вже був використаний для іншого закладу");
            model.addAttribute("regForm", form);
            return "register";
        }

        try {
            // 2. Якщо ключ валідний — створюємо ресторан (Тенант)
            Restaurant newRestaurant = Restaurant.builder()
                    .name(form.getRestaurantName())
                    .slug(form.getRestaurantSlug().trim().toLowerCase())
                    .isActive(true)
                    .themeColor("#198754")
                    .build();

            Restaurant savedRestaurant = restaurantService.save(newRestaurant);

            // 3. Створюємо користувача-власника (ROLE_ADMIN)
            User admin = User.builder()
                    .username(form.getUsername())
                    .password(form.getPassword())
                    .firstName(form.getFirstName())
                    .lastName(form.getLastName())
                    .role(User.Role.ROLE_ADMIN)
                    .restaurant(savedRestaurant)
                    .build();

            userService.registerNewUser(admin);

            // 4. КРИТИЧНИЙ ШАГ: Погашення ключа. Робимо його недійсним для наступних спроб
            activationKey.setUsed(true);
            activationKey.setActivatedAt(LocalDateTime.now());
            activationKeyRepository.save(activationKey); // Оновлюємо статус у БД

            return "redirect:/login?registered=true";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("regForm", form);
            return "register";
        }
    }
}