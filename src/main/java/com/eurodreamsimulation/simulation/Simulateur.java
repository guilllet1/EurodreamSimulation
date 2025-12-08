package com.eurodreamsimulation.simulation;

import com.eurodreamsimulation.model.Tirage;
// 1. Imports Log4j
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Simulateur {
    // 2. Définition du logger
    private static final Logger logger = LogManager.getLogger(Simulateur.class);

    private final List<Tirage> historiqueTirages;
    private final List<Joueur> joueurs;

    public Simulateur(List<Tirage> historiqueTirages) {
        this.historiqueTirages = historiqueTirages;
        this.joueurs = new ArrayList<>();
    }

    public void ajouterJoueur(Joueur j) {
        joueurs.add(j);
    }

    public void demarrer() {
        // Remplacement de System.out par logger.info avec {} pour les variables
        logger.info("--- Début de la Simulation sur {} tirages ---", historiqueTirages.size());
        
        for (Tirage t : historiqueTirages) {
            for (Joueur j : joueurs) {
                j.jouer(t);
            }
        }
        
        afficherResultatsParStrategie();
    }

    private void afficherResultatsParStrategie() {
        Map<String, StrategieStats> statsParStrategie = new HashMap<>();

        // 1. Collecte des statistiques
        for (Joueur j : joueurs) {
            String nomStrategie = j.getStrategie().getNom();
            
            StrategieStats stats = statsParStrategie.computeIfAbsent(
                nomStrategie, k -> new StrategieStats(nomStrategie)
            );
            
            stats.nombreJoueurs++;
            stats.totalGainsCumules += j.getTotalGains();
            stats.totalDepensesCumulees += j.getTotalDepenses();
            
            if (j.getMaxSingleDrawGain() > stats.maxSingleGridGain) {
                stats.maxSingleGridGain = j.getMaxSingleDrawGain();
            }

            if (j.getTotalGains() > stats.maxCumulativeGain) {
                stats.maxCumulativeGain = j.getTotalGains();
            }
        }

        // 2. Tri des résultats (Conservé de notre modification précédente)
        List<StrategieStats> listeTriee = new ArrayList<>(statsParStrategie.values());
        // Tri décroissant par Gain Moyen Brut
        listeTriee.sort(Comparator.comparingDouble(StrategieStats::getGainMoyenBrut).reversed());

        // 3. Affichage du résumé avec Log4j
        logger.info(""); // Saut de ligne
        logger.info("============================================================================================================================================================================");
        logger.info("                                                                    RÉSUMÉ DE LA SIMULATION PAR STRATÉGIE (Total: {} joueurs)", joueurs.size());
        logger.info("                                                                    (Classé par Gain Moyen Brut décroissant)");
        logger.info("============================================================================================================================================================================");
        
        // Utilisation de String.format pour conserver l'alignement des colonnes dans le log
        logger.info(String.format("| %-60s | %10s | %15s | %15s | %15s | %15s | %15s |", 
            "STRATÉGIE", "Nbre Joueurs", "Gain Max (Joueur)", "Gain Max (Grille)", "Gain Moyen Brut", "Gain Moyen Net", "Remboursement (%)"));
        logger.info("----------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        for (StrategieStats stats : listeTriee) {
            double gainMoyenBrut = stats.getGainMoyenBrut();
            double depenseMoyenne = stats.totalDepensesCumulees / stats.nombreJoueurs;
            double gainMoyenNet = gainMoyenBrut - depenseMoyenne;
            
            double tauxRemboursement = (depenseMoyenne != 0) ? (gainMoyenBrut / depenseMoyenne) * 100 : 0.0;

            logger.info(String.format("| %-60s | %10d | %15.2f € | %15.2f € | %15.2f € | %15.2f € | %15.2f |", 
                stats.nom, 
                stats.nombreJoueurs, 
                stats.maxCumulativeGain,
                stats.maxSingleGridGain,
                gainMoyenBrut,
                gainMoyenNet,
                tauxRemboursement));
        }
        logger.info("============================================================================================================================================================================");
    }
    
    private static class StrategieStats {
        String nom;
        int nombreJoueurs = 0;
        double totalGainsCumules = 0;
        double totalDepensesCumulees = 0;
        double maxSingleGridGain = 0; 
        double maxCumulativeGain = 0;

        public StrategieStats(String nom) {
            this.nom = nom;
        }

        // Méthode helper pour le tri
        public double getGainMoyenBrut() {
            if (nombreJoueurs == 0) return 0.0;
            return totalGainsCumules / nombreJoueurs;
        }
    }
}