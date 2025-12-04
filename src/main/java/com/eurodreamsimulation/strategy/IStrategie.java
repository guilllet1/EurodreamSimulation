package com.eurodreamsimulation.strategy;
import com.eurodreamsimulation.model.Grille;

public interface IStrategie {
    // Génère une grille pour un tirage donné
    Grille genererGrille();
    String getNom();
}