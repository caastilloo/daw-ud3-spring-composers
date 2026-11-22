package com.example.composers.exceptions;

// Se lanza cuando no se puede leer o escribir un fichero .csv
// Es unchecked (hereda de RuntimeException), asi que no hace falta poner throws en los metodos
public class CsvException extends RuntimeException {

    public CsvException(String msg) {
        super(msg);
    }
}
