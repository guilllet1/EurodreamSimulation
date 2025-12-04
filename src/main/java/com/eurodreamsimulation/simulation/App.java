package com.eurodreamsimulation.simulation;

import com.eurodreamsimulation.data.CsvLoader;
import com.eurodreamsimulation.model.Tirage;
import com.eurodreamsimulation.strategy.IStrategie;
import com.eurodreamsimulation.strategy.StrategieAleatoire;
import com.eurodreamsimulation.strategy.StrategieAnalyse;
import com.eurodreamsimulation.strategy.StrategieFixe;
import com.eurodreamsimulation.strategy.StrategieMoinsFrequents; // <--- NOUVEL IMPORT

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class App {
    
    private static final int NOMBRE_JOUEURS_ALEATOIRES = 1000;
    private static final int NOMBRE_JOUEURS_ANALYSE = 1;
    private static final int NOMBRE_JOUEURS_MOINS_FREQUENTS = 1; // <--- NOUVEAU GROUPE
    
    public static void main(String[] args) {
        
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        } catch (Exception e) { e.printStackTrace(); }

        CsvLoader loader = new CsvLoader();
        List<Tirage> data = loader.chargerDepuisRessources("eurodreams_202311.csv");

        if (data.isEmpty()) {
            System.err.println("La simulation est annulée.");
            return;
        }

        // Simulation chronologique
        Collections.reverse(data); 

        Simulateur sim = new Simulateur(data);
        int joueurId = 1;

        // --- DÉFINITION DES STRATÉGIES ---
        IStrategie strategieAleatoire = new StrategieAleatoire();
        IStrategie strategieAnalyse = new StrategieAnalyse(data);
        // Nouvelle stratégie
        IStrategie strategieMoinsFrequents = new StrategieMoinsFrequents(data);

        // --- CONFIGURATION DES JOUEURS ---
        
        // 1. Joueurs Aléatoires
        System.out.printf("Ajout de %d joueurs Aléatoires...%n", NOMBRE_JOUEURS_ALEATOIRES);
        for (int i = 0; i < NOMBRE_JOUEURS_ALEATOIRES; i++) {
            sim.ajouterJoueur(new Joueur(joueurId++, strategieAleatoire));
        }
        
        // 2. Joueurs "Analyse Fréquences" (Les plus sortis)
        System.out.printf("Ajout de %d joueurs Analyse (Les plus fréquents)...%n", NOMBRE_JOUEURS_ANALYSE);
        for (int i = 0; i < NOMBRE_JOUEURS_ANALYSE; i++) {
            sim.ajouterJoueur(new Joueur(joueurId++, strategieAnalyse));
        }

        // 3. Joueurs "Moins Fréquents" (Les oubliés) <--- AJOUT ICI
        System.out.printf("Ajout de %d joueurs Stratégie Inverse (Les moins fréquents)...%n", NOMBRE_JOUEURS_MOINS_FREQUENTS);
        for (int i = 0; i < NOMBRE_JOUEURS_MOINS_FREQUENTS; i++) {
            sim.ajouterJoueur(new Joueur(joueurId++, strategieMoinsFrequents));
        }

        // 4. Joueurs Fixes
        sim.ajouterJoueur(new Joueur(joueurId++, new StrategieFixe(Arrays.asList(1, 2, 3, 4, 5, 6), 1)));
        sim.ajouterJoueur(new Joueur(joueurId++, new StrategieFixe(Arrays.asList(10, 15, 22, 33, 38, 40), 5)));

        System.out.printf("Total de joueurs : %d%n", joueurId - 1);
        sim.demarrer();
    }
}