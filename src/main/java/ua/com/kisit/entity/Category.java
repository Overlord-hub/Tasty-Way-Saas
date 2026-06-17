package ua.com.kisit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Назва категорії меню (наприклад: "Піца", "Салати", "Гарячі напої")
    @Column(nullable = false, length = 100)
    private String name;

    // Картинка для гарного відображення іконки або банера категорії на фронтенді
    @Column(name = "image_category")
    private String imageCategory;

    // Прив'язка до конкретного ресторану (Multi-tenant ізоляція)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // Список страв, що входять до цієї категорії
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Dish> dishes;
}