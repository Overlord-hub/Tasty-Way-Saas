package ua.com.kisit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Назва ресторану (наприклад: "TastyWay Головний", "Суші Експрес")
    @Column(nullable = false, length = 100)
    private String name;

    // Унікальний текстовий ідентифікатор для URL-посилань (наприклад: "main", "sushi-bar")
    // Саме через нього ми будемо будувати гарні посилання типу /restaurant/main/table/3
    @Column(nullable = false, unique = true, length = 50)
    private String slug;

    // HEX-код кольору для індивідуального брендування інтерфейсу закладу (наприклад: "#198754")
    // Це дозволить кожному ресторану мати свій унікальний колір кнопок на фронтенді
    @Column(name = "theme_color", length = 7)
    @Builder.Default
    private String themeColor = "#198754";

    // Прапорець активності закладу (true - працює, false - заблоковано за несплату SaaS підписки)
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // Зворотний зв'язок із категоріями меню (кухнями), які належать цьому ресторану
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Category> cuisines;

    // Зворотний зв'язок із працівниками (менеджерами/офіціантами) цього закладу
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> staff;
}