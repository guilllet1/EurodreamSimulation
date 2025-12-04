package com.eurodreamsimulation.simulation;

import com.eurodreamsimulation.data.CsvLoader;
import com.eurodreamsimulation.model.Grille;
import com.eurodreamsimulation.model.Tirage;
import com.eurodreamsimulation.strategy.StrategieAnalyse;
import java.util.Collections;

import java.util.List;

/**
 * Classe exécutable pour charger l'historique depuis resources/eurodreams_202311.csv,
 * instancier la stratégie d'analyse et afficher la grille recommandée pour le prochain tirage.
 *
 * Usage (depuis la racine du projet Maven):
 *   mvn -q compile exec:java -Dexec.mainClass="com.eurodreamsimulation.simulation.NextDrawPredictor"
 *
 * Ou compiler et exécuter avec javac/java si vous préférez.
 */
public class NextDrawPredictor {

    public static void main(String[] args) {
        CsvLoader loader = new CsvLoader();
        List<Tirage> historique = loader.chargerDepuisRessources("eurodreams_202311.csv");
        // AJOUT : inverse la liste pour avoir le tirage le plus récent en premier
        Collections.reverse(historique);
        if (historique.isEmpty()) {
            System.err.println("Aucun tirage chargé. Vérifiez que le fichier src/main/resources/eurodreams_202311.csv est présent et lisible.");
            return;
        }

        // La stratégie existante prend l'historique en paramètre
        StrategieAnalyse strategie = new StrategieAnalyse(historique);
        Grille grille = strategie.genererGrille();

        System.out.println("=== Grille recommandée pour le prochain tirage ===");
        System.out.printf("Numéros : %s\n", grille.getNumeros());
        System.out.printf("Dream : %d\n", grille.getNumeroDream());
        System.out.println("(Stratégie utilisée : " + strategie.getNom() + ")");
    }
}
