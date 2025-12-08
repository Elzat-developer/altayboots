package altay.boots.altayboots.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 🔥 ВАЖНО: URL-путь, который будет использоваться в браузере.
        String urlPath = "/uploads/**";

        // 🔥 ВАЖНО: Локальный путь на диске, где реально лежат файлы.
        // Используйте "file:" и прямые слэши "/"
        String fileLocation = "file:C:/uploads/";

        // Регистрируем обработчик ресурсов
        registry.addResourceHandler(urlPath)
                .addResourceLocations(fileLocation);
    }
}
