package ua.com.kisit.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.com.kisit.entity.ActivationKey;
import ua.com.kisit.entity.User;
import ua.com.kisit.repository.ActivationKeyRepository;
import ua.com.kisit.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivationKeyRepository activationKeyRepository;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       ActivationKeyRepository activationKeyRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.activationKeyRepository = activationKeyRepository;
    }

    // Метод Spring Security для аутентифікації користувача під час входу в систему
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Співробітника з логіном '" + username + "' не знайдено"));
    }

    // Реєстрація нового користувача (Менеджера або Офіціанта) з шифруванням пароля
    @Transactional
    public User registerNewUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Користувач з логіном '" + user.getUsername() + "' вже існує в системі");
        }

        // Хешуємо пароль перед збереженням у базу даних (безпека по OWASP)
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // Перевірка наявності логіна в базі (для валідації на фронтенді)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * ГЕНЕРАЦІЯ ОДНОРАЗОВОГО ЛІЦЕНЗІЙНОГО КЛЮЧА
     * Цей метод викликається суперадміном для створення нового токена активації.
     */
    @Transactional
    public String generateNewLicenseKey() {
        // Генерує унікальний випадковий рядок за стандартом UUID
        String cleanToken = UUID.randomUUID().toString();

        ActivationKey newKey = ActivationKey.builder()
                .token(cleanToken)
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        activationKeyRepository.save(newKey);
        return cleanToken;
    }
}