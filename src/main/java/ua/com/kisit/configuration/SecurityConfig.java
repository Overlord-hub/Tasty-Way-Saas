package ua.com.kisit.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler successHandler;

    // Впроваджуємо наш кастомний обробник успішного логіну
    public SecurityConfig(CustomAuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Вимикаємо для REST API запитів
                .authorizeHttpRequests(auth -> auth
                        // Гостьові зони: QR-меню, API замовлень для клієнтів, статика
                        .requestMatchers("/r/**", "/api/**", "/error", "/register").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/uploads/**", "/images/**", "/favicon.ico").permitAll()

                        // Менеджерська зона з розподілом ролей
                        .requestMatchers("/manager/orders/**").hasAnyRole("WAITER", "MANAGER", "ADMIN")
                        .requestMatchers("/manager/menu/**", "/manager/dish/**", "/manager/promotions/**").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers("/manager/analytics/**", "/manager/pdf/**").hasRole("ADMIN")
                        .requestMatchers("/superadmin/**").hasRole("SUPERADMIN")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        // Вказуємо Spring Security використовувати наш динамічний перенаправлятор
                        .successHandler(successHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}