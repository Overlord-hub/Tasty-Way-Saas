package ua.com.kisit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.com.kisit.entity.Promotion;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    // Знайти всі акції ресторану
    List<Promotion> findByRestaurantId(Long restaurantId);

    // Знайти лише ті акції ресторану, які активні прямо зараз за датами
    @Query("SELECT p FROM Promotion p WHERE p.restaurant.id = :restaurantId " +
            "AND :now BETWEEN p.startDate AND p.endDate")
    List<Promotion> findActivePromotions(@Param("restaurantId") Long restaurantId,
                                         @Param("now") LocalDateTime now);
}