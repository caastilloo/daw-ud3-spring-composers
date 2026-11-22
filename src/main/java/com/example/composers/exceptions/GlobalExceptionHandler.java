package com.example.composers.exceptions;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

// Gestion global de errores: recoge las excepciones de todos los controladores sin tener que poner try-catch
@ControllerAdvice
public class GlobalExceptionHandler {

    // Excepciones comunes (cualquier RuntimeException que no tenga un handler mas concreto)
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, Model model) {
        model.addAttribute("txtError", ex.getMessage());
        return "errorView";
    }

    // Excepcion de prueba (checked)
    @ExceptionHandler(CustomException.class)
    public String handleCustomException(CustomException ex, Model model) {
        model.addAttribute("txtError", "Exception: " + ex.getMessage());
        return "errorView";
    }

    // Compositor que no existe: se reutiliza la vista que ya tenia para este caso
    @ExceptionHandler(ComposerNotFoundException.class)
    public String handleComposerNotFound(ComposerNotFoundException ex, Model model) {
        model.addAttribute("name", ex.getName());
        return "composerNotFoundView";
    }

    // Obra que no existe
    @ExceptionHandler(PieceNotFoundException.class)
    public String handlePieceNotFound(PieceNotFoundException ex, Model model) {
        model.addAttribute("txtError", ex.getMessage());
        return "errorView";
    }

    // Al crear un compositor con un nombre repetido: errorView con enlace al formulario de compositor
    @ExceptionHandler(DuplicateComposerException.class)
    public String handleDuplicateComposer(DuplicateComposerException ex, Model model) {
        model.addAttribute("txtError", ex.getMessage());
        model.addAttribute("formUrl", "/composer/add");
        model.addAttribute("formName", "compositor");
        return "errorView";
    }

    // Al crear una obra de un compositor no registrado: errorView con enlace al formulario de obra
    @ExceptionHandler(ComposerNotRegisteredException.class)
    public String handleComposerNotRegistered(ComposerNotRegisteredException ex, Model model) {
        model.addAttribute("txtError", ex.getMessage());
        model.addAttribute("formUrl", "/piece/add");
        model.addAttribute("formName", "obra");
        return "errorView";
    }

    // Email ya suscrito a ese compositor
    @ExceptionHandler(DuplicateSubscriberException.class)
    public String handleDuplicateSubscriber(DuplicateSubscriberException ex, Model model) {
        model.addAttribute("txtError", ex.getMessage());
        return "errorView";
    }

    // Enlace de anular suscripcion que ya no existe
    @ExceptionHandler(SubscriberNotFoundException.class)
    public String handleSubscriberNotFound(SubscriberNotFoundException ex, Model model) {
        model.addAttribute("txtError", ex.getMessage());
        return "errorView";
    }

    // Fichero mas grande que el limite de application.properties.
    // Por debajo es una IllegalStateException, pero Spring la entrega envuelta en MaxUploadSizeExceededException
    @ExceptionHandler({IllegalStateException.class, MaxUploadSizeExceededException.class})
    public String handleMaxSize(Exception ex, Model model) {
        model.addAttribute("txtError", "La imagen es demasiado grande. El tamaño máximo es 256 KB.");
        return "errorView";
    }

    // Error al leer o escribir los .csv
    @ExceptionHandler(CsvException.class)
    public String handleCsvException(CsvException ex, Model model) {
        model.addAttribute("txtError", "Error con los datos: " + ex.getMessage());
        return "errorView";
    }
}
