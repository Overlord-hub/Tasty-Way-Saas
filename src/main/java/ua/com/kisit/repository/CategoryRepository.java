package ua.com.kisit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.kisit.entity.Category;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Отримати всі категорії меню, які належать конкретному закладу
    List<Category> findByRestaurantId(Long restaurantId);
}