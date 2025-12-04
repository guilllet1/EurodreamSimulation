package com.eurodreamsimulation.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class Tirage {
    public long idTirage;
    public LocalDate dateTirage;
    public List<Integer> boules;
    public int numeroDream;
    
    // Map pour un accès rapide : Rang -> Info
    public Map<Integer, ResultatRang> mapRangs; 

    // Méthode clé : Calcule le gain pour une grille donnée sur CE tirage
    public double calculerGain(Grille grilleJouee) {
        int bonsNumeros = 0;
        for (Integer n : grilleJouee.getNumeros()) {
            if (boules.contains(n)) bonsNumeros++;
        }
        boolean bonDream = (grilleJouee.getNumeroDream() == this.numeroDream);

        int rangAtteint = determinerRang(bonsNumeros, bonDream);

        if (rangAtteint > 0 && mapRangs.containsKey(rangAtteint)) {
            return mapRangs.get(rangAtteint).rapport;
        }
        return 0.0;
    }

    private int determinerRang(int nbBoules, boolean bonDream) {
        if (nbBoules == 6 && bonDream) return 1;
        if (nbBoules == 6) return 2;
        if (nbBoules == 5) return 3;
        if (nbBoules == 4) return 4;
        if (nbBoules == 3) return 5;
        if (nbBoules == 2) return 6;
        return 0; // Perdu
    }
}