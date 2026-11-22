package com.example.composers.models;

// Categorias de instrumentacion de una obra musical
public enum Instrumentation {
    // Obras para piano
    PIANO("Piano"),
    // Obras para un unico instrumento distinto del piano
    SOLO("Solista"),
    // Varios interpretes, sin llegar al tamaño de una orquesta
    CHAMBER("Música de cámara"),
    // Obras orquestales (con o sin solista)
    ORCHESTRA("Orquesta"),
    // Obras solo para voces (coral o no)
    VOCAL("Vocal"),
    // Obras escenicas, como la opera (voz y orquesta)
    STAGE("Escénica"),
    // Cualquier otra instrumentacion
    OTHER("Otra");

    private final String label;

    Instrumentation(String label) {
        this.label = label;
    }

    // Texto que se muestra al usuario en las vistas
    public String getLabel() {
        return label;
    }
}
