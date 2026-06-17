package ua.com.kisit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activation_keys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivationKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Сам секретний згенерований токен (наприклад, UUID або випадковий рядок)
    @Column(nullable = false, unique = true, length = 100)
    private String token;

    // Прапорець використання: false - вільний, true - вже використаний
    @Column(name = "is_used", nullable = false)
    private boolean used = false;

    // Дата створення ключа (для аналітики)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Дата, коли ключ був використаний (заповнюється автоматично при реєстрації)
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;
}