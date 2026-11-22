package com.example.composers.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.example.composers.exceptions.CsvException;

// Implementacion de CrudOperator que guarda los datos en ficheros .csv
// Cada elemento es una linea del fichero y su primer campo es la clave (el nombre del compositor o el id de la obra)
@Service
@Qualifier("csv")
public class CsvUtils implements CrudOperator {

    // Create: añade un elemento (ya en formato csv) al final del fichero
    @Override
    public void create(String elementAsCSV, String csvPath) {
        List<String> lines = readAllLines(csvPath);
        lines.add(elementAsCSV);
        writeAllLines(csvPath, lines);
    }

    // Read: devuelve todas las lineas del fichero menos la cabecera
    @Override
    public String[] read(String csvPath) {
        return readAllLines(csvPath).stream()
                .skip(1)
                .filter(line -> !line.isBlank())
                .toArray(String[]::new);
    }

    // Update: cambia la linea cuya clave es "key" por el elemento editado
    @Override
    public void update(String key, String elementAsCSV, String csvPath) {
        List<String> lines = readAllLines(csvPath);
        for (int i = 1; i < lines.size(); i++) {
            if (getKey(lines.get(i)).equals(key)) {
                lines.set(i, elementAsCSV);
            }
        }
        writeAllLines(csvPath, lines);
    }

    // Delete: borra la linea cuya clave es "key"
    @Override
    public void delete(String key, String csvPath) {
        List<String> lines = readAllLines(csvPath);
        String header = lines.remove(0);
        lines.removeIf(line -> getKey(line).equals(key));
        lines.add(0, header);
        writeAllLines(csvPath, lines);
    }

    // Quita del texto lo que romperia el .csv (el separador ";" y los saltos de linea)
    public static String clean(String text) {
        return text == null ? "" : text.replace(';', ',').replaceAll("\\R", " ").trim();
    }

    // La clave es el primer campo de la linea
    private String getKey(String line) {
        return line.split(";", -1)[0];
    }

    private List<String> readAllLines(String csvPath) {
        try {
            return new ArrayList<>(Files.readAllLines(Path.of(csvPath), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new CsvException("No se pudo leer el fichero " + csvPath);
        }
    }

    private void writeAllLines(String csvPath, List<String> lines) {
        try {
            Files.write(Path.of(csvPath), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CsvException("No se pudo escribir el fichero " + csvPath);
        }
    }
}
