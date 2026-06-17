package ua.com.kisit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.kisit.entity.Restaurant;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // Пошук ресторану за його унікальним текстовим кодом у URL (наприклад, /restaurant/main-branch)
    Optional<Restaurant> findBySlug(String slug);
}