package com.example.composers.services;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

// Servicio que envia los emails usando Gmail como reenviador
@Service
public class EmailService {

    // JavaMailSender (Spring) es la interfaz que envia los emails. Spring Boot la crea sola con los datos
    // spring.mail.* de application.properties, y aqui se inyecta
    @Autowired
    private JavaMailSender sender;

    // Envia un email en HTML. Si attachment no es null, adjunta ese fichero.
    // Con @Async se ejecuta en otro hilo: quien lo llama no espera a que se envie (por eso devuelve void)
    @Async
    public void sendEmail(String destination, String subject, String textMessage, String attachment) {
        try {
            // MimeMessage (Jakarta Mail) es el mensaje de correo en formato MIME, que puede tener varias partes
            // (texto, HTML, adjuntos). createMimeMessage crea uno vacio
            MimeMessage message = sender.createMimeMessage();
            // MimeMessageHelper (Spring) ayuda a rellenar el MimeMessage sin tocar las partes a mano.
            // El true indica que el mensaje es multipart, necesario para poder añadir adjuntos
            MimeMessageHelper helper = new MimeMessageHelper(message, attachment != null);
            helper.setTo(destination);
            // El segundo parametro true indica que el texto es HTML
            helper.setText(textMessage, true);
            helper.setSubject(subject);
            if (attachment != null) {
                File adjunto = new File(attachment);
                helper.addAttachment(adjunto.getName(), adjunto);
            }
            // send envia el mensaje al servidor de correo (smtp.gmail.com)
            sender.send(message);
        } catch (MessagingException e) {
            // MessagingException (Jakarta Mail) es la excepcion base de los errores al crear o enviar mensajes
            e.printStackTrace();
        }
    }
}
