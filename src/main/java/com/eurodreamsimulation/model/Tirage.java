package com.eurodreamsimulation.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class Tirage {
    public long idTirage;
    public LocalDate dateTirage;
    public List<Integer> boules;
    public int numeroDream;
    
    // On conserve la map pour ne pas casser le CsvLoader, 
    // mais elle ne sera plus utilisée pour le calcul des gains.
    public Map<Integer, ResultatRang> mapRangs; 

    // Méthode clé : Calcule le gain pour une grille donnée sur CE tirage
    public double calculerGain(Grille grilleJouee) {
        int bonsNumeros = 0;
        for (Integer n : grilleJouee.getNumeros()) {
            if (boules.contains(n)) bonsNumeros++;
        }
        boolean bonDream = (grilleJouee.getNumeroDream() == this.numeroDream);

        int rangAtteint = determinerRang(bonsNumeros, bonDream);

        // --- MODIFICATION : Application du barème fixe ---
        switch (rangAtteint) {
            case 1: return 20000.0; // 6 numéros + Dream
            case 2: return 2000.0;  // 6 numéros
            case 3: return 100.0;   // 5 numéros
            case 4: return 40.0;    // 4 numéros
            case 5: return 5.0;     // 3 numéros
            case 6: return 2.5;     // 2 numéros
            default: return 0.0;    // Perdu
        }
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