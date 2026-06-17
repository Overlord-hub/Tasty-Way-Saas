package ua.com.kisit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "dishes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Назва страви (наприклад: "Борщ український", "Капучино")
    @Column(nullable = false, length = 150)
    private String name;

    // Опис інгредієнтів, ваги або виходу порції
    @Column(length = 500)
    private String description;

    // Точна ціна страви в ресторані
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Фотографія страви для меню сайту
    @Column(name = "image_dish")
    private String imageDish;

    // Прапорець доступності на кухні (true - можна замовити, false - стоп-лист)
    @Column(name = "is_available", nullable = false)
    private boolean isAvailable = true;

    // Прив'язка страви до категорії меню
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Прив'язка страви до конкретного ресторану (тенанта)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
}