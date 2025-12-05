package com.eurodreamsimulation.simulation;

import com.eurodreamsimulation.data.CsvLoader;
import com.eurodreamsimulation.model.Grille;
import com.eurodreamsimulation.model.Tirage;
import com.eurodreamsimulation.strategy.StrategieAleatoire;
import com.eurodreamsimulation.strategy.StrategieAnalyse;
import com.eurodreamsimulation.strategy.StrategieFixe;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class NextDrawPredictor {

    public static void main(String[] args) {
        
        // 1. Configuration de l'affichage (Accents et Euro €)
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 1. Chargement (URL FDJ ou Fichier local)
        CsvLoader loader = new CsvLoader();
        // Utilisation de l'URL FDJ recommandée pour avoir les dernières données
        List<Tirage> historique = loader.chargerDepuisURL(
            "https://www.sto.api.fdj.fr/anonymous/service-draw-info/v3/documentations/1a2b3c4d-9876-4562-b3fc-2c963f66afa5"
        );
        
        if (historique.isEmpty()) {
            // Fallback sur le fichier local si l'URL échoue
            System.out.println("Téléchargement échoué, tentative locale...");
            historique = loader.chargerDepuisRessources("eurodreams_202311.csv");
        }

        if (historique.isEmpty()) return;

        // 2. Récupération du DERNIER tirage réel connu (le plus récent est en position 0)
        Tirage dernierTirageReel = historique.get(0);
        
        // 3. Calcul de la date future
        LocalDate derniereDateConnue = dernierTirageReel.dateTirage;
        LocalDate dateProchainTirage = derniereDateConnue.plusDays(1);
        while (dateProchainTirage.getDayOfWeek() != DayOfWeek.MONDAY && 
               dateProchainTirage.getDayOfWeek() != DayOfWeek.THURSDAY) {
            dateProchainTirage = dateProchainTirage.plusDays(1);
        }

        // 4. Stratégies
        Tirage tirageFutur = new Tirage();
        tirageFutur.dateTirage = dateProchainTirage; 

        Grille gFixe = new StrategieFixe(Arrays.asList(2, 4, 8, 9, 12, 26), 1).genererGrille(tirageFutur);
        Grille gAnalyse = new StrategieAnalyse(historique).genererGrille(tirageFutur);
        Grille gAleatoire = new StrategieAleatoire().genererGrille(tirageFutur);

        // 5. --- APPEL GEMINI (NOUVEAU) ---
        System.out.println("Demande d'analyse à Gemini...");
        String descriptionDernierTirage = String.format("Tirage du %s : %s - Dream %d", 
                dernierTirageReel.dateTirage, 
                dernierTirageReel.boules, 
                dernierTirageReel.numeroDream);
        
        String analyseIA = GeminiClient.analyserTirage(descriptionDernierTirage);
        // ---------------------------------

        // 6. Construction du rapport
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
        String dateStr = dateProchainTirage.format(dtf);
        dateStr = dateStr.substring(0, 1).toUpperCase() + dateStr.substring(1);

        StringBuilder rapport = new StringBuilder();
        rapport.append("==========================================================\n");
        rapport.append("   PRÉDICTIONS EURODREAMS : ").append(dateStr.toUpperCase()).append("\n");
        rapport.append("==========================================================\n\n");
        
        // Ajout de l'analyse IA en premier
        rapport.append("🧠 ANALYSE IA DU PRÉCÉDENT TIRAGE (").append(dernierTirageReel.dateTirage).append(")\n");
        rapport.append("   ").append(descriptionDernierTirage).append("\n");
        rapport.append("   >> ").append(analyseIA).append("\n");
        rapport.append("----------------------------------------------------------\n\n");

        ajouterAuRapport(rapport, "1. OPTION FIXE", gFixe, "Toujours la même");
        ajouterAuRapport(rapport, "2. OPTION ANALYSE", gAnalyse, "Paires fréquentes");
        ajouterAuRapport(rapport, "3. OPTION ALÉATOIRE", gAleatoire, "Hasard total");
        
        rapport.append("==========================================================\n");

        // 7. Envoi
        System.out.println(rapport.toString());
        EmailSender.envoyer("g.berthier@gmail.com", "Pronostics EuroDreams + IA - " + dateStr, rapport.toString());
    }

    private static void ajouterAuRapport(StringBuilder sb, String titre, Grille grille, String desc) {
        sb.append(titre).append("\n");
        sb.append("   Numéros : ").append(grille.getNumeros()).append("\n");
        sb.append("   Dream   : ").append(grille.getNumeroDream()).append("\n");
        sb.append("   (").append(desc).append(")\n");
        sb.append("----------------------------------------------------------\n");
    }
}