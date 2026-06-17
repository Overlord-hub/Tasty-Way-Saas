package ua.com.kisit.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Отримуємо абсолютний шлях до папки uploads у корені проєкту
        String uploadPosixPath = Paths.get("uploads").toAbsolutePath().toUri().toString();

        // Якщо запит йде на /uploads/**, шукати фізичний файл у цій папці
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPosixPath);
    }
}