package com.example.composers.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.composers.services.CsvUtils;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Obra musical de un compositor
public class MusicalPiece {

    private Integer id;
    @NotEmpty(message = "El título es obligatorio")
    @Size(max = 150, message = "El título no puede superar los 150 caracteres")
    private String title;
    // Nombre artistico del compositor (clave que referencia a {@link Composer})
    @NotEmpty(message = "Selecciona un compositor")
    private String composer;
    // Año de composicion
    @Min(value = 1000, message = "El año debe ser 1000 o posterior")
    @Max(value = 2100, message = "El año no puede ser posterior a 2100")
    private Integer year;
    @NotNull(message = "Elige la instrumentación")
    private Instrumentation instrumentation;
    @Size(max = 300, message = "El enlace no puede superar los 300 caracteres")
    private String url;
    private List<String> alternativeTitles = new ArrayList<>();

    public MusicalPiece() {
    }

    // Constructor a partir de una linea del .csv ya separada: cada posicion del array es un campo
    public MusicalPiece(String[] fields) {
        id = Integer.valueOf(fields[0]);
        title = fields[1];
        composer = fields[2];
        year = fields[3].isBlank() ? null : Integer.valueOf(fields[3]);
        instrumentation = Instrumentation.valueOf(fields[4]);
        url = fields[5];
        alternativeTitles = fields[6].isBlank() ? new ArrayList<>() : new ArrayList<>(Arrays.asList(fields[6].split("\\|")));
    }

    // Devuelve la obra como una linea del .csv (los titulos alternativos van separados por |)
    public String toCsv() {
        return String.join(";", String.valueOf(id), CsvUtils.clean(title), CsvUtils.clean(composer),
                year == null ? "" : year.toString(), instrumentation.name(), CsvUtils.clean(url),
                String.join("|", alternativeTitles.stream().map(t -> CsvUtils.clean(t).replace('|', '/')).toList()));
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer == null ? null : composer.trim();
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public void setInstrumentation(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? null : url.trim();
    }

    public List<String> getAlternativeTitles() {
        return alternativeTitles;
    }

    public void setAlternativeTitles(List<String> alternativeTitles) {
        this.alternativeTitles = alternativeTitles;
    }

    // Los titulos alternativos como texto, uno por linea
    public String getAlternativeTitlesText() {
        return String.join("\n", alternativeTitles);
    }

    // Rellena los titulos alternativos a partir del texto del textarea (uno por linea, ignorando lineas vacias)
    public void setAlternativeTitlesText(String text) {
        alternativeTitles = text == null ? new ArrayList<>()
                : new ArrayList<>(Arrays.stream(text.split("\\R")).map(String::trim).filter(s -> !s.isEmpty()).toList());
    }
}
