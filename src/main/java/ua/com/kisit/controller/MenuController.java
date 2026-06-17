package ua.com.kisit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ua.com.kisit.entity.Category;
import ua.com.kisit.entity.Dish;
import ua.com.kisit.entity.Promotion;
import ua.com.kisit.entity.Restaurant;
import ua.com.kisit.service.CategoryService;
import ua.com.kisit.service.DishService;
import ua.com.kisit.service.PromotionService;
import ua.com.kisit.service.RestaurantService;

import java.util.List;

@Controller
@RequestMapping("/r")
public class MenuController {

    private final RestaurantService restaurantService;
    private final CategoryService categoryService;
    private final DishService dishService;
    private final PromotionService promotionService;

    public MenuController(RestaurantService restaurantService,
                          CategoryService categoryService,
                          DishService dishService,
                          PromotionService promotionService) {
        this.restaurantService = restaurantService;
        this.categoryService = categoryService;
        this.dishService = dishService;
        this.promotionService = promotionService;
    }

    @GetMapping("/{slug}/table/{tableNumber}")
    public String getDigitalMenu(@PathVariable String slug,
                                 @PathVariable Integer tableNumber,
                                 Model model) {

        Restaurant restaurant = restaurantService.getBySlug(slug);
        List<Category> categories = categoryService.getCategoriesByRestaurant(restaurant.getId());
        List<Dish> dishes = dishService.getAvailableDishesByRestaurant(restaurant.getId());
        List<Promotion> promotions = promotionService.getAllByRestaurant(restaurant.getId());

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("categories", categories);
        model.addAttribute("dishes", dishes);
        model.addAttribute("tableNumber", tableNumber);

        // Передаємо сервіс акцій для обчислення знижок прямо в шаблоні HTML
        model.addAttribute("promoService", promotionService);
        model.addAttribute("promotions", promotions);

        return "menu";
    }
}