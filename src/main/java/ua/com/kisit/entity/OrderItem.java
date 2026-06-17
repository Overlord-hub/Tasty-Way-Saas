package ua.com.kisit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Кількість порцій цієї страви в замовленні (наприклад: 2 порції супу)
    @Column(nullable = false)
    private Integer quantity;

    // Ціна страви на момент замовлення (щоб аналітика не попливла при зміні прайсу в меню)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Зв'язок із головним замовленням
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Зв'язок із замовленою стравою
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;
}