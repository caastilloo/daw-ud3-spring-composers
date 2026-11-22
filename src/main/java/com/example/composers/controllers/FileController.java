package com.example.composers.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.composers.models.Composer;
import com.example.composers.services.ComposerService;
import com.example.composers.services.FileStorageService;
import com.example.composers.services.SubscriberService;

// Controlador de las imagenes de los compositores
@Controller
public class FileController {

    @Autowired
    private ComposerService composerService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private SubscriberService subscriberService;

    // Formulario para añadir una imagen a un compositor
    @GetMapping("/composer/image/{name}")
    public String showImageForm(@PathVariable String name, Model model) {
        model.addAttribute("composer", composerService.getComposer(name));
        return "composerImageFormView";
    }

    // Recibe la imagen (el nombre del parametro coincide con el name del input), la guarda y la añade al compositor
    @PostMapping("/composer/image/{name}/submit")
    public String addImage(@PathVariable String name, @RequestParam MultipartFile file) {
        Composer composer = composerService.getComposer(name);
        String storedFilename = fileStorageService.store(file);
        composerService.addPhoto(composer.getName(), "/files/" + storedFilename);
        // Añadir una imagen tambien modifica el perfil: se avisa a los suscriptores con la imagen adjunta
        subscriberService.notifySubscribers(composer.getName(), storedFilename);
        return "redirect:/composer?name=" + URLEncoder.encode(composer.getName(), StandardCharsets.UTF_8);
    }

    // Devuelve al navegador una imagen guardada en uploadDir. filename:.+ permite que el nombre tenga puntos (la extension)
    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = fileStorageService.loadAsResource(filename);
        return ResponseEntity.ok().body(file);
    }
}
