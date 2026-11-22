package com.example.composers.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

// Servicio que guarda, recupera y borra los ficheros que suben los usuarios (las imagenes de los compositores)
@Service
public class FileStorageService {

    // Path (java.nio.file) representa una ruta del sistema de ficheros. Paths.get crea la ruta a partir de un texto.
    // "uploadDir" es relativa, asi que es la carpeta uploadDir de la raiz del proyecto (o la del .jar)
    private final Path rootLocation = Paths.get("uploadDir");

    // Guarda el fichero en uploadDir y devuelve el nombre con el que se ha guardado
    public String store(MultipartFile file) throws RuntimeException {
        if (file.isEmpty()) throw new RuntimeException("El fichero está vacío");
        // StringUtils (Spring) tiene metodos de ayuda para Strings.
        // cleanPath normaliza la ruta: quita los "." y "..", y cambia las barras \ por /
        String filename = StringUtils.cleanPath(file.getOriginalFilename());

        // Si despues de limpiarlo sigue teniendo "..", alguien intenta salirse de la carpeta
        if (filename.contains("..")) throw new RuntimeException("Fichero no válido");
        // getFilenameExtension devuelve la extension del nombre (lo que va despues del ultimo punto), sin el punto
        String extension = StringUtils.getFilenameExtension(filename);
        // El nombre guardado son los milisegundos actuales, asi no se repite y no se sobrescriben ficheros
        String storedFilename = System.currentTimeMillis() + "." + extension;

        // InputStream es un flujo de bytes de entrada: se lee el contenido del fichero subido poco a poco.
        // Va en un try-with-resources para que el flujo se cierre solo al terminar
        try (InputStream inputStream = file.getInputStream()) {
            // Files (java.nio.file) tiene metodos estaticos para trabajar con ficheros.
            // copy copia los bytes del flujo a la ruta destino. resolve une la carpeta con el nombre del fichero.
            // REPLACE_EXISTING: si ya existiera un fichero con ese nombre, lo sustituye
            Files.copy(inputStream, this.rootLocation.resolve(storedFilename), StandardCopyOption.REPLACE_EXISTING);
            return storedFilename;
        } catch (IOException ioe) {
            throw new RuntimeException("Error al guardar el fichero");
        }
    }

    // Borra un fichero de uploadDir
    public void delete(String filename) throws RuntimeException {
        try {
            Path file = rootLocation.resolve(filename);
            // Files.exists comprueba si el fichero existe y Files.delete lo borra
            if (!Files.exists(file)) throw new RuntimeException("El fichero no existe");
            Files.delete(file);
        } catch (IOException ioe) {
            throw new RuntimeException("Error al borrar el fichero");
        }
    }

    // Devuelve el fichero como un Resource para poder enviarlo al navegador
    public Resource loadAsResource(String filename) throws RuntimeException {
        try {
            Path file = rootLocation.resolve(filename);
            // Resource (Spring) es una interfaz que representa un recurso (un fichero, una URL...) y permite leerlo.
            // UrlResource es la implementacion que accede al recurso a partir de una URL; toUri pasa la ruta a URI (file:/...)
            Resource resource = new UrlResource(file.toUri());
            // exists indica si el recurso existe y isReadable si se puede leer
            if (resource.exists() || resource.isReadable()) return resource;
            else throw new RuntimeException("Error de lectura del fichero");
        } catch (Exception e) {
            throw new RuntimeException("Error de lectura del fichero");
        }
    }

    // Ruta completa de un fichero guardado (para adjuntarlo en un email)
    public String getFilePath(String filename) {
        return rootLocation.resolve(filename).toString();
    }
}
