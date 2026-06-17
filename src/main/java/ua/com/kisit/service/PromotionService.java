package ua.com.kisit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.com.kisit.entity.Dish;
import ua.com.kisit.entity.Promotion;
import ua.com.kisit.entity.Promotion.PromotionType;
import ua.com.kisit.repository.PromotionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    public Promotion getById(Long id) {
        return promotionRepository.findById(id).orElse(null);
    }

    public List<Promotion> getAllByRestaurant(Long restaurantId) {
        return promotionRepository.findByRestaurantId(restaurantId);
    }

    @Transactional
    public Promotion save(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    @Transactional
    public void deleteById(Long id) {
        promotionRepository.deleteById(id);
    }

    /**
     * Розрахунок акційної ціни для конкретної страви з використанням BigDecimal.
     * Пробігається по всіх активних акціях закладу і застосовує найкращу знижку.
     */
    public BigDecimal calculateDiscountPrice(Dish dish, Long restaurantId) {
        List<Promotion> activePromotions = promotionRepository.findActivePromotions(restaurantId, LocalDateTime.now());

        BigDecimal currentPrice = dish.getPrice();
        BigDecimal bestPrice = currentPrice;

        if (activePromotions == null || activePromotions.isEmpty()) {
            return currentPrice;
        }

        for (Promotion promo : activePromotions) {
            boolean applies = false;

            // 1. Перевіряємо, чи акція націлена саме на цю страву
            if (promo.getTargetDish() != null && promo.getTargetDish().getId().equals(dish.getId())) {
                applies = true;
            }
            // 2. Або перевіряємо, чи акція діє на всю категорію цієї страви
            else if (promo.getTargetCategory() != null && promo.getTargetCategory().getId().equals(dish.getCategory().getId())) {
                applies = true;
            }

            // Якщо акція підходить, рахуємо нову ціну за правилами BigDecimal
            if (applies) {
                BigDecimal calculatedPrice = currentPrice;
                // Перетворюємо discountValue (Double) у BigDecimal для точних розрахунків
                BigDecimal promoValue = BigDecimal.valueOf(promo.getDiscountValue());

                if (promo.getType() == PromotionType.PERCENTAGE) {
                    // Формула: price * (1 - percent / 100)
                    BigDecimal discountPercentage = promoValue.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                    BigDecimal discountAmount = currentPrice.multiply(discountPercentage);
                    calculatedPrice = currentPrice.subtract(discountAmount);
                } else if (promo.getType() == PromotionType.FLAT) {
                    // Формула: price - flat_value
                    calculatedPrice = currentPrice.subtract(promoValue);
                }

                // Гарантуємо, що ціна не впаде нижче нуля
                if (calculatedPrice.compareTo(BigDecimal.ZERO) < 0) {
                    calculatedPrice = BigDecimal.ZERO;
                }

                // Обираємо максимальну знижку для клієнта (найменшу ціну)
                if (calculatedPrice.compareTo(bestPrice) < 0) {
                    bestPrice = calculatedPrice;
                }
            }
        }

        // Округляємо фінальну вартість до 2 знаків після коми (копійки)
        return bestPrice.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Швидка перевірка: чи відрізняється акційна ціна від базової.
     * Знадобиться у Thymeleaf, щоб знати, чи малювати закреслену стару ціну.
     */
    public boolean hasDiscount(Dish dish, Long restaurantId) {
        BigDecimal originalPrice = dish.getPrice().setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountPrice = calculateDiscountPrice(dish, restaurantId);
        return discountPrice.compareTo(originalPrice) < 0;
    }

    /**
     * Розрахунок глобальних акцій на суму всього чеку (Order-Total-Specific).
     * Якщо кошик перевищує minOrderSum, застосовуємо фінальну знижку до всього замовлення.
     */
    public BigDecimal calculateFinalOrderTotal(BigDecimal currentOrderTotal, Long restaurantId) {
        List<Promotion> activePromotions = promotionRepository.findActivePromotions(restaurantId, LocalDateTime.now());
        BigDecimal bestTotal = currentOrderTotal;

        if (activePromotions == null || activePromotions.isEmpty()) {
            return currentOrderTotal;
        }

        for (Promotion promo : activePromotions) {
            // Шукаємо акції, які діють на весь чек (у яких страва та категорія порожні, але вказана мін. сума)
            if (promo.getTargetDish() == null && promo.getTargetCategory() == null && promo.getMinOrderSum() != null) {

                BigDecimal minSum = BigDecimal.valueOf(promo.getMinOrderSum());

                // Перевіряємо, чи чек клієнта дотягнув до потрібної суми
                if (currentOrderTotal.compareTo(minSum) >= 0) {
                    BigDecimal calculatedTotal = currentOrderTotal;
                    BigDecimal promoValue = BigDecimal.valueOf(promo.getDiscountValue());

                    if (promo.getType() == PromotionType.PERCENTAGE) {
                        BigDecimal discountPercentage = promoValue.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                        BigDecimal discountAmount = currentOrderTotal.multiply(discountPercentage);
                        calculatedTotal = currentOrderTotal.subtract(discountAmount);
                    } else if (promo.getType() == PromotionType.FLAT) {
                        calculatedTotal = currentOrderTotal.subtract(promoValue);
                    }

                    if (calculatedTotal.compareTo(BigDecimal.ZERO) < 0) {
                        calculatedTotal = BigDecimal.ZERO;
                    }

                    if (calculatedTotal.compareTo(bestTotal) < 0) {
                        bestTotal = calculatedTotal;
                    }
                }
            }
        }
        return bestTotal.setScale(2, RoundingMode.HALF_UP);
    }
}