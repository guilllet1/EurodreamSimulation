package com.eurodreamsimulation.strategy;

import com.eurodreamsimulation.model.Grille;
import com.eurodreamsimulation.model.Tirage;
import java.util.*;
import java.util.stream.Collectors;

public class StrategieMoinsFrequents implements IStrategie {

    private final List<Tirage> historiqueComplet;
    // Nom affiché dans le tableau de résultats
    private final String nom = "Stratégie 'Les Oubliés' (Moins Sortis)";

    public StrategieMoinsFrequents(List<Tirage> historique) {
        this.historiqueComplet = historique;
    }

    @Override
    public Grille genererGrille(Tirage tirageActuel) {
        // 1. Filtrage Temporel : On ne regarde que le passé connu
        List<Tirage> historiqueConnu = historiqueComplet.stream()
                .filter(t -> t.dateTirage.isBefore(tirageActuel.dateTirage))
                .collect(Collectors.toList());

        // Si pas d'historique (début de simulation), on joue aléatoire
        if (historiqueConnu.isEmpty()) {
            return genererGrilleAleatoire();
        }

        // 2. Initialisation des compteurs à 0
        // C'est important d'initialiser, sinon les numéros jamais sortis ne seraient pas dans la Map
        Map<Integer, Integer> frequenceBoules = new HashMap<>();
        for (int i = 1; i <= 40; i++) frequenceBoules.put(i, 0);

        Map<Integer, Integer> frequenceDreams = new HashMap<>();
        for (int i = 1; i <= 5; i++) frequenceDreams.put(i, 0);

        // 3. Comptage des occurrences sur l'historique connu
        for (Tirage t : historiqueConnu) {
            for (Integer b : t.boules) {
                frequenceBoules.put(b, frequenceBoules.get(b) + 1);
            }
            frequenceDreams.put(t.numeroDream, frequenceDreams.get(t.numeroDream) + 1);
        }

        // 4. Sélection des 6 boules les MOINS fréquentes (Tri Croissant)
        List<Integer> boulesChoisies = frequenceBoules.entrySet().stream()
                .sorted(Map.Entry.comparingByValue()) // Tri du plus petit au plus grand nombre de sorties
                .limit(6) // On prend les 6 premiers
                .map(Map.Entry::getKey) // On récupère le numéro de la boule
                .sorted() // On trie les numéros (ex: 5, 12...) pour la beauté de la grille
                .collect(Collectors.toList());

        // 5. Sélection du Dream le MOINS fréquent
        int dreamChoisi = frequenceDreams.entrySet().stream()
                .sorted(Map.Entry.comparingByValue()) // Tri croissant
                .map(Map.Entry::getKey)
                .findFirst() // Le premier est celui qui est sorti le moins souvent
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