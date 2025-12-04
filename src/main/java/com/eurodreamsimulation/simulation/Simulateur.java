package com.eurodreamsimulation.simulation;

import com.eurodreamsimulation.model.Tirage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Simulateur {
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
        System.out.println("--- Début de la Simulation sur " + historiqueTirages.size() + " tirages ---");
        
        for (Tirage t : historiqueTirages) {
            for (Joueur j : joueurs) {
                j.jouer(t);
            }
        }
        
        afficherResultatsParStrategie();
    }

    private void afficherResultatsParStrategie() {
        Map<String, StrategieStats> statsParStrategie = new HashMap<>();

        // 1. Collecte des statistiques de tous les joueurs
        for (Joueur j : joueurs) {
            String nomStrategie = j.getStrategie().getNom();
            
            StrategieStats stats = statsParStrategie.computeIfAbsent(
                nomStrategie, k -> new StrategieStats(nomStrategie)
            );
            
            stats.nombreJoueurs++;
            stats.totalGainsCumules += j.getTotalGains();
            stats.totalDepensesCumulees += j.getTotalDepenses();
            
            // Suivi du gain maximal obtenu sur une seule grille
            if (j.getMaxSingleDrawGain() > stats.maxSingleGridGain) {
                stats.maxSingleGridGain = j.getMaxSingleDrawGain();
            }

            // Suivi du gain maximal cumulé obtenu par un joueur
            if (j.getTotalGains() > stats.maxCumulativeGain) {
                stats.maxCumulativeGain = j.getTotalGains();
            }
        }

        // 2. Affichage du résumé par stratégie
        System.out.println("\n============================================================================================================================================================================");
        System.out.println("                                                                    RÉSUMÉ DE LA SIMULATION PAR STRATÉGIE (Total: " + joueurs.size() + " joueurs)");
        System.out.println("============================================================================================================================================================================");
        
        // CORRECTION D'ALIGNEMENT : Retrait du symbole Euro (€) des en-têtes de colonne
        System.out.printf("| %-60s | %10s | %15s | %15s | %15s | %15s | %15s |%n", 
            "STRATÉGIE", "Nbre Joueurs", "Gain Max (Joueur)", "Gain Max (Grille)", "Gain Moyen Brut", "Gain Moyen Net", "Remboursement (%)");
        System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        for (StrategieStats stats : statsParStrategie.values()) {
            double gainMoyenBrut = stats.totalGainsCumules / stats.nombreJoueurs; 
            double depenseMoyenne = stats.totalDepensesCumulees / stats.nombreJoueurs;
            double gainMoyenNet = gainMoyenBrut - depenseMoyenne;
            
            double tauxRemboursement = (depenseMoyenne != 0) ? (gainMoyenBrut / depenseMoyenne) * 100 : 0.0;

            // La ligne de contenu reste alignée à droite
            System.out.printf("| %-60s | %10d | %15.2f € | %15.2f € | %15.2f € | %15.2f € | %15.2f |%n", 
                stats.nom, 
                stats.nombreJoueurs, 
                stats.maxCumulativeGain,
                stats.maxSingleGridGain,
                gainMoyenBrut,
                gainMoyenNet,
                tauxRemboursement);
        }
        System.out.println("============================================================================================================================================================================");
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
    }
}