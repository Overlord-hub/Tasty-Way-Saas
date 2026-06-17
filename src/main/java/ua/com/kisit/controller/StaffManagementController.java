package ua.com.kisit.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ua.com.kisit.entity.User;
import ua.com.kisit.service.UserService;

@Controller
@RequestMapping("/manager/staff")
public class StaffManagementController {

    private final UserService userService;

    public StaffManagementController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/add")
    public String showAddStaffForm(@AuthenticationPrincipal User manager, Model model) {

        // Передаємо id ресторану в модель, щоб HTML сторінка знала, куди повертатися при скасуванні
        if (manager != null && manager.getRestaurant() != null) {
            model.addAttribute("restaurantId", manager.getRestaurant().getId());
        } else {
            model.addAttribute("restaurantId", 1); // Страховка
        }

        model.addAttribute("newStaff", new User());
        model.addAttribute("userRole", manager.getRole().name());

        return "manager/add_staff";
    }

    @PostMapping("/add")
    public String registerStaff(@ModelAttribute("newStaff") User newStaff,
                                @AuthenticationPrincipal User manager) {

        newStaff.setRestaurant(manager.getRestaurant());
        userService.registerNewUser(newStaff);

        return "redirect:/manager/orders?restaurantId=" + manager.getRestaurant().getId();
    }
}