package com.eurodreamsimulation.simulation;

import com.eurodreamsimulation.data.CsvLoader;
import com.eurodreamsimulation.model.Tirage;
import com.eurodreamsimulation.strategy.*; // Importe toutes les stratégies du dossier

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class App {
    
    private static final Logger logger = LogManager.getLogger(NextDrawPredictor.class);
    
    // Nombre de joueurs pour avoir des statistiques fiables
    private static final int NB_JOUEURS_ALEATOIRES = 1000;
    private static final int NB_JOUEURS_PAR_STRATEGIE = 1; 
    
    public static void main(String[] args) {
                
        // 1. Configuration de l'affichage (Accents et Euro €)
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. Chargement des données
        CsvLoader loader = new CsvLoader();
        // Par :
        List<Tirage> data = loader.chargerDepuisURL(
                "https://www.sto.api.fdj.fr/anonymous/service-draw-info/v3/documentations/1a2b3c4d-9876-4562-b3fc-2c963f66afa5"
        );

        if (data.isEmpty()) {
            logger.error("La simulation est annulée : fichier CSV introuvable ou vide.");
            return;
        }

        // 3. Inversion Chronologique (Indispensable pour les stratégies dynamiques)
        // On passe de [2025 -> 2023] à [2023 -> 2025]
        Collections.reverse(data);

        // 4. Initialisation du Simulateur
        Simulateur sim = new Simulateur(data);
        int joueurId = 1;

        // --- INSTANCIATION DES STRATÉGIES ---
        // Elles reçoivent 'data' pour pouvoir analyser le passé à chaque tour
        
        IStrategie stratAleatoire = new StrategieAleatoire();
        IStrategie stratPaires = new StrategieAnalyse(data);         // Votre stratégie "Intelligente"
        IStrategie stratPlusFreq = new StrategiePlusFrequents(data); // Joue les "Stars"
        IStrategie stratMoinsFreq = new StrategieMoinsFrequents(data); // Joue les "Oubliés"

        // --- CONFIGURATION DES ÉQUIPES ---

        // Groupe A : Le Hasard (1000 joueurs pour lisser la chance)
        logger.info("Création du groupe Aléatoire (%d joueurs)...%n", NB_JOUEURS_ALEATOIRES);
        for (int i = 0; i < NB_JOUEURS_ALEATOIRES; i++) {
            sim.ajouterJoueur(new Joueur(joueurId++, stratAleatoire));
        }

        // Groupe F : Somme Probable (1 écart-type)
        logger.info("Création du groupe Somme Probable (1 sigma) (%d joueurs)...%n", NB_JOUEURS_ALEATOIRES);
        IStrategie stratSomme = new StrategieSommeProbable(1.5); // 1.0 = 1 écart-type autour de la moyenne
        for (int i = 0; i < NB_JOUEURS_ALEATOIRES; i++) {
            sim.ajouterJoueur(new Joueur(joueurId++, stratSomme));
        }

        // Groupe B : Analyse des Paires (StrategieAnalyse)
        logger.info("Création du groupe Paires Fréquentes (%d joueurs)...%n", NB_JOUEURS_PAR_STRATEGIE);
        for (int i = 0; i < NB_JOUEURS_PAR_STRATEGIE; i++) {
            sim.ajouterJoueur(new Joueur(joueurId++, stratPaires));
        }

        // Groupe C : Les Plus Fréquents (StrategiePlusFrequents)
        logger.info("Création du groupe Numéros 'Chauds' (%d joueurs)...%n", NB_JOUEURS_PAR_STRATEGIE);
        for (int i = 0; i < NB_JOUEURS_PAR_STRATEGIE; i++) {
            sim.ajouterJoueur(new Joueur(joueurId++, stratPlusFreq));
        }

        // Groupe D : Les Moins Fréquents (StrategieMoinsFrequents)
        logger.info("Création du groupe Numéros 'Froids' (%d joueurs)...%n", NB_JOUEURS_PAR_STRATEGIE);
        for (int i = 0; i < NB_JOUEURS_PAR_STRATEGIE; i++) {
            sim.ajouterJoueur(new Joueur(joueurId++, stratMoinsFreq));
        }

        // Groupe E : Les Fixes (Témoins)
        logger.info("Création du groupe Fixe (2 joueurs)...");
        sim.ajouterJoueur(new Joueur(joueurId++, new StrategieFixe(Arrays.asList(2, 4, 8, 9, 12, 26), 1)));
        sim.ajouterJoueur(new Joueur(joueurId++, new StrategieFixe(Arrays.asList(10, 15, 22, 33, 38, 40), 5)));

        // 5. Lancement
        logger.info("\n--- DÉMARRAGE DE LA SIMULATION ---");
        logger.info("Analyse de " + data.size() + " tirages en cours...");
        sim.demarrer();
    }
}