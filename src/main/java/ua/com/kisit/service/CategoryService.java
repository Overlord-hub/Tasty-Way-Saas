package ua.com.kisit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.com.kisit.entity.Category;
import ua.com.kisit.repository.CategoryRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Отримати всі категорії конкретного ресторану
    public List<Category> getCategoriesByRestaurant(Long restaurantId) {
        return categoryRepository.findByRestaurantId(restaurantId);
    }

    // Знайти конкретну категорію за ID
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Категорію меню не знайдено за ID: " + id));
    }

    // Зберегти нову або оновити існуючу категорію
    @Transactional
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    // Видалити категорію з бази даних
    @Transactional
    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }
}