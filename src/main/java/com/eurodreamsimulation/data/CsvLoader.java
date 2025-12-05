package com.eurodreamsimulation.data;

import com.eurodreamsimulation.model.ResultatRang;
import com.eurodreamsimulation.model.Tirage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CsvLoader {
    
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Charge l'historique depuis un fichier local (src/main/resources)
     */
    public List<Tirage> chargerDepuisRessources(String nomFichier) {
        List<Tirage> tirages = new ArrayList<>();
        InputStream is = getClass().getClassLoader().getResourceAsStream(nomFichier);

        if (is == null) {
            System.err.println("❌ ERREUR : Fichier '" + nomFichier + "' introuvable dans resources.");
            return tirages;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            chargerDonnees(br, tirages);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tirages;
    }

    /**
     * NOUVEAU : Télécharge et lit le CSV directement depuis l'URL de la FDJ (ZIP)
     */
    public List<Tirage> chargerDepuisURL(String urlAdresse) {
        List<Tirage> tirages = new ArrayList<>();
        System.out.println("Téléchargement des données depuis la FDJ...");

        try {
            URL url = new URL(urlAdresse);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // Important : certains serveurs bloquent les requêtes sans User-Agent (Java par défaut)
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            // On ouvre le flux et on le traite comme un ZIP
            try (InputStream in = connection.getInputStream();
                 ZipInputStream zis = new ZipInputStream(in)) {

                ZipEntry entry;
                // On parcourt les fichiers dans le ZIP jusqu'à trouver le CSV
                while ((entry = zis.getNextEntry()) != null) {
                    if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".csv")) {
                        System.out.println("✅ Fichier trouvé dans l'archive : " + entry.getName());
                        
                        // On lit le CSV (UTF-8 est standard pour le web, mais attention si c'est du Windows-1252)
                        // Pour la FDJ c'est souvent ISO-8859-1 ou UTF-8. Essayons UTF-8 par défaut.
                        BufferedReader br = new BufferedReader(new InputStreamReader(zis, StandardCharsets.UTF_8));
                        chargerDonnees(br, tirages);
                        
                        // Une fois le CSV lu, on peut arrêter de parcourir le ZIP
                        break; 
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du téléchargement/lecture : " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("-> " + tirages.size() + " tirages chargés.");
        return tirages;
    }

    /**
     * Méthode commune pour lire les lignes, quelle que soit la source (Fichier ou URL)
     */
    private void chargerDonnees(BufferedReader br, List<Tirage> tirages) throws IOException {
        String ligne;
        boolean header = true;
        while ((ligne = br.readLine()) != null) {
            if (header) { header = false; continue; }
            
            // Logique de parsing extraite ici
            Tirage t = parseLigne(ligne);
            if (t != null) {
                tirages.add(t);
            }
        }
    }

    private Tirage parseLigne(String ligne) {
        String[] cols = ligne.split(";", -1);
        if (cols.length < 30) return null;

        try {
            Tirage t = new Tirage();
            // Nettoyage des guillemets éventuels autour des chiffres
            t.idTirage = Long.parseLong(clean(cols[0]));
            t.dateTirage = LocalDate.parse(clean(cols[2]), dtf);
            
            t.boules = new ArrayList<>();
            for (int i=5; i<=10; i++) t.boules.add(safeInt(cols[i]));
            t.numeroDream = safeInt(cols[11]);

            t.mapRangs = new HashMap<>();
            // La mapRangs n'est plus utilisée pour le calcul (car fixée en dur dans Tirage.java)
            // mais on la garde pour compatibilité si besoin.
            
            return t;
        } catch (Exception e) { 
            // Ignorer les lignes malformées
            return null; 
        }
    }

    // Utilitaires de nettoyage
    private String clean(String s) {
        return s.replace("\"", "").trim();
    }

    private int safeInt(String s) {
        try { return Integer.parseInt(clean(s)); } catch (Exception e) { return 0; }
    }
    
    // Le safeDouble n'est plus utilisé avec les gains fixes, mais je le laisse au cas où
    private double safeDouble(String s) {
        try { return Double.parseDouble(s.replace(",", ".").replace(" ", "").replace("\u00A0", "").trim()); } 
        catch (Exception e) { return 0.0; }
    }
}