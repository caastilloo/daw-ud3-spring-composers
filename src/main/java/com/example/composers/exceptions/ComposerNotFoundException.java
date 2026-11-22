package com.example.composers.exceptions;

// Se lanza cuando se busca un compositor que no existe
// Es unchecked (hereda de RuntimeException), asi que no hace falta poner throws en los metodos
public class ComposerNotFoundException extends RuntimeException {

    private final String name;

    public ComposerNotFoundException(String name) {
        super("No existe ningún compositor llamado \"" + name + "\"");
        this.name = name;
    }

    // Nombre que se ha buscado, para enseñarlo en composerNotFoundView
    public String getName() {
        return name;
    }
}
