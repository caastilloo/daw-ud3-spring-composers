package com.example.composers.exceptions;

// Se lanza al guardar un compositor con un nombre artistico que ya usa otro
// Es unchecked (hereda de RuntimeException), asi que no hace falta poner throws en los metodos
public class DuplicateComposerException extends RuntimeException {

    public DuplicateComposerException(String name) {
        super("Ya existe un compositor con el nombre artístico \"" + name + "\"");
    }
}
