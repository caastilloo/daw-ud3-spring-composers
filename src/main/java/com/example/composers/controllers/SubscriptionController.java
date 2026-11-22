package com.example.composers.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.composers.models.Composer;
import com.example.composers.models.Subscriber;
import com.example.composers.services.ComposerService;
import com.example.composers.services.SubscriberService;

import jakarta.validation.Valid;

// Controlador de las suscripciones a los compositores
@Controller
public class SubscriptionController {

    @Autowired
    private ComposerService composerService;

    @Autowired
    private SubscriberService subscriberService;

    // Formulario para suscribirse a un compositor
    @GetMapping("/composer/subscribe/{name}")
    public String showSubscribeForm(@PathVariable String name, Model model) {
        Composer composer = composerService.getComposer(name);
        Subscriber subscriber = new Subscriber();
        subscriber.setComposer(composer.getName());
        model.addAttribute("subscriber", subscriber);
        return "suscribirseAlCompositorView";
    }

    // Guarda la suscripcion y vuelve a la ficha del compositor
    @PostMapping("/composer/subscribe/submit")
    public String subscribe(@Valid @ModelAttribute("subscriber") Subscriber subscriber, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "suscribirseAlCompositorView";
        }
        Composer composer = composerService.getComposer(subscriber.getComposer());
        subscriber.setComposer(composer.getName());
        subscriberService.subscribe(subscriber);
        return "redirect:/composer?name=" + URLEncoder.encode(composer.getName(), StandardCharsets.UTF_8) + "&subscribed=true";
    }

    // Vista a la que lleva el enlace del email: enseña la suscripcion y el boton para anularla
    @GetMapping("/unsubscribe/{id}")
    public String showUnsubscribe(@PathVariable String id, Model model) {
        model.addAttribute("subscriber", subscriberService.getSubscriber(id));
        return "anularSuscripcionAlCompositorView";
    }

    // Al pulsar el boton se borra la linea del .csv
    @PostMapping("/unsubscribe/{id}")
    public String unsubscribe(@PathVariable String id, Model model) {
        Subscriber subscriber = subscriberService.getSubscriber(id);
        subscriberService.unsubscribe(id);
        model.addAttribute("subscriber", subscriber);
        model.addAttribute("done", true);
        return "anularSuscripcionAlCompositorView";
    }
}
