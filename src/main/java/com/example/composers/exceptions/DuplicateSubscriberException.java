package com.example.composers.exceptions;

// Se lanza cuando un email ya esta suscrito a ese compositor
public class DuplicateSubscriberException extends RuntimeException {

    public DuplicateSubscriberException(String email, String composer) {
        super("El email " + email + " ya está suscrito a " + composer);
    }
}
