package ua.com.kisit.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ua.com.kisit.entity.Order;
import ua.com.kisit.entity.User;
import ua.com.kisit.service.CategoryService;
import ua.com.kisit.service.DishService;
import ua.com.kisit.service.OrderService;

@Controller
@RequestMapping("/manager")
public class WaiterController {

    private final OrderService orderService;
    private final DishService dishService;
    private final CategoryService categoryService;

    public WaiterController(OrderService orderService,
                            DishService dishService,
                            CategoryService categoryService) {
        this.orderService = orderService;
        this.dishService = dishService;
        this.categoryService = categoryService;
    }

    /**
     * Головне табло замовлень ресторану (робоче місце офіціанта/адміна).
     */
    @GetMapping("/orders")
    public String getOrdersDashboard(@RequestParam Long restaurantId,
                                     @AuthenticationPrincipal User currentUser,
                                     Model model) {
        // Перевірка приналежності до тенанта
        if (!currentUser.getRestaurant().getId().equals(restaurantId)) {
            return "redirect:/error?message=AccessDeniedToTenant";
        }

        model.addAttribute("orders", orderService.getOrdersByRestaurant(restaurantId));
        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("userRole", currentUser.getRole().name());

        return "manager/orders";
    }

    /**
     * Зміна статусу замовлення.
     */
    @PostMapping("/orders/{id}/status")
    public String changeOrderStatus(@PathVariable Long id,
                                    @RequestParam Order.Status status,
                                    @RequestParam Long restaurantId,
                                    @AuthenticationPrincipal User currentUser) {
        // Не даємо змінити статус замовлення в чужому ресторані
        if (!currentUser.getRestaurant().getId().equals(restaurantId)) {
            return "redirect:/error?message=AccessDeniedToTenant";
        }

        orderService.updateStatus(id, status);
        return "redirect:/manager/orders?restaurantId=" + restaurantId;
    }

    /**
     * Сторінка керування меню (стоп-листи страв).
     */
    @GetMapping("/menu")
    public String getMenuManagement(@RequestParam Long restaurantId,
                                    @AuthenticationPrincipal User currentUser,
                                    Model model) {
        // Перевірка приналежності до тенанта
        if (!currentUser.getRestaurant().getId().equals(restaurantId)) {
            return "redirect:/error?message=AccessDeniedToTenant";
        }

        model.addAttribute("dishes", dishService.getAllDishesByRestaurant(restaurantId));
        model.addAttribute("categories", categoryService.getCategoriesByRestaurant(restaurantId));
        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("userRole", currentUser.getRole().name());

        return "manager/menu";
    }

    /**
     * Миттєве перемикання доступності страви (стоп-лист кухні).
     */
    @PostMapping("/menu/dish/{id}/toggle")
    public String toggleDishAvailability(@PathVariable Long id,
                                         @RequestParam Long restaurantId,
                                         @AuthenticationPrincipal User currentUser) {
        // Заборона модифікації стоп-листа іншого закладу
        if (!currentUser.getRestaurant().getId().equals(restaurantId)) {
            return "redirect:/error?message=AccessDeniedToTenant";
        }

        dishService.toggleAvailability(id);
        return "redirect:/manager/menu?restaurantId=" + restaurantId;
    }
}