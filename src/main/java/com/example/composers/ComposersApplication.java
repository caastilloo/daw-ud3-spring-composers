package com.example.composers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

// Clase principal de la aplicacion My Favourite Composers
@SpringBootApplication
// Permite que los metodos con @Async se ejecuten sin que el resto del codigo espere
@EnableAsync
public class ComposersApplication {

    // Arranca la aplicacion Spring Boot
    public static void main(String[] args) {
        SpringApplication.run(ComposersApplication.class, args);
    }
}
