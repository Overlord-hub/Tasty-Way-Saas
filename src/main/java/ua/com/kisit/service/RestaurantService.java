package ua.com.kisit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.com.kisit.entity.Restaurant;
import ua.com.kisit.repository.RestaurantRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    // Знайти ресторан за його ID (наприклад, для внутрішньої логіки)
    public Restaurant getById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ресторан не знайдено за ID: " + id));
    }

    // Знайти ресторан за унікальним slug для URL-адреси
    public Restaurant getBySlug(String slug) {
        return restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Ресторан з кодом '" + slug + "' не знайдено"));
    }

    // Отримати список усіх ресторанів на платформі (для головного адміна сайту)
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    // Зберегти або оновити ресторан
    @Transactional
    public Restaurant save(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }
}