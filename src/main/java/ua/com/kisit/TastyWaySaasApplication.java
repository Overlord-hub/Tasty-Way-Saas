package ua.com.kisit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "ua.com.kisit.entity")
@EnableJpaRepositories(basePackages = "ua.com.kisit.repository")
public class TastyWaySaasApplication {

    public static void main(String[] args) {
        SpringApplication.run(TastyWaySaasApplication.class, args);
    }
}
//package ua.com.kisit;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.autoconfigure.domain.EntityScan;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Додай цей імпорт
//
//@SpringBootApplication
//@EntityScan(basePackages = "ua.com.kisit.entity")
//@EnableJpaRepositories(basePackages = "ua.com.kisit.repository")
//public class TastyWaySaasApplication {
//
//    public static void main(String[] args) {
//        // ТИМЧАСОВО: Генеруємо точний хеш для слова "manager123"
//        String exactHash = new BCryptPasswordEncoder().encode("manager123");
//        System.out.println("=========================================");
//        System.out.println("КРИТИЧНО! СКОПІЮЙ ЦЕЙ ХЕШ В БД: " + exactHash);
//        System.out.println("=========================================");
//
//        SpringApplication.run(TastyWaySaasApplication.class, args);
//    }
//}