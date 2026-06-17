package ua.com.kisit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ua.com.kisit.repository.ActivationKeyRepository;
import ua.com.kisit.service.UserService;

@Controller
@RequestMapping("/superadmin")
public class SuperAdminKeyController {

    private final UserService userService;
    private final ActivationKeyRepository activationKeyRepository;

    public SuperAdminKeyController(UserService userService, ActivationKeyRepository activationKeyRepository) {
        this.userService = userService;
        this.activationKeyRepository = activationKeyRepository;
    }

    /**
     * Показ панелі керування ліцензійними ключами
     */
    @GetMapping("/keys")
    public String showKeysPanel(Model model) {
        // Завантажуємо всі ключі з бази даних, щоб бачити, які вільні, а які використані
        model.addAttribute("keys", activationKeyRepository.findAll());
        return "superadmin_keys"; // Наш новий HTML-шаблон
    }

    /**
     * Обробка запиту на генерацію нового одноразового ключа
     */
    @PostMapping("/keys/generate")
    public String generateKey() {
        // Викликаємо твій метод із UserService, який створює UUID токен у БД
        userService.generateNewLicenseKey();
        // Перенаправляємо назад на панель, щоб оновити список
        return "redirect:/superadmin/keys";
    }
}