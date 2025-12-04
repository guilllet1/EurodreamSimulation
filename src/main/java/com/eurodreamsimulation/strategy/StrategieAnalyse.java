package com.eurodreamsimulation.strategy;

import com.eurodreamsimulation.model.Grille;
import com.eurodreamsimulation.model.Tirage;

import java.util.*;
import java.util.stream.Collectors;

public class StrategieAnalyse implements IStrategie {

    // Structure pour stocker les paires et leur fréquence, triées par fréquence décroissante
    private final List<Map.Entry<List<Integer>, Integer>> pairesParFrequence;
    
    // Fréquence des Dreams (clé: numéro Dream, valeur: fréquence)
    private final Map<Integer, Integer> dreamFrequence;

    // Numéro Dream du tirage précédent (pour la désambiguïsation en cas d'égalité)
    private int dreamPrecedent;

    // Nom pour l'affichage dans le résumé
    private final String nom = "Paires et Dream (Analyse Fréquences)";

    public StrategieAnalyse(List<Tirage> historique) {
        // L'analyse est faite dans le constructeur, une seule fois.
        
        // Initialisation du Dream précédent (prend le dernier tirage connu dans l'historique)
        if (!historique.isEmpty()) {
            this.dreamPrecedent = historique.get(0).numeroDream; // Le premier élément est le tirage le plus récent dans notre CSV
            System.out.println("historique.get(0).dateTirage " + historique.get(0).dateTirage);
        } else {
            this.dreamPrecedent = 1; // Valeur par défaut
        }

        // 1. Calcul des fréquences de Dream
        this.dreamFrequence = calculerFrequencesDream(historique);
        
        // 2. Calcul et tri des paires
        Map<List<Integer>, Integer> pairesCounts = calculerFrequencesPaires(historique);
        this.pairesParFrequence = pairesCounts.entrySet().stream()
                .sorted(Map.Entry.<List<Integer>, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        System.out.println("Stratégie d'analyse initialisée. Total paires uniques: " + pairesParFrequence.size());
    }
    
    // --- Méthodes d'Analyse (Calculées une seule fois) ---

    private Map<Integer, Integer> calculerFrequencesDream(List<Tirage> historique) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (Tirage t : historique) {
            counts.put(t.numeroDream, counts.getOrDefault(t.numeroDream, 0) + 1);
        }
        return counts;
    }

    private Map<List<Integer>, Integer> calculerFrequencesPaires(List<Tirage> historique) {
        Map<List<Integer>, Integer> pairCounts = new HashMap<>();
        for (Tirage t : historique) {
            // Générer toutes les paires pour ce tirage
            List<Integer> numbers = t.boules;
            for (int i = 0; i < 6; i++) {
                for (int j = i + 1; j < 6; j++) {
                    // Trier la paire pour qu'elle soit unique (ex: [1, 5] est pareil que [5, 1])
                    List<Integer> pair = Arrays.asList(numbers.get(i), numbers.get(j));
                    pair.sort(Comparator.naturalOrder());
                    pairCounts.put(pair, pairCounts.getOrDefault(pair, 0) + 1);
                }
            }
        }
        return pairCounts;
    }

    // --- Méthode de Génération de Grille (Le cœur de la Stratégie) ---
    
    @Override
    public Grille genererGrille() {
        // 1. Choix du Numéro Dream (Le plus fréquent, avec désambiguïsation)
        int numeroDreamChoisi = choisirNumeroDream();

        // 2. Choix des 6 Numéros (Maximiser le score total des paires)
        List<Integer> numerosChoisis = choisirNumerosParPaires();
        
        
        // Sécurité: si l'algorithme n'a pas réussi à trouver 6 numéros uniques, compléter par de l'aléatoire.
        if (numerosChoisis.size() < 6) {
             System.err.println("Avertissement: La stratégie n'a pas pu former 6 numéros uniques. Complétion aléatoire.");
             Random rand = new Random();
             while (numerosChoisis.size() < 6) {
                 int nouveauNum = rand.nextInt(40) + 1;
                 if (!numerosChoisis.contains(nouveauNum)) {
                     numerosChoisis.add(nouveauNum);
                 }
             }
        }
        
        System.err.println("Numéro choisi : "+numerosChoisis.stream().distinct().limit(6).collect(Collectors.toList()));

        // Il est crucial de s'assurer qu'il y a bien 6 numéros uniques.
        return new Grille(numerosChoisis.stream().distinct().limit(6).collect(Collectors.toList()), numeroDreamChoisi);
    }
    
    private int choisirNumeroDream() {
        // Trouve la fréquence maximale
        int maxFreq = dreamFrequence.values().stream().max(Integer::compare).orElse(0);

        // Liste des numéros Dream ayant cette fréquence maximale
        List<Integer> candidats = dreamFrequence.entrySet().stream()
                .filter(entry -> entry.getValue() == maxFreq)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (candidats.size() == 1) {
            return candidats.get(0); // Choix unique
        } else if (candidats.contains(dreamPrecedent)) {
            // Règle de désambiguïsation: "tu prends le numéro dream tiré au tirage précédent"
            return dreamPrecedent;
        } else {
            // En cas d'égalité sans match sur le tirage précédent, on prend le plus petit pour une détermination fixe
            return Collections.min(candidats);
        }
    }

    private List<Integer> choisirNumerosParPaires() {
        Set<Integer> combinaisonFinale = new HashSet<>();
        int scoreActuel = 0;
        
        // Nous allons parcourir les paires les plus fréquentes
        for (Map.Entry<List<Integer>, Integer> paireEntry : pairesParFrequence) {
            List<Integer> paire = paireEntry.getKey();
            int num1 = paire.get(0);
            int num2 = paire.get(1);
            
            // Si l'ajout de cette paire n'augmente pas la taille de la combinaison au-delà de 6
            boolean num1DejaPris = combinaisonFinale.contains(num1);
            boolean num2DejaPris = combinaisonFinale.contains(num2);

            // Règle: On doit maximiser le score (somme des numéros) total des paires.
            // La façon la plus simple est d'ajouter les paires tant que la combinaison reste cohérente (6 numéros uniques)
            
            if (combinaisonFinale.size() < 6) {
                // Si la combinaison a moins de 6 numéros, on essaie d'ajouter la paire
                
                // Si les deux numéros sont nouveaux et qu'on a de la place
                if (!num1DejaPris && !num2DejaPris && combinaisonFinale.size() <= 4) {
                    combinaisonFinale.add(num1);
                    combinaisonFinale.add(num2);
                } 
                // Si un seul numéro est nouveau
                else if (!num1DejaPris && combinaisonFinale.size() < 6) {
                    combinaisonFinale.add(num1);
                }
                else if (!num2DejaPris && combinaisonFinale.size() < 6) {
                    combinaisonFinale.add(num2);
                }
            }

            // Si nous avons 6 numéros, nous arrêtons de chercher
            if (combinaisonFinale.size() == 6) {
                break;
            }
        }
        
        // Pour être sûr, on prend les 6 premiers et on les trie
        return combinaisonFinale.stream()
                .sorted()
                .limit(6)
                .collect(Collectors.toList());
    }

    @Override
    public String getNom() {
        return nom;
    }
}