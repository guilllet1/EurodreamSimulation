package com.eurodreamsimulation.strategy;
import com.eurodreamsimulation.model.Grille;
import com.eurodreamsimulation.model.Tirage;
import java.util.List;

public class StrategieFixe implements IStrategie {
    private final Grille grilleFixe;

    public StrategieFixe(List<Integer> numeros, int dream) {
        this.grilleFixe = new Grille(numeros, dream);
    }

    @Override
    public Grille genererGrille(Tirage t) {
        return grilleFixe; // Joue toujours la même
    }

    @Override
    public String getNom() { return "Toujours les mêmes : " + grilleFixe; }
}