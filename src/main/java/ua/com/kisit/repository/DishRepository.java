package ua.com.kisit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.kisit.entity.Dish;
import java.util.List;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {

    // Знайти всі страви конкретного ресторану
    List<Dish> findByRestaurantId(Long restaurantId);

    // Знайти лише доступні страви конкретного ресторану, які зараз можна замовити (не в стоп-листі)
    List<Dish> findByRestaurantIdAndIsAvailableTrue(Long restaurantId);

    // Знайти страви всередині конкретної категорії для певного ресторану
    List<Dish> findByRestaurantIdAndCategoryId(Long restaurantId, Long categoryId);
}