package com.eurodreamsimulation.strategy;

import com.eurodreamsimulation.model.Grille;
import com.eurodreamsimulation.model.Tirage;
import java.util.*;
import java.util.stream.Collectors;

public class StrategieAnalyse implements IStrategie {

    // On stocke tout l'historique brut, on ne l'analyse pas tout de suite
    private final List<Tirage> historiqueComplet;
    private final String nom = "Paires et Dream (Analyse Dynamique)";

    public StrategieAnalyse(List<Tirage> historique) {
        this.historiqueComplet = historique;
    }

    @Override
    public Grille genererGrille(Tirage tirageActuel) {
        // 1. FILTRAGE : On ne garde que les tirages ANTERIEURS à la date actuelle
        List<Tirage> historiqueConnu = historiqueComplet.stream()
                .filter(t -> t.dateTirage.isBefore(tirageActuel.dateTirage))
                .collect(Collectors.toList());

        // Si pas d'historique (ex: premier tirage), on joue aléatoire ou par défaut
        if (historiqueConnu.isEmpty()) {
            return genererGrilleAleatoire();
        }

        // 2. ANALYSE : Calculer les fréquences sur cet historique partiel
        Map<Integer, Integer> dreamFrequence = calculerFrequencesDream(historiqueConnu);
        Map<List<Integer>, Integer> pairesCounts = calculerFrequencesPaires(historiqueConnu);
        
        // Tri des paires
        List<Map.Entry<List<Integer>, Integer>> pairesParFrequence = pairesCounts.entrySet().stream()
                .sorted(Map.Entry.<List<Integer>, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        // 3. GENERATION : Même logique qu'avant, mais avec les données fraiches
        int dreamPrecedent = historiqueConnu.get(0).numeroDream; // Le plus récent du sous-ensemble
        int numeroDreamChoisi = choisirNumeroDream(dreamFrequence, dreamPrecedent);
        List<Integer> numerosChoisis = choisirNumerosParPaires(pairesParFrequence);

        // Complétion si nécessaire
        compléterSiNecessaire(numerosChoisis);
        
        //System.out.println("Date:"+tirageActuel.dateTirage + " // Grille : "+numerosChoisis.stream().sorted().distinct().limit(6).collect(Collectors.toList())+"-"+ numeroDreamChoisi);
        return new Grille(numerosChoisis.stream().sorted().distinct().limit(6).collect(Collectors.toList()), numeroDreamChoisi);
    }

    // --- Méthodes utilitaires (similaires à votre code original) ---

    private Grille genererGrilleAleatoire() {
        // Fallback simple si pas d'historique
        Random rand = new Random();
        List<Integer> nums = new ArrayList<>();
        while(nums.size() < 6) {
            int n = rand.nextInt(40) + 1;
            if(!nums.contains(n)) nums.add(n);
        }
        return new Grille(nums, rand.nextInt(5) + 1);
    }

    private Map<Integer, Integer> calculerFrequencesDream(List<Tirage> hist) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (Tirage t : hist) counts.put(t.numeroDream, counts.getOrDefault(t.numeroDream, 0) + 1);
        return counts;
    }

    private Map<List<Integer>, Integer> calculerFrequencesPaires(List<Tirage> hist) {
        Map<List<Integer>, Integer> counts = new HashMap<>();
        for (Tirage t : hist) {
            List<Integer> numbers = t.boules;
            for (int i = 0; i < 6; i++) {
                for (int j = i + 1; j < 6; j++) {
                    List<Integer> pair = Arrays.asList(numbers.get(i), numbers.get(j));
                    pair.sort(Comparator.naturalOrder());
                    counts.put(pair, counts.getOrDefault(pair, 0) + 1);
                }
            }
        }
        return counts;
    }

    private int choisirNumeroDream(Map<Integer, Integer> dreamFreq, int dreamPrecedent) {
        int maxFreq = dreamFreq.values().stream().max(Integer::compare).orElse(0);
        List<Integer> candidats = dreamFreq.entrySet().stream()
                .filter(e -> e.getValue() == maxFreq)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (candidats.contains(dreamPrecedent)) return dreamPrecedent;
        return candidats.isEmpty() ? 1 : Collections.min(candidats);
    }

    private List<Integer> choisirNumerosParPaires(List<Map.Entry<List<Integer>, Integer>> pairesFreq) {
        Set<Integer> combinaison = new HashSet<>();
        for (var entry : pairesFreq) {
            if (combinaison.size() >= 6) break;
            List<Integer> p = entry.getKey();
            if (combinaison.size() <= 4 && !combinaison.contains(p.get(0)) && !combinaison.contains(p.get(1))) {
                combinaison.add(p.get(0));
                combinaison.add(p.get(1));
            } else if (combinaison.size() < 6) {
                if (!combinaison.contains(p.get(0))) combinaison.add(p.get(0));
                else if (!combinaison.contains(p.get(1))) combinaison.add(p.get(1));
            }
        }
        return new ArrayList<>(combinaison);
    }
    
    private void compléterSiNecessaire(List<Integer> numeros) {
        Random rand = new Random();
        while (numeros.size() < 6) {
            int n = rand.nextInt(40) + 1;
            if (!numeros.contains(n)) numeros.add(n);
        }
    }

    @Override
    public String getNom() { return nom; }
}