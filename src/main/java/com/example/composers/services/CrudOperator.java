package com.example.composers.services;

// Operaciones CRUD separadas de donde se guardan los datos.
// "element" es el elemento ya convertido a texto y "source" el origen de los datos (la ruta del .csv o, mas adelante, la base de datos)
// La interfaz no lleva @Service: la anotacion va en las clases que la implementan
public interface CrudOperator {

    // Create: guarda un elemento nuevo
    void create(String element, String source);

    // Read: devuelve todos los elementos guardados
    String[] read(String source);

    // Update: sustituye el elemento con esa clave por el editado
    void update(String key, String element, String source);

    // Delete: borra el elemento con esa clave
    void delete(String key, String source);
}
