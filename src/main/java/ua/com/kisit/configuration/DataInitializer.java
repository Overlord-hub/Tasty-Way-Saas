package ua.com.kisit.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ua.com.kisit.entity.User;
import ua.com.kisit.repository.ActivationKeyRepository;
import ua.com.kisit.repository.UserRepository;
import ua.com.kisit.service.UserService;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final UserRepository userRepository;
    private final ActivationKeyRepository activationKeyRepository;
    private final PasswordEncoder passwordEncoder; // Впроваджуємо енкодер паролів

    public DataInitializer(UserService userService,
                           UserRepository userRepository,
                           ActivationKeyRepository activationKeyRepository,
                           PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.activationKeyRepository = activationKeyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // 1. Створення суперадміна
        if (!userRepository.findByUsername("superadmin").isPresent()) {
            System.out.println("====== SAAS СИСТЕМА: СТВОРЕННЯ ОБЛІКОВОГО ЗАПИСУ СУБЕРАДМІНА ======");

            User superAdmin = User.builder()
                    .username("superadmin")
                    .password(passwordEncoder.encode("admin2026"))
                    .firstName("Супер")
                    .lastName("Адміністратор")
                    .role(User.Role.ROLE_SUPERADMIN)
                    .restaurant(null)
                    .build();

            userRepository.save(superAdmin);
            System.out.println("Суберадмін успішно створений!");
            System.out.println("Логін: superadmin | Пароль: admin2026");
            System.out.println("=================================================================");
        }
    }
}