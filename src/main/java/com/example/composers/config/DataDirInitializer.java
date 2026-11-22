package com.example.composers.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

// Al arrancar, prepara las carpetas que la aplicacion necesita en el sistema de ficheros.
// Dentro de un .jar (o de un contenedor) no existe src/main/resources, asi que los .csv
// iniciales se copian desde el classpath a app.data-dir si todavia no estan alli.
@Component
public class DataDirInitializer implements ApplicationRunner {

    private static final String[] CSV_FILES = {"composers.csv", "pieces.csv", "subscribers.csv"};

    @Value("${app.data-dir}")
    private String dataDir;

    @Override
    public void run(ApplicationArguments args) throws IOException {
        Path dir = Path.of(dataDir);
        Files.createDirectories(dir);
        for (String name : CSV_FILES) {
            Path target = dir.resolve(name);
            if (Files.notExists(target)) {
                try (InputStream in = new ClassPathResource("data/csv/" + name).getInputStream()) {
                    Files.copy(in, target);
                }
            }
        }
        // Carpeta donde FileStorageService guarda las imagenes subidas
        Files.createDirectories(Path.of("uploadDir"));
    }
}
