package com.eurodreamsimulation.strategy;
import com.eurodreamsimulation.model.Grille;
import com.eurodreamsimulation.model.Tirage;
import java.util.*;

public class StrategieAleatoire implements IStrategie {
    private final Random random = new Random();

    @Override
    public Grille genererGrille(Tirage t) {
        Set<Integer> boules = new HashSet<>();
        while (boules.size() < 6) {
            boules.add(random.nextInt(40) + 1); // 1 à 40
        }
        int dream = random.nextInt(5) + 1; // 1 à 5
        return new Grille(new ArrayList<>(boules), dream);
    }

    @Override
    public String getNom() { return "Aléatoire"; }
}