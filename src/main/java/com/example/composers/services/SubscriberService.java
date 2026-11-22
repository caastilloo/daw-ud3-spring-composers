package com.example.composers.services;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.composers.exceptions.DuplicateSubscriberException;
import com.example.composers.exceptions.SubscriberNotFoundException;
import com.example.composers.models.Subscriber;

// Servicio de las suscripciones a los compositores (fichero subscribers.csv)
@Service
public class SubscriberService {

    @Value("${app.data-dir}/subscribers.csv")
    private String SUBSCRIBERS_CSV;
    // Direccion de la aplicacion, para el enlace de anular la suscripcion (en la nube no es localhost)
    @Value("${app.url}")
    private String APP_URL;

    @Autowired
    @Qualifier("csv")
    private CrudOperator crudOperator;

    @Autowired
    private EmailService emailService;

    @Autowired
    private FileStorageService fileStorageService;

    // Lee todos los suscriptores del .csv
    private List<Subscriber> getSubscribers() {
        return Arrays.stream(crudOperator.read(SUBSCRIBERS_CSV))
                .map(line -> new Subscriber(line.split(";", -1)))
                .toList();
    }

    // Suscriptores de un compositor
    public List<Subscriber> getSubscribers(String composer) {
        return getSubscribers().stream().filter(s -> s.getComposer().equals(composer)).toList();
    }

    // Busca una suscripcion por su id
    public Subscriber getSubscriber(String id) {
        return getSubscribers().stream().filter(s -> s.getId().equals(id)).findFirst()
                .orElseThrow(() -> new SubscriberNotFoundException(id));
    }

    // Guarda una suscripcion nueva. El mismo email no puede estar dos veces en el mismo compositor
    public void subscribe(Subscriber subscriber) {
        boolean repeated = getSubscribers(subscriber.getComposer()).stream()
                .anyMatch(s -> s.getEmail().equals(subscriber.getEmail()));
        if (repeated) {
            throw new DuplicateSubscriberException(subscriber.getEmail(), subscriber.getComposer());
        }
        subscriber.setId(String.valueOf(System.currentTimeMillis()));
        crudOperator.create(subscriber.toCsv(), SUBSCRIBERS_CSV);
    }

    // Borra una suscripcion (su linea del .csv)
    public void unsubscribe(String id) {
        getSubscriber(id);
        crudOperator.delete(id, SUBSCRIBERS_CSV);
    }

    // Si el compositor cambia de nombre, sus suscripciones pasan al nombre nuevo
    public void renameComposer(String oldName, String newName) {
        for (Subscriber s : getSubscribers(oldName)) {
            s.setComposer(newName);
            crudOperator.update(s.getId(), s.toCsv(), SUBSCRIBERS_CSV);
        }
    }

    // Borra las suscripciones de un compositor eliminado
    public void deleteByComposer(String composer) {
        getSubscribers(composer).forEach(s -> crudOperator.delete(s.getId(), SUBSCRIBERS_CSV));
    }

    // Avisa a los suscriptores de que se ha modificado el perfil del compositor.
    // Si se ha añadido una imagen nueva (newImage no es null) va adjunta en el email
    public void notifySubscribers(String composer, String newImage) {
        String attachment = newImage == null ? null : fileStorageService.getFilePath(newImage);
        for (Subscriber s : getSubscribers(composer)) {
            String body = "<h2>Hola, " + s.getUsername() + "</h2>"
                    + "<p>Se ha modificado el perfil de <strong>" + composer + "</strong>.</p>"
                    + (newImage != null ? "<p>Además se ha añadido una imagen nueva, la tienes adjunta en este correo.</p>" : "")
                    + "<p><a href=\"" + APP_URL + "/composer?name=" + composer.replace(" ", "%20") + "\">Ver el compositor</a></p>"
                    + "<p style=\"font-size:12px;color:#666\">Si no quieres recibir más avisos, "
                    + "<a href=\"" + APP_URL + "/unsubscribe/" + s.getId() + "\">anula tu suscripción</a>.</p>";
            // Es asincrono: el bucle no espera a que se envie cada email
            emailService.sendEmail(s.getEmail(), "Cambios en " + composer + " - My Favourite Composers", body, attachment);
        }
    }
}
