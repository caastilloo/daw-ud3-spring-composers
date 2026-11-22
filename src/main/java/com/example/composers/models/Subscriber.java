package com.example.composers.models;

import com.example.composers.services.CsvUtils;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

// Usuario suscrito a un compositor
public class Subscriber {

    // Identificador, se usa en el enlace para anular la suscripcion
    private String id;
    // Nombre artistico del compositor al que se suscribe
    private String composer;
    @NotEmpty(message = "El nombre de usuario es obligatorio")
    private String username;
    @NotEmpty(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;

    public Subscriber() {
    }

    // Constructor a partir de una linea del .csv ya separada
    public Subscriber(String[] fields) {
        id = fields[0];
        composer = fields[1];
        username = fields[2];
        email = fields[3];
    }

    // Devuelve el suscriptor como una linea del .csv
    public String toCsv() {
        return String.join(";", id, CsvUtils.clean(composer), CsvUtils.clean(username), CsvUtils.clean(email));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase();
    }
}
