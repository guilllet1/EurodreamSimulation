package com.eurodreamsimulation.simulation;

import com.eurodreamsimulation.data.CsvLoader;
import com.eurodreamsimulation.model.Grille;
import com.eurodreamsimulation.model.Tirage;
import com.eurodreamsimulation.strategy.StrategieAnalyse;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Classe exécutable pour charger l'historique et prédire le prochain tirage.
 */
public class NextDrawPredictor {

    public static void main(String[] args) {
        
         // --- CORRECTIF ENCODAGE (Affichage des € et accents) ---
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        CsvLoader loader = new CsvLoader();
        List<Tirage> historique = loader.chargerDepuisRessources("eurodreams_202311.csv");
        
        // Si la liste est vide, on arrête
        if (historique.isEmpty()) {
            System.err.println("Aucun tirage chargé. Vérifiez le fichier CSV.");
            return;
        }

        // Pour être sûr d'avoir l'ordre ou les données souhaitées
        // (L'utilisateur inversait la liste dans le code original)
        Collections.reverse(historique);

        // 1. Déterminer une date future pour la prédiction
        // On cherche la date la plus récente de l'historique et on ajoute 1 jour
        LocalDate derniereDateConnue = historique.stream()
                .map(t -> t.dateTirage)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());
        
        Tirage tirageFutur = new Tirage();
        tirageFutur.dateTirage = derniereDateConnue.plusDays(1); // Date "demain" par rapport aux données

        // 2. Initialiser la stratégie
        StrategieAnalyse strategie = new StrategieAnalyse(historique);

        // 3. Générer la grille en passant le tirage futur
        // La stratégie va utiliser tout l'historique car date < tirageFutur.date
        Grille grille = strategie.genererGrille(tirageFutur);

        System.out.println("=== Grille recommandée pour le prochain tirage ===");
        System.out.printf("Numéros : %s\n", grille.getNumeros());
        System.out.printf("Dream : %d\n", grille.getNumeroDream());
        System.out.println("(Stratégie utilisée : " + strategie.getNom() + ")");
    }
}