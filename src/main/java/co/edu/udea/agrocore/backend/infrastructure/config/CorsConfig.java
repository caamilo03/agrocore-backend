package co.edu.udea.agrocore.backend.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a todos los endpoints (rutas) de tu API
                .allowedOrigins("https://agrocore-frontend.pages.dev/") // IMPORTANTE: Reemplaza esto con la URL exacta de tu frontend
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos HTTP permitidos
                .allowedHeaders("*") // Permite cualquier cabecera
                .allowCredentials(true); // Necesario si en el futuro manejas cookies o tokens de autenticación
    }
}