package ua.com.kisit.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.com.kisit.entity.Dish;
import ua.com.kisit.entity.Promotion;
import ua.com.kisit.entity.Restaurant;
import ua.com.kisit.entity.User;
import ua.com.kisit.service.CategoryService;
import ua.com.kisit.service.DishService;
import ua.com.kisit.service.PromotionService;
import ua.com.kisit.service.RestaurantService;

import java.io.IOException;

@Controller
@RequestMapping("/manager")
public class SalesManagerController {

    private final DishService dishService;
    private final CategoryService categoryService;
    private final RestaurantService restaurantService;
    private final PromotionService promotionService;

    public SalesManagerController(DishService dishService,
                                  CategoryService categoryService,
                                  RestaurantService restaurantService,
                                  PromotionService promotionService) {
        this.dishService = dishService;
        this.categoryService = categoryService;
        this.restaurantService = restaurantService;
        this.promotionService = promotionService;
    }

    /**
     * 1. Сторінка з формою створення нової страви.
     */
    @GetMapping("/dish/add")
    public String showAddDishForm(@AuthenticationPrincipal User currentUser, Model model) {
        Long restaurantId = currentUser.getRestaurant().getId();

        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("categories", categoryService.getCategoriesByRestaurant(restaurantId));
        model.addAttribute("dish", new Dish());
        model.addAttribute("userRole", currentUser.getRole().name());

        return "manager/add-dish";
    }

    /**
     * 2. Обробка відправки форми створення страви.
     */
    @PostMapping("/dish/save")
    public String saveDish(@ModelAttribute Dish dish,
                           @RequestParam("imageFile") MultipartFile imageFile,
                           @RequestParam Long categoryId,
                           @AuthenticationPrincipal User currentUser) {
        Restaurant restaurant = currentUser.getRestaurant();
        Long restaurantId = restaurant.getId();

        // Перевірка приналежності категорії до ресторану користувача
        ua.com.kisit.entity.Category category = categoryService.getById(categoryId);
        if (category == null || !category.getRestaurant().getId().equals(restaurantId)) {
            return "redirect:/error?message=AccessDeniedToTenant";
        }

        try {
            String fileName = dishService.saveDishImage(imageFile);
            if (fileName != null) {
                dish.setImageDish(fileName);
            }

            dish.setRestaurant(restaurant);
            dish.setCategory(category);
            dish.setAvailable(true);

            dishService.save(dish);

        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/manager/dish/add?restaurantId=" + restaurantId + "&error=file";
        }

        return "redirect:/manager/menu?restaurantId=" + restaurantId;
    }

    /**
     * 3. Сторінка перегляду всіх маркетингових акцій.
     */
    @GetMapping("/promotions")
    public String getPromotionsList(@AuthenticationPrincipal User currentUser, Model model) {
        Long restaurantId = currentUser.getRestaurant().getId();

        model.addAttribute("promotions", promotionService.getAllByRestaurant(restaurantId));
        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("userRole", currentUser.getRole().name());

        return "manager/promotions";
    }

    /**
     * 4. Сторінка з формою створення нової акції.
     */
    @GetMapping("/promotions/add")
    public String showAddPromotionForm(@AuthenticationPrincipal User currentUser, Model model) {
        Long restaurantId = currentUser.getRestaurant().getId();

        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("dishes", dishService.getAvailableDishesByRestaurant(restaurantId));
        model.addAttribute("categories", categoryService.getCategoriesByRestaurant(restaurantId));
        model.addAttribute("promotion", new Promotion());
        model.addAttribute("userRole", currentUser.getRole().name());

        return "manager/add-promotion";
    }

    /**
     * 5. Обробка збереження нової акції.
     */
    @PostMapping("/promotions/save")
    public String savePromotion(@ModelAttribute Promotion promotion,
                                @RequestParam(required = false) Long targetDishId,
                                @RequestParam(required = false) Long targetCategoryId,
                                @AuthenticationPrincipal User currentUser) {
        Restaurant restaurant = currentUser.getRestaurant();
        Long restaurantId = restaurant.getId();

        // Валідація цільової страви на приналежність тенанту
        if (targetDishId != null) {
            Dish dish = dishService.getById(targetDishId);
            if (dish == null || !dish.getRestaurant().getId().equals(restaurantId)) {
                return "redirect:/error?message=AccessDeniedToTenant";
            }
            promotion.setTargetDish(dish);
        }

        // БЕЗПЕКА: Валідація цільової категорії на приналежність тенанту
        if (targetCategoryId != null) {
            ua.com.kisit.entity.Category category = categoryService.getById(targetCategoryId);
            if (category == null || !category.getRestaurant().getId().equals(restaurantId)) {
                return "redirect:/error?message=AccessDeniedToTenant";
            }
            promotion.setTargetCategory(category);
        }

        promotion.setRestaurant(restaurant);
        promotionService.save(promotion);
        return "redirect:/manager/promotions?restaurantId=" + restaurantId;
    }

    /**
     * 6. Видалення акції.
     */
    @PostMapping("/promotions/{id}/delete")
    public String deletePromotion(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        Long restaurantId = currentUser.getRestaurant().getId();
        Promotion promotion = promotionService.getById(id);

        // Перевірка приналежності акції до поточного тенанта
        if (promotion == null || !promotion.getRestaurant().getId().equals(restaurantId)) {
            return "redirect:/error?message=AccessDeniedToTenant";
        }

        promotionService.deleteById(id);
        return "redirect:/manager/promotions?restaurantId=" + restaurantId;
    }

