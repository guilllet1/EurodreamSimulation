package com.eurodreamsimulation.simulation;

import com.eurodreamsimulation.strategy.IStrategie;

public class Joueur {
    private final int id;
    private final IStrategie strategie;
    private double totalGains = 0;
    private double totalDepenses = 0;
    private double maxSingleDrawGain = 0; // NOUVEAU : Maximum gagné sur une seule grille

    public Joueur(int id, IStrategie strategie) {
        this.id = id;
        this.strategie = strategie;
    }

    public void jouer(com.eurodreamsimulation.model.Tirage tirage) {
        // 1. Payer la grille (2.50€)
        totalDepenses += 2.50;

        // 2. Générer la grille via la stratégie
        var grille = strategie.genererGrille(tirage);
        
        // 3. Récupérer le gain exact du fichier CSV
        double gain = tirage.calculerGain(grille);
        totalGains += gain;
        
        // MISE À JOUR DU MAXIMUM PAR GRILLE
        if (gain > maxSingleDrawGain) {
            maxSingleDrawGain = gain;
        }
    }

    public double getSolde() { return totalGains - totalDepenses; }
    
    public double getTotalGains() { return totalGains; }
    
    public double getTotalDepenses() { return totalDepenses; } 
    
    // NOUVEAU GETTER
    public double getMaxSingleDrawGain() { return maxSingleDrawGain; }
    
    public IStrategie getStrategie() { return strategie; }
    public int getId() { return id; }
}