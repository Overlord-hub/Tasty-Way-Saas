package ua.com.kisit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.kisit.entity.User;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Пошук користувача (менеджера/офіціанта) за логіном для авторизації
    Optional<User> findByUsername(String username);

    // Перевірка, чи існує вже такий логін у системі (для реєстрації)
    boolean existsByUsername(String username);
}