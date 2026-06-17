package ua.com.kisit.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationForm {
    // Дані для створення ресторану
    private String restaurantName;
    private String restaurantSlug;

    // Дані для створення власника (Адміністратора)
    private String username;
    private String password;
    private String firstName;
    private String lastName;

    //Спеціальний код
    private String activationCode;
}