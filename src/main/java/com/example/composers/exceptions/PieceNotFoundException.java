package com.example.composers.exceptions;

// Se lanza cuando se busca una obra que no existe
// Es unchecked (hereda de RuntimeException), asi que no hace falta poner throws en los metodos
public class PieceNotFoundException extends RuntimeException {

    public PieceNotFoundException(int id) {
        super("No existe ninguna obra con el id " + id);
    }
}
