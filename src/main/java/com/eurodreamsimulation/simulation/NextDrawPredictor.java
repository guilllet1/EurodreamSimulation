package com.eurodreamsimulation.simulation;

import com.eurodreamsimulation.data.CsvLoader;
import com.eurodreamsimulation.model.Grille;
import com.eurodreamsimulation.model.Tirage;
import com.eurodreamsimulation.strategy.StrategieAleatoire;
import com.eurodreamsimulation.strategy.StrategieAnalyse;
import com.eurodreamsimulation.strategy.StrategieFixe;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class NextDrawPredictor {

    private static final Logger logger = LogManager.getLogger(NextDrawPredictor.class);
    private static String emailDestinataire; 

    public static void main(String[] args) {
        
        // 1. Configuration de l'affichage (Accents et Euro €)
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        logger.info("=== Lancement du Prédictor EuroDreams ===");

        // 0. CHARGEMENT DE LA CONFIGURATION
        if (!chargerConfiguration()) {
            logger.error("Arrêt du programme : Configuration email manquante.");
            return;
        }

        // 1. Chargement des données
        CsvLoader loader = new CsvLoader();
        String urlFdj = "https://www.sto.api.fdj.fr/anonymous/service-draw-info/v3/documentations/1a2b3c4d-9876-4562-b3fc-2c963f66afa5";
        
        List<Tirage> historique = loader.chargerDepuisURL(urlFdj);
        
        if (historique.isEmpty()) {
            logger.warn("Téléchargement échoué ou vide. Tentative de chargement local...");
            historique = loader.chargerDepuisRessources("eurodreams_202311.csv");
        }

        if (historique.isEmpty()) {
            logger.error("Aucune donnée disponible (ni URL, ni locale). Arrêt.");
            return;
        }

        // 2. Identification du dernier tirage RÉEL
        Tirage dernierTirageReel = historique.get(0);
        logger.info("Dernier tirage connu : {} (Dream {})", dernierTirageReel.dateTirage, dernierTirageReel.numeroDream);

        // 3. Calcul de la date du PROCHAIN tirage
        LocalDate dateProchainTirage = calculerProchaineDate(dernierTirageReel.dateTirage);
        logger.info("Date du prochain tirage estimée : {}", dateProchainTirage);

        // 4. Préparation des stratégies
        Tirage tirageFutur = new Tirage();
        tirageFutur.dateTirage = dateProchainTirage;

        // A. Stratégie Fixe
        StrategieFixe stratFixe = new StrategieFixe(Arrays.asList(2, 4, 8, 9, 12, 26), 1);
        // B. Stratégie Analyse
        StrategieAnalyse stratAnalyse = new StrategieAnalyse(historique);
        // C. Stratégie Aléatoire
        StrategieAleatoire stratAleatoire = new StrategieAleatoire();

        // 5. Génération des grilles
        Grille gFixe = stratFixe.genererGrille(tirageFutur);
        Grille gAnalyse = stratAnalyse.genererGrille(tirageFutur);
        Grille gAleatoire = stratAleatoire.genererGrille(tirageFutur);

        // 6. Appel à Gemini pour l'analyse
        logger.info("Interrogation de Gemini pour analyser le tirage du {}...", dernierTirageReel.dateTirage);
        
        // --- CORRECTION ICI : La variable est maintenant utilisée correctement ---
        String descriptionDernierTirage = String.format("Tirage du %s : %s - Dream %d", 
                dernierTirageReel.dateTirage, 
                dernierTirageReel.boules, 
                dernierTirageReel.numeroDream);
        
        String analyseIA = GeminiClient.analyserTirage(descriptionDernierTirage);
        logger.info("Réponse Gemini reçue.");

        // 7. Construction du Rapport
        // On passe bien 'descriptionDernierTirage'
        String rapport = construireRapport(dateProchainTirage, dernierTirageReel, descriptionDernierTirage, analyseIA, gFixe, gAnalyse, gAleatoire);

        // 8. Affichage et Envoi
        System.out.println(rapport); 

        logger.info("Envoi de l'email à {}...", emailDestinataire);
        EmailSender.envoyer(
            emailDestinataire, 
            "Pronostics EuroDreams - " + dateProchainTirage, 
            rapport
        );
        
        logger.info("=== Fin du programme ===");
    }

    // --- Méthodes Utilitaires ---

    private static boolean chargerConfiguration() {
        try (InputStream input = NextDrawPredictor.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                logger.error("Fichier 'config.properties' introuvable dans src/main/resources/");
                return false;
            }

            Properties prop = new Properties();
            prop.load(input);

            String recipient = prop.getProperty("mail.recipient");
            if (recipient == null || recipient.isEmpty()) {
                recipient = prop.getProperty("mail.sender");
            }

            if (recipient != null && !recipient.isEmpty()) {
                emailDestinataire = recipient;
                logger.info("Email destinataire chargé : {}", emailDestinataire);
                return true;
            } else {
                logger.error("Aucun email destinataire trouvé dans config.properties.");
                return false;
            }

        } catch (IOException e) {
            logger.error("Erreur lors du chargement de la configuration : {}", e.getMessage());
            return false;
        }
    }

    private static LocalDate calculerProchaineDate(LocalDate derniereDate) {
        LocalDate date = derniereDate.plusDays(1);
        while (date.getDayOfWeek() != DayOfWeek.MONDAY && 
               date.getDayOfWeek() != DayOfWeek.THURSDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static String construireRapport(LocalDate dateProchaine, Tirage dernierTirage, String descDernier, String analyseIA, Grille gFixe, Grille gAnalyse, Grille gAleatoire) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
        String dateStr = dateProchaine.format(dtf);
        dateStr = dateStr.substring(0, 1).toUpperCase() + dateStr.substring(1);

        StringBuilder sb = new StringBuilder();
        sb.append("==========================================================\n");
        sb.append("   PRÉDICTIONS EURODREAMS : ").append(dateStr.toUpperCase()).append("\n");
        sb.append("==========================================================\n\n");

        sb.append("📊 DERNIER RÉSULTAT CONNU\n");
        sb.append("   ").append(descDernier).append("\n\n");
        
        sb.append("🧠 L'AVIS DE L'IA (GEMINI)\n");
        sb.append("   ").append(analyseIA).append("\n");
        sb.append("----------------------------------------------------------\n\n");

        sb.append("🎯 VOS OPTIONS DE JEU\n\n");
        
        ajouterOption(sb, "1. OPTION FIXE (Vos numéros)", gFixe, "Toujours la même combinaison");
        ajouterOption(sb, "2. OPTION ANALYSE (Statistique)", gAnalyse, "Basé sur les paires fréquentes historiques");
        ajouterOption(sb, "3. OPTION ALÉATOIRE (Chance)", gAleatoire, "Génération purement hasardeuse");

        sb.append("==========================================================\n");
        return sb.toString();
    }

    private static void ajouterOption(StringBuilder sb, String titre, Grille grille, String desc) {
        sb.append(titre).append("\n");
        sb.append("   Numéros : ").append(grille.getNumeros()).append("\n");
        sb.append("   Dream   : ").append(grille.getNumeroDream()).append("\n");
        sb.append("   Info    : ").append(desc).append("\n");
        sb.append("----------------------------------------------------------\n");
    }
}