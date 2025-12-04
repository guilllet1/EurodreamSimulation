package com.eurodreamsimulation.strategy;
import com.eurodreamsimulation.model.Grille;
import com.eurodreamsimulation.model.Tirage; // Import nécessaire

public interface IStrategie {
    // On ajoute le paramètre 'tirageActuel' pour donner le contexte
    Grille genererGrille(Tirage tirageActuel);
    String getNom();
}