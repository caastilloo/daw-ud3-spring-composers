package com.example.composers.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.composers.exceptions.CustomException;
import com.example.composers.models.Composer;
import com.example.composers.models.Instrumentation;
import com.example.composers.models.MusicalPiece;
import com.example.composers.services.ComposerService;

// Controlador de la version con @RequestParam: todos los datos llegan en la query string (?clave=valor)
@Controller
public class ComposerController {

    @Autowired
    private ComposerService composerService;

    // Muestra un compositor
    @GetMapping("/composer")
    public String showComposer(@RequestParam String name, @RequestParam(defaultValue = "false") boolean subscribed, Model model) {
        model.addAttribute("subscribed", subscribed);
        // Si no existe, el servicio lanza ComposerNotFoundException y la recoge el @ControllerAdvice
        Composer composer = composerService.getComposer(name);
        model.addAttribute("composer", composer);
        // Obras ordenadas por titulo y agrupadas por instrumentacion (la vista recorre el enumerado y consulta el mapa)
        List<MusicalPiece> sortedPieces = new ArrayList<>(composer.getPieces());
        Collections.sort(sortedPieces, (p1, p2) -> p1.getTitle().compareToIgnoreCase(p2.getTitle()));
        Map<Instrumentation, List<MusicalPiece>> grouped = sortedPieces.stream()
                .collect(Collectors.groupingBy(MusicalPiece::getInstrumentation));
        model.addAttribute("piecesByInstrumentation", grouped);
        return "composerView";
    }

    // Lista compositores filtrando por nombre (texto o expresion regular), nacionalidad y las casillas marcadas
    @GetMapping("/composers")
    public String listComposers(@RequestParam(required = false, defaultValue = "") String name,
                                @RequestParam(required = false, defaultValue = "") String nationality,
                                @RequestParam(defaultValue = "false") boolean alive,
                                @RequestParam(defaultValue = "false") boolean manyPieces,
                                Model model) {
        model.addAttribute("name", name);
        model.addAttribute("nationality", nationality);
        model.addAttribute("alive", alive);
        model.addAttribute("manyPieces", manyPieces);
        model.addAttribute("composers", composerService.search(name, nationality, alive, manyPieces));
        return "listComposersView";
    }

    // Busca obras por titulo (texto parcial o expresion regular)
    @GetMapping("/searchMusicalPiece")
    public String searchMusicalPiece(@RequestParam(required = false) String title, Model model) {
        model.addAttribute("title", title == null ? "" : title);
        if (title != null && !title.isBlank()) {
            model.addAttribute("pieces", composerService.searchPieces(title));
        }
        return "searchMusicalPieceView";
    }

    // Muestra todas las obras de todos los compositores (compositor - obra - enlace)
    @GetMapping("/allMusicalPieces")
    public String allMusicalPieces(Model model) {
        model.addAttribute("pieces", composerService.getAllPieces());
        return "allMusicalPiecesView";
    }

    // Muestra los dos desplegables para comparar compositores
    @GetMapping("/composerCompare")
    public String showCompare(Model model) {
        model.addAttribute("composers", composerService.getComposersSortedByName());
        return "composerCompareView";
    }

    // Indica cual de los dos compositores es mayor
    @GetMapping("/whoIsOlder")
    public String whoIsOlder(@RequestParam String first, @RequestParam String second, Model model) {
        Optional<Composer> a = composerService.findComposerByName(first);
        Optional<Composer> b = composerService.findComposerByName(second);
        if (a.isEmpty() || b.isEmpty()) {
            return "redirect:/composerCompare";
        }
        Composer c1 = a.get();
        Composer c2 = b.get();
        model.addAttribute("first", c1);
        model.addAttribute("second", c2);
        if (c1.getName().equals(c2.getName())) {
            model.addAttribute("sameComposer", true);
        } else if (c1.getBirthDate().equals(c2.getBirthDate())) {
            model.addAttribute("sameBirthDate", true);
        } else {
            boolean firstIsOlder = c1.getBirthDate().isBefore(c2.getBirthDate());
            model.addAttribute("older", firstIsOlder ? c1 : c2);
            model.addAttribute("younger", firstIsOlder ? c2 : c1);
        }
        return "whoIsOlderView";
    }

    // ------------------------------------------------------------------ Pruebas de errores

    // Lanza una RuntimeException para probar el handler de excepciones comunes
    @GetMapping("/errorThrower")
    public String errorThrowerController(@RequestParam(required = false) String param) {
        if (true) throw new RuntimeException("I'm throwing an error");
        return "indexView";
    }

    // Lanza la excepcion checked CustomException (por eso lleva throws)
    @GetMapping("/customErrorThrower")
    public String customErrorThrowerController(@RequestParam(required = false) String param) throws CustomException {
        if (true) throw new CustomException("I'm throwing a custom error");
        return "indexView";
    }
}
