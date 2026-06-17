package ua.com.kisit.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.com.kisit.entity.Dish;
import ua.com.kisit.repository.DishRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DishService {

    private final DishRepository dishRepository;

    public DishService(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    // Отримати абсолютно всі страви ресторану (включаючи ті, що в стоп-листі — для менеджера)
    public List<Dish> getAllDishesByRestaurant(Long restaurantId) {
        return dishRepository.findByRestaurantId(restaurantId);
    }

    // Отримати лише доступні страви для онлайн-меню гостя (фільтр по стоп-листу)
    public List<Dish> getAvailableDishesByRestaurant(Long restaurantId) {
        return dishRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId);
    }

    // Отримати страви конкретної категорії всередині одного ресторану
    public List<Dish> getDishesByRestaurantAndCategory(Long restaurantId, Long categoryId) {
        return dishRepository.findByRestaurantIdAndCategoryId(restaurantId, categoryId);
    }

    // Знайти страву за її ID
    public Dish getById(Long id) {
        return dishRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Страва не знайдена за ID: " + id));
    }

    // Зберегти або оновити страву
    @Transactional
    public Dish save(Dish dish) {
        return dishRepository.save(dish);
    }

    // Швидке перемикання стоп-листа (доступності страви на кухні)
    @Transactional
    public void toggleAvailability(Long dishId) {
        Dish dish = getById(dishId);
        dish.setAvailable(!dish.isAvailable());
        dishRepository.save(dish);
    }

    // Видалити страву з меню
    @Transactional
    public void deleteById(Long id) {
        dishRepository.deleteById(id);
    }

    @Transactional
    public String saveDishImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return "default-dish.jpg"; // Якщо менеджер не завантажив фото, ставимо дефолтну заглушку
        }

        // Створюємо папку "uploads" в корінь проекту, якщо її ще немає
        String uploadDir = "uploads/";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Генеруємо унікальне ім'я файлу, щоб картинки з назвою "pizza.jpg" не перезаписували одна одну
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(uniqueFileName);

        // Фізично копіюємо файл на диск сервера
        Files.copy(file.getInputStream(), filePath);

        return uniqueFileName; // Повертаємо назву файлу, яку ми потім запишемо в БД
    }
}