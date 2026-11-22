package com.example.composers.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.composers.exceptions.ComposerNotFoundException;
import com.example.composers.exceptions.ComposerNotRegisteredException;
import com.example.composers.exceptions.DuplicateComposerException;
import com.example.composers.exceptions.PieceNotFoundException;
import com.example.composers.models.Composer;
import com.example.composers.models.MusicalPiece;
import com.example.composers.services.ComposerService;
import com.example.composers.services.FileStorageService;
import com.example.composers.services.SubscriberService;

import jakarta.validation.Valid;

// Controlador de los formularios para añadir y editar compositores y obras
@Controller
public class FormController {

    @Autowired
    private ComposerService composerService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private SubscriberService subscriberService;

    // Añade la lista de compositores al modelo de todas las peticiones de este controlador (desplegable de obras)
    @ModelAttribute("composers")
    public List<Composer> composers() {
        return composerService.getComposersSortedByName();
    }

    // Codifica un texto para poder ponerlo en una URL (los espacios como %20 para que tambien valga dentro de la ruta)
    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8).replace("+", "%20");
    }

    // Traduce el codigo de error que llega en la URL al mensaje que se enseña arriba del formulario
    private String errorMessage(Integer err) {
        switch (err) {
            case 1:
                return "Ya existe otro compositor con ese nombre artístico.";
            case 2:
                return "El elemento que intentas editar ya no existe.";
            case 3:
                return "El compositor elegido no está registrado.";
            default:
                return "Se ha producido un error desconocido.";
        }
    }

    // ------------------------------------------------------------------ Compositores

    // Muestra el formulario vacio para añadir un compositor
    @GetMapping("/composer/add")
    public String showComposerForm(@RequestParam(defaultValue = "false") boolean fromPiece, Model model) {
        Composer composer = new Composer();
        if (fromPiece && composerService.getPendingPiece() != null) {
            composer.setName(composerService.getPendingPiece().getComposer());
        } else {
            composerService.setPendingPiece(null);
        }
        model.addAttribute("composer", composer);
        model.addAttribute("pendingPiece", composerService.getPendingPiece());
        model.addAttribute("mode", "add");
        return "composerFormView";
    }

    // Procesa el formulario de añadir compositor. Los errores los gestiona el @ControllerAdvice
    @PostMapping("/composer/add/submit")
    public String addComposer(@Valid @ModelAttribute("composer") Composer composer, BindingResult bindingResult, Model model) {
        model.addAttribute("mode", "add");
        model.addAttribute("pendingPiece", composerService.getPendingPiece());
        if (bindingResult.hasErrors()) {
            return "composerFormView";
        }
        // Si el nombre ya existe el servicio lanza DuplicateComposerException y la recoge el @ControllerAdvice
        composerService.addComposer(composer);
        MusicalPiece pending = composerService.getPendingPiece();
        if (pending != null) {
            // Ya existe el compositor, asi que se registra la obra que esperaba
            pending.setComposer(composer.getName());
            composerService.addPiece(pending);
            composerService.setPendingPiece(null);
        }
        return "redirect:/composer?name=" + encode(composer.getName());
    }

    // Muestra el formulario de edicion. Si llega un codigo de error, se enseña el mensaje arriba en rojo
    @GetMapping("/composer/edit/{name}")
    public String showComposerEditForm(@PathVariable String name, @RequestParam(required = false) Integer err, Model model) {
        Composer composer = composerService.getComposer(name);
        if (err != null) {
            model.addAttribute("txtErr", errorMessage(err));
        }
        model.addAttribute("composer", composer);
        model.addAttribute("mode", "edit");
        model.addAttribute("originalName", composer.getName());
        return "composerFormView";
    }

    // Procesa el formulario de edicion de un compositor. Si hay error, redirige al formulario con un codigo
    @PostMapping("/composer/edit/submit")
    public String editComposer(@Valid @ModelAttribute("composer") Composer composer, BindingResult bindingResult,
                               @RequestParam String originalName, @RequestParam(required = false) MultipartFile file, Model model) {
        model.addAttribute("mode", "edit");
        model.addAttribute("originalName", originalName);
        if (bindingResult.hasErrors()) {
            return "composerFormView";
        }
        try {
            composerService.editComposer(originalName, composer);
        } catch (DuplicateComposerException ex) {
            return "redirect:/composer/edit/" + encode(originalName) + "?err=1";
        } catch (ComposerNotFoundException ex) {
            return "redirect:/composer/edit/" + encode(originalName) + "?err=2";
        }
        // Imagen nueva opcional: si se ha elegido un fichero se guarda y se añade al compositor
        String newImage = null;
        if (file != null && !file.isEmpty()) {
            newImage = fileStorageService.store(file);
            composerService.addPhoto(composer.getName(), "/files/" + newImage);
        }
        subscriberService.renameComposer(originalName, composer.getName());
        // Se avisa a los suscriptores (con la imagen adjunta si hay una nueva)
        subscriberService.notifySubscribers(composer.getName(), newImage);
        return "redirect:/composer?name=" + encode(composer.getName());
    }

    // ------------------------------------------------------------------ Obras

    // Muestra el formulario vacio para añadir una obra
    @GetMapping("/piece/add")
    public String showPieceForm(Model model) {
        model.addAttribute("musicalPiece", new MusicalPiece());
        model.addAttribute("newComposer", false);
        model.addAttribute("newComposerName", "");
        model.addAttribute("mode", "add");
        return "pieceFormView";
    }

    // Procesa el formulario de añadir obra. Los errores los gestiona el @ControllerAdvice
    @PostMapping("/piece/add/submit")
    public String addPiece(@Valid @ModelAttribute("musicalPiece") MusicalPiece musicalPiece, BindingResult bindingResult,
                           @RequestParam(defaultValue = "false") boolean newComposer,
                           @RequestParam(defaultValue = "") String newComposerName, Model model) {
        model.addAttribute("mode", "add");
        model.addAttribute("newComposer", newComposer);
        model.addAttribute("newComposerName", newComposerName);
        if (newComposer) {
            // Con la casilla marcada el desplegable esta desactivado, asi que el unico error esperado es el del compositor
            boolean otherErrors = bindingResult.hasGlobalErrors()
                    || bindingResult.getFieldErrors().stream().anyMatch(e -> !e.getField().equals("composer"));
            if (otherErrors) {
                return "pieceFormView";
            }
            String name = newComposerName.trim();
            if (name.isEmpty()) {
                model.addAttribute("formError", "Escribe el nombre del compositor no registrado.");
                return "pieceFormView";
            }
            if (composerService.findComposerByName(name).isPresent()) {
                model.addAttribute("formError", "\"" + name + "\" ya está registrado. Desmarca la casilla y elígelo en la lista.");
                return "pieceFormView";
            }
            musicalPiece.setComposer(name);
            composerService.setPendingPiece(musicalPiece);
            return "redirect:/composer/add?fromPiece=true";
        }
        if (bindingResult.hasErrors()) {
            return "pieceFormView";
        }
        // Si el compositor no esta registrado el servicio lanza ComposerNotRegisteredException (la recoge el @ControllerAdvice)
        composerService.addPiece(musicalPiece);
        return "redirect:/composer?name=" + encode(musicalPiece.getComposer());
    }

    // Muestra el formulario de edicion de una obra. Si llega un codigo de error, se enseña el mensaje arriba en rojo
    @GetMapping("/piece/edit/{id}")
    public String showPieceEditForm(@PathVariable int id, @RequestParam(required = false) Integer err, Model model) {
        MusicalPiece piece = composerService.getPiece(id);
        if (err != null) {
            model.addAttribute("txtErr", errorMessage(err));
        }
        model.addAttribute("musicalPiece", piece);
        model.addAttribute("mode", "edit");
        return "pieceFormView";
    }

    // Procesa el formulario de edicion de una obra. Si hay error, redirige al formulario con un codigo
    @PostMapping("/piece/edit/submit")
    public String editPiece(@Valid @ModelAttribute("musicalPiece") MusicalPiece musicalPiece, BindingResult bindingResult, Model model) {
        model.addAttribute("mode", "edit");
        if (bindingResult.hasErrors()) {
            return "pieceFormView";
        }
        try {
            composerService.editPiece(musicalPiece);
        } catch (ComposerNotRegisteredException ex) {
            return "redirect:/piece/edit/" + musicalPiece.getId() + "?err=3";
        } catch (PieceNotFoundException ex) {
            return "redirect:/piece/edit/" + musicalPiece.getId() + "?err=2";
        }
        return "redirect:/composer?name=" + encode(musicalPiece.getComposer());
    }

    // ------------------------------------------------------------------ Borrado

    // Borra una obra. Se hace con POST (un enlace GET no deberia modificar datos)
    @PostMapping("/piece/delete/{id}")
    public String deletePiece(@PathVariable int id, @RequestParam String composer) {
        composerService.deletePiece(id);
        return "redirect:/composer?name=" + encode(composer);
    }

    // Borra un compositor y todas sus obras
    @PostMapping("/composer/delete/{name}")
    public String deleteComposer(@PathVariable String name) {
        Composer composer = composerService.getComposer(name);
        // Se borran tambien sus imagenes subidas y sus suscripciones
        composer.getPhotos().stream()
                .filter(p -> p.startsWith("/files/"))
                .forEach(p -> fileStorageService.delete(p.substring("/files/".length())));
        subscriberService.deleteByComposer(composer.getName());
        composerService.deleteComposer(name);
        return "redirect:/composers";
    }
}
