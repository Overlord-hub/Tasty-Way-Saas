package ua.com.kisit.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ua.com.kisit.entity.Order;
import ua.com.kisit.entity.User;
import ua.com.kisit.repository.OrderRepository;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import ua.com.kisit.service.OrderPdfReportService;

import java.math.BigDecimal;
import java.io.ByteArrayInputStream;
import java.util.List;

@Controller
@RequestMapping("/manager/analytics")
public class OwnerAnalyticsController {

    private final OrderRepository orderRepository;
    private final OrderPdfReportService pdfReportService;

    public OwnerAnalyticsController(OrderRepository orderRepository, OrderPdfReportService pdfReportService) {
        this.orderRepository = orderRepository;
        this.pdfReportService = pdfReportService;
    }

    /**
     * Головний екран фінансової аналітики для Власника (ADMIN)
     */
    @GetMapping
    public String getOwnerDashboard(@RequestParam Long restaurantId,
                                    @AuthenticationPrincipal User currentUser, // 1. Отримуємо поточного юзера
                                    Model model) {

        // Не дозволяємо не-адмінам або чужим ресторанам бачити касу закладу
        if (!currentUser.getRole().name().equals("ROLE_ADMIN")) {
            return "redirect:/error?message=AccessDenied";
        }
        if (!currentUser.getRestaurant().getId().equals(restaurantId)) {
            return "redirect:/error?message=AccessDeniedToTenant";
        }

        // 1. Рахуємо загальну виручку закладу
        BigDecimal revenue = orderRepository.calculateTotalRevenue(restaurantId);
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }

        // 2. Кількість замовлень на різних етапах
        Long activeOrders = orderRepository.countByRestaurantIdAndStatus(restaurantId, Order.Status.NEW)
                + orderRepository.countByRestaurantIdAndStatus(restaurantId, Order.Status.COOKING);
        Long closedOrders = orderRepository.countByRestaurantIdAndStatus(restaurantId, Order.Status.CLOSED);

        // Отримуємо всі замовлення
        List<Order> allOrders = orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);

        // 3. Рахуємо методи оплати для кругової діаграми
        long cashCount = allOrders.stream()
                .filter(o -> o.getStatus() == Order.Status.CLOSED && o.getPaymentMethod() == ua.com.kisit.entity.Order.PaymentMethod.CASH)
                .count();

        long cardCount = allOrders.stream()
                .filter(o -> o.getStatus() == Order.Status.CLOSED && o.getPaymentMethod() == ua.com.kisit.entity.Order.PaymentMethod.CARD)
                .count();

        // 4. Фільтруємо тільки чисті цифри (ID та Суми) останніх 7 закритих чеків
        List<Order> closedOrdersList = allOrders.stream()
                .filter(o -> o.getStatus() == Order.Status.CLOSED)
                .limit(7)
                .collect(java.util.stream.Collectors.toList());

        // Перевертаємо хронологічно
        java.util.Collections.reverse(closedOrdersList);

        // Робимо чисті прості масиви строк і чисел
        List<String> orderLabels = closedOrdersList.stream().map(o -> "Чек #" + o.getId()).collect(java.util.stream.Collectors.toList());
        List<BigDecimal> orderSums = closedOrdersList.stream().map(Order::getTotalPrice).collect(java.util.stream.Collectors.toList());

        // Передаємо цифри в Thymeleaf
        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("revenue", revenue);
        model.addAttribute("activeOrders", activeOrders);
        model.addAttribute("closedOrders", closedOrders);
        model.addAttribute("cashCount", cashCount);
        model.addAttribute("cardCount", cardCount);

        // Передаємо підготовлені прості масиви для лінійного графіка
        model.addAttribute("orderLabels", orderLabels);
        model.addAttribute("orderSums", orderSums);

        model.addAttribute("orders", allOrders);

        model.addAttribute("userRole", currentUser.getRole().name());

        return "manager/analytics";
    }

    /**
     * Ендпоінт для миттєвої генерації та скачування фінансового PDF звіту
     */
    @GetMapping("/pdf")
    public ResponseEntity<InputStreamResource> downloadPdfReport(@RequestParam Long restaurantId,
                                                                 @AuthenticationPrincipal User currentUser) { // Безпека для PDF

        // Перевірка прав перед вивантаженням комерційного звіту
        if (!currentUser.getRole().name().equals("ROLE_ADMIN") || !currentUser.getRestaurant().getId().equals(restaurantId)) {
            return ResponseEntity.status(403).body(null);
        }

        BigDecimal revenue = orderRepository.calculateTotalRevenue(restaurantId);
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }
        List<Order> orders = orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);

        String restaurantName = currentUser.getRestaurant().getName();

        ByteArrayInputStream bis = pdfReportService.generateFinancialReport(orders, revenue, restaurantName);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=financial_report_restaurant_" + restaurantId + ".pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}