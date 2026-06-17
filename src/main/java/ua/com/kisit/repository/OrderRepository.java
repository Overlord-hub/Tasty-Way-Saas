package ua.com.kisit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.com.kisit.entity.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Отримати всі замовлення для конкретного ресторану, відсортовані від найновіших до найстаріших
    List<Order> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    // Отримати замовлення конкретного ресторану за певним статусом (наприклад, лише NEW)
    List<Order> findByRestaurantIdAndStatus(Long restaurantId, Order.Status status);

    // Порахувати загальну виручку ресторану тільки за оплаченими (CLOSED) замовленнями
    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.restaurant.id = :restaurantId AND o.status = 'CLOSED'")
    BigDecimal calculateTotalRevenue(@Param("restaurantId") Long restaurantId);

    // Швидкий підрахунок кількості замовлень ресторану за конкретним статусом
    Long countByRestaurantIdAndStatus(Long restaurantId, Order.Status status);

    //Шукає перше відкрите замовлення столика, яке ще не оплачене (не CLOSED)
    @Query("SELECT o FROM Order o WHERE o.restaurant.id = :restaurantId AND o.tableNumber = :tableNumber AND o.status IN ('NEW', 'COOKING')")
    Optional<Order> findActiveOrderForTable(@Param("restaurantId") Long restaurantId, @Param("tableNumber") Integer tableNumber);
}