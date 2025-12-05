package com.eurodreamsimulation.simulation;

import com.eurodreamsimulation.data.CsvLoader;
import com.eurodreamsimulation.model.Grille;
import com.eurodreamsimulation.model.Tirage;
import com.eurodreamsimulation.strategy.StrategieAleatoire;
import com.eurodreamsimulation.strategy.StrategieAnalyse;
import com.eurodreamsimulation.strategy.StrategieFixe;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class NextDrawPredictor {

    public static void main(String[] args) {
        // 1. Chargement
        CsvLoader loader = new CsvLoader();
        List<Tirage> historique = loader.chargerDepuisRessources("eurodreams_202311.csv");
        
        if (historique.isEmpty()) {
            System.err.println("Simulation annulée : CSV vide.");
            return;
        }

        // 2. Calcul de la date cible
        LocalDate derniereDateConnue = historique.stream()
                .map(t -> t.dateTirage)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

        LocalDate dateProchainTirage = derniereDateConnue.plusDays(1);
        while (dateProchainTirage.getDayOfWeek() != DayOfWeek.MONDAY && 
               dateProchainTirage.getDayOfWeek() != DayOfWeek.THURSDAY) {
            dateProchainTirage = dateProchainTirage.plusDays(1);
        }

        // 3. Préparation des stratégies
        Tirage tirageFutur = new Tirage();
        tirageFutur.dateTirage = dateProchainTirage; 

        StrategieFixe stratFixe = new StrategieFixe(Arrays.asList(2, 4, 8, 9, 12, 26), 1);
        StrategieAnalyse stratAnalyse = new StrategieAnalyse(historique);
        StrategieAleatoire stratAleatoire = new StrategieAleatoire();

        // 4. Génération des grilles
        Grille gFixe = stratFixe.genererGrille(tirageFutur);
        Grille gAnalyse = stratAnalyse.genererGrille(tirageFutur);
        Grille gAleatoire = stratAleatoire.genererGrille(tirageFutur);

        // 5. Construction du rapport (Pour Console + Email)
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
        String dateStr = dateProchainTirage.format(dtf);
        dateStr = dateStr.substring(0, 1).toUpperCase() + dateStr.substring(1);

        StringBuilder rapport = new StringBuilder();
        rapport.append("==========================================================\n");
        rapport.append("   PRÉDICTIONS EURODREAMS : ").append(dateStr.toUpperCase()).append("\n");
        rapport.append("==========================================================\n\n");
        
        ajouterAuRapport(rapport, "1. OPTION FIXE (Vos numéros)", gFixe, "Toujours la même combinaison");
        ajouterAuRapport(rapport, "2. OPTION ANALYSE", gAnalyse, "Basé sur les paires fréquentes");
        ajouterAuRapport(rapport, "3. OPTION ALÉATOIRE", gAleatoire, "Hasard total");
        
        rapport.append("==========================================================\n");

        // 6. Affichage Console
        System.out.println(rapport.toString());

        // 7. Envoi par Email
        // Remplacez par l'email qui doit RECEVOIR le pronostic
        String emailDestinataire = "g.berthier@gmail.com"; 
        
        System.out.println("Tentative d'envoi de l'email...");
        EmailSender.envoyer(
            emailDestinataire, 
            "Pronostics EuroDreams - " + dateStr, 
            rapport.toString()
        );
    }

    private static void ajouterAuRapport(StringBuilder sb, String titre, Grille grille, String desc) {
        sb.append(titre).append("\n");
        sb.append("   Numéros : ").append(grille.getNumeros()).append("\n");
        sb.append("   Dream   : ").append(grille.getNumeroDream()).append("\n");
        sb.append("   (").append(desc).append(")\n");
        sb.append("----------------------------------------------------------\n");
    }
}