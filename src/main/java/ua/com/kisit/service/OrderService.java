package ua.com.kisit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.com.kisit.entity.Order;
import ua.com.kisit.entity.OrderItem;
import ua.com.kisit.entity.Dish;
import ua.com.kisit.entity.Restaurant;
import ua.com.kisit.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final DishService dishService;
    private final PromotionService promotionService;
    private final RestaurantService restaurantService;

    public OrderService(OrderRepository orderRepository,
                        DishService dishService,
                        PromotionService promotionService,
                        RestaurantService restaurantService) { // Інжектим через конструктор
        this.orderRepository = orderRepository;
        this.dishService = dishService;
        this.promotionService = promotionService;
        this.restaurantService = restaurantService;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByRestaurant(Long restaurantId) {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
    }

    @Transactional
    public Order createOrder(Long restaurantId, Integer tableNumber, Order.PaymentMethod paymentMethod, Map<Long, Integer> itemsInCart) {
        if (itemsInCart == null || itemsInCart.isEmpty()) {
            throw new RuntimeException("Неможливо оформити порожній кошик");
        }

        // 1. Шукаємо, чи є вже відкрите замовлення за цим столиком
        java.util.Optional<Order> existingOrderOpt = orderRepository.findActiveOrderForTable(restaurantId, tableNumber);

        Order order;
        boolean isNewOrder = false;

        if (existingOrderOpt.isPresent()) {
            // якщо стіл ДОЗАМОВЛЯЄ: беремо поточний існуючий чек
            order = existingOrderOpt.get();
        } else {
            // якщо це новий стіл: створюємо чек з нуля
            order = Order.builder()
                    .createdAt(LocalDateTime.now())
                    .tableNumber(tableNumber)
                    .paymentMethod(paymentMethod)
                    .status(Order.Status.NEW)
                    .restaurant(restaurantService.getById(restaurantId))
                    .totalPrice(BigDecimal.ZERO)
                    .items(new java.util.ArrayList<>()) // ініціалізуємо порожній список для страв
                    .build();
            isNewOrder = true;
        }

        // 2. Формуємо OrderItems для нових дозамовлених страв
        final Order finalOrder = order; // константа для лямбди
        List<OrderItem> newItems = itemsInCart.entrySet().stream().map(entry -> {
            Dish dish = dishService.getById(entry.getKey());
            Integer quantity = entry.getValue();

            BigDecimal finalDishPrice = promotionService.calculateDiscountPrice(dish, restaurantId);

            return OrderItem.builder()
                    .dish(dish)
                    .quantity(quantity)
                    .price(finalDishPrice)
                    .order(finalOrder)
                    .build();
        }).toList();

        // 3. Додаємо нові страви до загального списку замовлення
        order.getItems().addAll(newItems);

        // 4. Перераховуємо останій чек з нуля (враховуючи і старі, і нові дозамовлені страви)
        BigDecimal newTotalPrice = order.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Застосовуємо глобальні акції до оновленої суми всього чеку
        newTotalPrice = promotionService.calculateFinalOrderTotal(newTotalPrice, restaurantId);

        order.setTotalPrice(newTotalPrice);

        // Якщо це було дозамовлення, міняємо статус назад на NEW, щоб кухар побачив нові страви
        if (!isNewOrder && order.getStatus() == Order.Status.COOKING) {
            order.setStatus(Order.Status.NEW);
        }

        return orderRepository.save(order);
    }

    @Transactional
    public void updateStatus(Long orderId, Order.Status newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Замовлення з ID " + orderId + " не знайдено"));
        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}