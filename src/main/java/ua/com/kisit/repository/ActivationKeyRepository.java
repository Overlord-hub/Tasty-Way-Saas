package ua.com.kisit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.kisit.entity.ActivationKey;
import java.util.Optional;

@Repository
public interface ActivationKeyRepository extends JpaRepository<ActivationKey, Long> {

    // Пошук токена для перевірки його існування
    Optional<ActivationKey> findByToken(String token);
}