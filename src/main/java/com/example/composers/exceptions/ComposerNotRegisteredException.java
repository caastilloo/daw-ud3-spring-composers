package com.example.composers.exceptions;

// Se lanza al guardar una obra de un compositor que no esta registrado
// Es unchecked (hereda de RuntimeException), asi que no hace falta poner throws en los metodos
public class ComposerNotRegisteredException extends RuntimeException {

    public ComposerNotRegisteredException(String name) {
        super("El compositor \"" + name + "\" no está registrado");
    }
}
