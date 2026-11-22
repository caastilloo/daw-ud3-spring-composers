package com.example.composers.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Configuracion de MVC: asocia URLs directamente con vistas que no necesitan logica en un controlador
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // Mapea las URLs de la pagina de inicio y la del buscador a sus vistas
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("indexView");
        registry.addViewController("/home").setViewName("indexView");
        registry.addViewController("/index").setViewName("indexView");
        registry.addViewController("/searchComposer").setViewName("searchComposerView");
    }
}
