package ua.com.kisit.configuration;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import ua.com.kisit.entity.User;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // Отримуємо об'єкт користувача, який авторизувався
        User user = (User) authentication.getPrincipal();

        String redirectUrl;

        // Перевіряємо, чи це Суберадмін усієї SaaS-платформи
        if (user.getRole() == User.Role.ROLE_SUPERADMIN) {
            // Суберадміна ведемо на панель генерації ліцензійних ключів
            redirectUrl = "/superadmin/keys";
        } else {
            // Персонал ресторану (ADMIN, MANAGER, WAITER) ведемо на замовлення їхнього тенанта
            if (user.getRestaurant() != null) {
                Long restaurantId = user.getRestaurant().getId();
                redirectUrl = "/manager/orders?restaurantId=" + restaurantId;
            } else {
                // Страховка на випадок, якщо у звичайного юзера чомусь немає ресторану
                redirectUrl = "/login?error=no_restaurant";
            }
        }

        // Виконуємо редірект
        response.sendRedirect(request.getContextPath() + redirectUrl);
    }
}