package com.example.composers.exceptions;

// Excepcion de prueba del enunciado. Hereda de Exception, asi que es checked:
// el metodo que la lanza tiene que declararla con throws
public class CustomException extends Exception {

    public CustomException(String msg) {
        super(msg);
    }
}
