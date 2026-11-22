package com.example.composers.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.composers.services.CsvUtils;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

// Compositor historico
public class Composer {

    // Nombre artistico, usado como clave
    @Size(max = 60, message = "El nombre artístico no puede superar los 60 caracteres")
    private String name;
    // Nombre real. Ej: "Franz Liszt". Puede estar vacio si se escribe el nombre artistico
    @Size(max = 100, message = "El nombre real no puede superar los 100 caracteres")
    private String realName;
    @Size(max = 1000, message = "La biografía no puede superar los 1000 caracteres")
    private String biography;
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe estar en el pasado")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    @PastOrPresent(message = "La fecha de fallecimiento no puede estar en el futuro")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate deathDate;
    @NotEmpty(message = "La nacionalidad es obligatoria")
    @Size(max = 40, message = "La nacionalidad no puede superar los 40 caracteres")
    private String nationality;
    @NotEmpty(message = "El lugar de nacimiento es obligatorio")
    @Size(max = 80, message = "El lugar de nacimiento no puede superar los 80 caracteres")
    private String birthPlace;
    @Size(max = 80, message = "El lugar de fallecimiento no puede superar los 80 caracteres")
    private String deathPlace;
    private List<String> photos = new ArrayList<>();
    private List<MusicalPiece> pieces = new ArrayList<>();

    public Composer() {
    }

    // Constructor a partir de una linea del .csv ya separada: cada posicion del array es un campo
    public Composer(String[] fields) {
        name = fields[0];
        realName = fields[1];
        biography = fields[2];
        birthDate = LocalDate.parse(fields[3]);
        deathDate = fields[4].isBlank() ? null : LocalDate.parse(fields[4]);
        nationality = fields[5];
        birthPlace = fields[6];
        deathPlace = fields[7].isBlank() ? null : fields[7];
        photos = fields[8].isBlank() ? new ArrayList<>() : new ArrayList<>(Arrays.asList(fields[8].split("\\|")));
    }

    // Devuelve el compositor como una linea del .csv (las fotos van separadas por |)
    public String toCsv() {
        return String.join(";", CsvUtils.clean(name), CsvUtils.clean(realName), CsvUtils.clean(biography),
                String.valueOf(birthDate), deathDate == null ? "" : deathDate.toString(),
                CsvUtils.clean(nationality), CsvUtils.clean(birthPlace), CsvUtils.clean(deathPlace),
                String.join("|", photos));
    }

    // Añade una obra a la lista de composiciones del compositor
    public void addPiece(MusicalPiece piece) {
        pieces.add(piece);
    }

    // Validacion: debe haber al menos un nombre (artistico o real)
    @AssertTrue(message = "Escribe al menos el nombre artístico o el nombre real")
    public boolean isSomeNameFilled() {
        return (name != null && !name.isEmpty()) || (realName != null && !realName.isEmpty());
    }

    // Validacion: si hay fecha de fallecimiento, no puede ser anterior a la de nacimiento
    @AssertTrue(message = "La fecha de fallecimiento no puede ser anterior a la de nacimiento")
    public boolean isDeathAfterBirth() {
        return birthDate == null || deathDate == null || !deathDate.isBefore(birthDate);
    }

    // Si falta el nombre artistico o el real, copia en el que falta el que si se ha escrito
    public void completeNames() {
        if (name == null || name.isEmpty()) {
            name = realName;
        }
        if (realName == null || realName.isEmpty()) {
            realName = name;
        }
    }

    // Indica si el compositor sigue vivo (no tiene fecha de fallecimiento)
    public boolean isAlive() {
        return deathDate == null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName == null ? null : realName.trim();
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography == null ? null : biography.trim();
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public LocalDate getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(LocalDate deathDate) {
        this.deathDate = deathDate;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality == null ? null : nationality.trim();
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace == null ? null : birthPlace.trim();
    }

    public String getDeathPlace() {
        return deathPlace;
    }

    public void setDeathPlace(String deathPlace) {
        this.deathPlace = deathPlace == null ? null : deathPlace.trim();
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }

    public List<MusicalPiece> getPieces() {
        return pieces;
    }

    public void setPieces(List<MusicalPiece> pieces) {
        this.pieces = pieces;
    }
}
