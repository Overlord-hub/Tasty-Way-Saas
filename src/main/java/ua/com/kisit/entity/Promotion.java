package ua.com.kisit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // Наприклад: "Знижка 15% на всі Бургери" або "Нічна знижка на каву"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PromotionType type; // PERCENTAGE (відсотки) або FLAT (конкретна сума в ₴)

    @Column(name = "discount_value", nullable = false)
    private Double discountValue; // Значення знижки (наприклад: 15.0 для відсотків або 50.0 для гривень)

    // --- ТАРГЕТИНГ (Гнучкі цілі акції) ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id")
    private Dish targetDish; // Якщо акція на конкретний товар (якщо null - перевіряємо категорію)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category targetCategory; // Якщо акція на всю категорію страв

    @Column(name = "min_order_sum")
    private Double minOrderSum; // Якщо знижка на весь чек (наприклад, від 500 ₴)

    // --- ТИМЧАСОВІ РАМКИ (Сезонність та тривалість) ---

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate; // Дата і час початку акції

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate; // Дата і час завершення акції

    // Зв'язок з рестораном, бо акції у кожного закладу в SaaS свої
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    public enum PromotionType {
        PERCENTAGE, // Знижка у відсотках (%)
        FLAT        // Фіксована знижка в гривнях (₴)
    }
}