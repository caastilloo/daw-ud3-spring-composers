package com.example.composers.exceptions;

// Se lanza cuando se intenta anular una suscripcion que no existe
public class SubscriberNotFoundException extends RuntimeException {

    public SubscriberNotFoundException(String id) {
        super("No existe ninguna suscripción con el código " + id);
    }
}
