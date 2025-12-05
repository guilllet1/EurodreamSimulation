package com.eurodreamsimulation.strategy;

import com.eurodreamsimulation.model.Grille;
import com.eurodreamsimulation.model.Tirage;
import java.util.*;
import java.util.stream.Collectors;

public class StrategiePlusFrequents implements IStrategie {

    private final List<Tirage> historiqueComplet;
    private final String nom = "Stratégie 'Les Stars' (Plus Fréquents)";

    public StrategiePlusFrequents(List<Tirage> historique) {
        this.historiqueComplet = historique;
    }

    @Override
    public Grille genererGrille(Tirage tirageActuel) {
        // 1. Filtrage : On regarde uniquement le passé
        List<Tirage> historiqueConnu = historiqueComplet.stream()
                .filter(t -> t.dateTirage.isBefore(tirageActuel.dateTirage))
                .collect(Collectors.toList());

        // Si pas d'historique, on joue aléatoire
        if (historiqueConnu.isEmpty()) {
            return genererGrilleAleatoire();
        }

        // 2. Initialisation des compteurs
        Map<Integer, Integer> frequenceBoules = new HashMap<>();
        for (int i = 1; i <= 40; i++) frequenceBoules.put(i, 0);

        Map<Integer, Integer> frequenceDreams = new HashMap<>();
        for (int i = 1; i <= 5; i++) frequenceDreams.put(i, 0);

        // 3. Comptage
        for (Tirage t : historiqueConnu) {
            for (Integer b : t.boules) {
                frequenceBoules.put(b, frequenceBoules.get(b) + 1);
            }
            frequenceDreams.put(t.numeroDream, frequenceDreams.get(t.numeroDream) + 1);
        }

        // 4. Sélection des 6 boules les PLUS fréquentes (Tri Décroissant)
        List<Integer> boulesChoisies = frequenceBoules.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed()) // Le plus grand d'abord
                .limit(6)
                .map(Map.Entry::getKey)
                .sorted() // Tri pour l'affichage (ex: 2, 10, 15...)
                .collect(Collectors.toList());

        // 5. Sélection du Dream le PLUS fréquent
        int dreamChoisi = frequenceDreams.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(1);

        return new Grille(boulesChoisies, dreamChoisi);
    }

    private Grille genererGrilleAleatoire() {
        Random rand = new Random();
        List<Integer> nums = new ArrayList<>();
        while (nums.size() < 6) {
            int n = rand.nextInt(40) + 1;
            if (!nums.contains(n)) nums.add(n);
        }
        return new Grille(nums, rand.nextInt(5) + 1);
    }

    @Override
    public String getNom() { return nom; }
}