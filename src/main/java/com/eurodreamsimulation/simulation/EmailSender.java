package com.eurodreamsimulation.simulation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmailSender {

    private static final Properties CONFIG = new Properties();
    
    private static final Logger logger = LogManager.getLogger(NextDrawPredictor.class);

    // Bloc statique : Chargé une seule fois au démarrage de l'application
    static {
        try (InputStream input = EmailSender.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                logger.error("❌ ERREUR CRITIQUE : Fichier 'config.properties' introuvable dans src/main/resources/");
            } else {
                CONFIG.load(input);
            }
        } catch (IOException ex) {
            logger.error("❌ Erreur lors du chargement de la configuration : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void envoyer(String destinataire, String sujet, String contenu) {
        // Récupération des valeurs depuis le fichier
        String host = CONFIG.getProperty("mail.smtp.host");
        String port = CONFIG.getProperty("mail.smtp.port");
        String sender = CONFIG.getProperty("mail.sender");
        String password = CONFIG.getProperty("mail.password");

        if (sender == null || password == null) {
            logger.error("❌ ERREUR : Identifiants email manquants dans config.properties");
            return;
        }

        // 1. Configuration des propriétés SMTP
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.trust", host);

        // 2. Création de la session sécurisée
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sender, password);
            }
        });

        try {
            // 3. Création du message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject(sujet);
            message.setText(contenu);

            // 4. Envoi
            Transport.send(message);
            logger.info("✅ Email envoyé avec succès à " + destinataire);

        } catch (MessagingException e) {
            logger.error("❌ Erreur lors de l'envoi de l'email : " + e.getMessage());
            e.printStackTrace();
        }
    }
}