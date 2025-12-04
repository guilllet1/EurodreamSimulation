package com.eurodreamsimulation.simulation;

import com.eurodreamsimulation.data.CsvLoader;
import com.eurodreamsimulation.model.Tirage;
import com.eurodreamsimulation.strategy.IStrategie;
import com.eurodreamsimulation.strategy.StrategieAleatoire;
import com.eurodreamsimulation.strategy.StrategieAnalyse; // NOUVEAU
import com.eurodreamsimulation.strategy.StrategieFixe;

import java.util.Arrays;
import java.util.List;

public class App {
    
    private static final int NOMBRE_JOUEURS_ALEATOIRES = 1000;
    private static final int NOMBRE_JOUEURS_ANALYSE = 10; // Tester la nouvelle stratégie avec 10 joueurs
    
    public static void main(String[] args) {
        
        // 1. Charger le CSV en mémoire
        CsvLoader loader = new CsvLoader();
        List<Tirage> data = loader.chargerDepuisRessources("eurodreams_202311.csv");

        if (data.isEmpty()) {
            System.err.println("La simulation est annulée.");
            return;
        }

        // 2. Initialiser le simulateur
        Simulateur sim = new Simulateur(data);
        int joueurId = 1;

        // --- 3. DÉFINITION DES STRATÉGIES ---
        IStrategie strategieAleatoire = new StrategieAleatoire();
        
        // Création de la nouvelle stratégie d'analyse, qui a besoin de l'historique
        IStrategie strategieAnalyse = new StrategieAnalyse(data); 

        // --- 4. CONFIGURATION DES JOUEURS ---
        
        // A. Joueurs Aléatoires (Statistique de base)
        System.out.printf("Ajout de %d joueurs avec la stratégie aléatoire...%n", 
                          NOMBRE_JOUEURS_ALEATOIRES);
        for (int i = 0; i < NOMBRE_JOUEURS_ALEATOIRES; i++) {
            sim.ajouterJoueur(new Joueur(joueurId++, strategieAleatoire));
        }
        
        // B. Joueurs avec Stratégie d'Analyse (la nouvelle stratégie)
        System.out.printf("Ajout de %d joueurs avec la stratégie d'analyse des fréquences...%n", 
                          NOMBRE_JOUEURS_ANALYSE);
        for (int i = 0; i < NOMBRE_JOUEURS_ANALYSE; i++) {
            sim.ajouterJoueur(new Joueur(joueurId++, strategieAnalyse));
        }

        // C. Joueurs Fixes (pour la comparaison)
        sim.ajouterJoueur(new Joueur(joueurId++, new StrategieFixe(
            Arrays.asList(1, 2, 3, 4, 5, 6), 1
        )));
        sim.ajouterJoueur(new Joueur(joueurId++, new StrategieFixe(
            Arrays.asList(10, 15, 22, 33, 38, 40), 5
        )));

        // 5. Lancer la simulation
        System.out.printf("Total de joueurs dans la simulation : %d%n", joueurId - 1);
        sim.demarrer();
    }
}