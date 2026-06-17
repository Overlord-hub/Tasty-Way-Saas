package ua.com.kisit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.com.kisit.entity.Order;
import ua.com.kisit.service.OrderService;

import java.util.Map;

@RestController
@RequestMapping("/api/orders") // Сучасний REST-префікс для AJAX/Fetch запитів
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * REST-ендпоінт для миттєвого створення замовлення з LocalStorage браузера.
     * Сюди JS відправить POST-запит із JSON-тілом.
     */
    @PostMapping("/create")
    public ResponseEntity<?> checkoutOrder(@RequestParam Long restaurantId,
                                           @RequestParam Integer tableNumber,
                                           @RequestParam Order.PaymentMethod paymentMethod,
                                           @RequestBody Map<Long, Integer> itemsInCart) {
        try {
            // Викликаємо наш залізобетонний сервіс, який сам перевірить ціни в базі
            Order savedOrder = orderService.createOrder(restaurantId, tableNumber, paymentMethod, itemsInCart);

            // Повертаємо клієнту статус 200 та ID створеного замовлення для сторінки подяки
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "orderId", savedOrder.getId(),
                    "message", "Замовлення успішно надіслано на кухню!"
            ));
        } catch (Exception e) {
            // Якщо щось пішло не так (наприклад, кошик порожній) - повертаємо помилку 400
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}