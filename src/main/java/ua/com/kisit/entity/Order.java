package ua.com.kisit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Дата та точний час створення замовлення (для виведення на табло офіціанта за хронологією)
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Номер фізичного столика в залі, з якого було відскановано QR-код
    @Column(name = "table_number", nullable = false)
    private Integer tableNumber;

    // Підсумкова вартість усього замовлення (вже з урахуванням усіх застосованих знижок)
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    // Спосіб розрахунку за цим столиком (Готівка офіціанту / Термінал)
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    // Поточний етап життєвого циклу замовлення в залі
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.NEW;

    // Критично для SaaS: замовлення належить конкретному закладу
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // Список конкретних страв та їх кількості у цьому чеку
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;

    // ========== Перерахування (Enums) для чистоти бізнес-логіки ==========

    public enum Status {
        NEW,      // Щойно надійшло від клієнта, горить червоним на табло
        COOKING,  // Прийнято в роботу, готується шеф-кухарем на кухні
        SERVED,   // Страви готові та винесені офіціантом на стіл
        CLOSED    // Замовлення повністю оплачене, чек закритий
    }

    public enum PaymentMethod {
        CASH,     // Розрахунок готівкою через офіціанта
        CARD      // Оплата карткою через POS-термінал закладу
    }
}