    /**
     * 7. Сторінка редагування існуючої страви.
     */
    @GetMapping("/dish/{id}/edit")
    public String showEditDishForm(@PathVariable Long id, @AuthenticationPrincipal User currentUser, Model model) {
        Long restaurantId = currentUser.getRestaurant().getId();
        Dish dish = dishService.getById(id);

        // Захист екрана редагування від чужих тенантів
        if (dish == null || !dish.getRestaurant().getId().equals(restaurantId)) {
            return "redirect:/error?message=AccessDeniedToTenant";
        }

        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("categories", categoryService.getCategoriesByRestaurant(restaurantId));
        model.addAttribute("dish", dish);
        model.addAttribute("userRole", currentUser.getRole().name());

        return "manager/edit-dish";
    }

    /**
     * 8. Обробка оновлення страви (POST-запит).
     */
    @PostMapping("/dish/{id}/update")
    public String updateDish(@PathVariable Long id,
                             @ModelAttribute Dish dish,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             @RequestParam Long categoryId,
                             @AuthenticationPrincipal User currentUser) {
        Restaurant restaurant = currentUser.getRestaurant();
        Long restaurantId = restaurant.getId();
        Dish existingDish = dishService.getById(id);

        // Перевірка приналежності редагованої страви тенанту
        if (existingDish == null || !existingDish.getRestaurant().getId().equals(restaurantId)) {
            return "redirect:/error?message=AccessDeniedToTenant";
        }

        // Перевірка приналежності обраної нової категорії тенанту
        ua.com.kisit.entity.Category category = categoryService.getById(categoryId);
        if (category == null || !category.getRestaurant().getId().equals(restaurantId)) {
            return "redirect:/error?message=AccessDeniedToTenant";
        }

        try {
            if (!imageFile.isEmpty()) {
                String fileName = dishService.saveDishImage(imageFile);
                dish.setImageDish(fileName);
            } else {
                dish.setImageDish(existingDish.getImageDish());
            }

            dish.setId(id);
            dish.setRestaurant(restaurant);
            dish.setCategory(category);
            dish.setAvailable(existingDish.isAvailable()); // Зберігаємо поточний статус стоп-листа

            dishService.save(dish);

        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/manager/dish/" + id + "/edit?restaurantId=" + restaurantId + "&error=file";
        }

        return "redirect:/manager/menu?restaurantId=" + restaurantId;
    }

    /**
     * 9. Видалення страви з бази даних.
     */
    @PostMapping("/dish/{id}/delete")
    public String deleteDishPermanently(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        Long restaurantId = currentUser.getRestaurant().getId();
        Dish dish = dishService.getById(id);

        // Захист від несанкціонованого видалення комерційних даних
        if (dish == null || !dish.getRestaurant().getId().equals(restaurantId)) {
            return "redirect:/error?message=AccessDeniedToTenant";
        }

        dishService.deleteById(id);
        return "redirect:/manager/menu?restaurantId=" + restaurantId;
    }

    /**
     * 10. Обробка швидкого створення нової категорії меню.
     */
    @PostMapping("/category/save")
    public String saveNewCategory(@RequestParam String categoryName,
                                  @AuthenticationPrincipal User currentUser) {
        Restaurant restaurant = currentUser.getRestaurant();

        if (categoryName != null && !categoryName.trim().isEmpty()) {
            ua.com.kisit.entity.Category category = new ua.com.kisit.entity.Category();
            category.setName(categoryName.trim());
            category.setRestaurant(restaurant);
            categoryService.save(category);
        }

        return "redirect:/manager/menu?restaurantId=" + restaurant.getId();
    }

    /**
     * 11. Обробка редагування (оновлення) назви категорії.
     */
    @PostMapping("/category/{id}/update")
    public String updateCategory(@PathVariable Long id,
                                 @RequestParam String categoryName,
                                 @AuthenticationPrincipal User currentUser) {
        Long restaurantId = currentUser.getRestaurant().getId();
        ua.com.kisit.entity.Category category = categoryService.getById(id);

        // Захист від модифікації структурних категорій іншого тенанта
        if (category == null || !category.getRestaurant().getId().equals(restaurantId)) {
            return "redirect:/error?message=AccessDeniedToTenant";
        }

        if (categoryName != null && !categoryName.trim().isEmpty()) {
            category.setName(categoryName.trim());
            categoryService.save(category);
        }
        return "redirect:/manager/menu?restaurantId=" + restaurantId;
    }

    /**
     * 12. Видалення категорії з меню.
     */
    @PostMapping("/category/{id}/delete")
    public String deleteCategory(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        Long restaurantId = currentUser.getRestaurant().getId();
        ua.com.kisit.entity.Category category = categoryService.getById(id);

        // Захист від видалення структурних даних іншого тенанта
        if (category == null || !category.getRestaurant().getId().equals(restaurantId)) {
            return "redirect:/error?message=AccessDeniedToTenant";
        }

        try {
            categoryService.deleteById(id);
        } catch (Exception e) {
            return "redirect:/manager/menu?restaurantId=" + restaurantId + "&error=category_has_dishes";
        }
        return "redirect:/manager/menu?restaurantId=" + restaurantId;
    }
}