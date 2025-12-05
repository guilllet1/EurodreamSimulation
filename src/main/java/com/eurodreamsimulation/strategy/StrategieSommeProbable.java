package com.eurodreamsimulation.strategy;

import com.eurodreamsimulation.model.Grille;
import com.eurodreamsimulation.model.Tirage;
import java.util.*;

public class StrategieSommeProbable implements IStrategie {
    private final Random random = new Random();
    private final double multipleEcartType; // Le paramètre "n" (ex: 1.0)
    
    // --- CONSTANTES THÉORIQUES (EuroDreams : 6 boules parmi 40) ---
    // Moyenne théorique de la somme = 6 * (40+1)/2 = 123
    private static final double MOYENNE_THEORIQUE = 123.0;
    // Écart-type théorique (calcul combinatoire pour tirage sans remise) ≈ 26.4
    private static final double ECART_TYPE_THEORIQUE = 26.4;

    public StrategieSommeProbable(double multipleEcartType) {
        this.multipleEcartType = multipleEcartType;
    }

    @Override
    public Grille genererGrille(Tirage t) {
        Set<Integer> boules = new HashSet<>();
        
        // Boucle infinie jusqu'à trouver une combinaison qui respecte la contrainte
        while (true) {
            boules.clear();
            // 1. Générer 6 numéros aléatoires
            while (boules.size() < 6) {
                boules.add(random.nextInt(40) + 1);
            }
            
            // 2. Calculer la somme
            int somme = boules.stream().mapToInt(Integer::intValue).sum();
            
            // 3. Vérifier si la somme est dans l'intervalle [Moyenne +/- n*EcartType]
            double limiteBasse = MOYENNE_THEORIQUE - (multipleEcartType * ECART_TYPE_THEORIQUE);
            double limiteHaute = MOYENNE_THEORIQUE + (multipleEcartType * ECART_TYPE_THEORIQUE);

            if (somme >= limiteBasse && somme <= limiteHaute) {
                break; // La grille est valide, on sort de la boucle
            }
            // Sinon, on recommence la génération
        }
        
        // Le numéro Dream est choisi totalement au hasard (1 à 5)
        int dream = random.nextInt(5) + 1;
        
        return new Grille(new ArrayList<>(boules), dream);
    }

    @Override
    public String getNom() {
        return "Somme Probable (Intervalle " + multipleEcartType + " \u03C3)";
    }
}