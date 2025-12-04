package com.eurodreamsimulation.data;

import com.eurodreamsimulation.model.ResultatRang;
import com.eurodreamsimulation.model.Tirage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CsvLoader {
    
    public List<Tirage> chargerDepuisRessources(String nomFichier) {
        List<Tirage> tirages = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        // Tentative de lecture depuis le Classpath (dossier resources)
        InputStream is = getClass().getClassLoader().getResourceAsStream(nomFichier);

        // --- AJOUT DU MESSAGE DE LOG EN CAS D'ERREUR ---
        if (is == null) {
            System.err.println("\n=======================================================");
            System.err.println("❌ ERREUR DE FICHIER : Le fichier '" + nomFichier + "' est introuvable.");
            System.err.println("Veuillez vous assurer qu'il est placé dans le répertoire:");
            System.err.println("    src/main/resources/");
            System.err.println("La simulation ne peut pas être lancée.");
            System.err.println("=======================================================\n");
            return tirages;
        }
        // --------------------------------------------------

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String ligne;
            boolean header = true;
            while ((ligne = br.readLine()) != null) {
                if (header) { header = false; continue; }
                
                String[] cols = ligne.split(";", -1);
                if (cols.length < 30) continue;

                try {
                    Tirage t = new Tirage();
                    t.idTirage = Long.parseLong(cols[0]);
                    t.dateTirage = LocalDate.parse(cols[2], dtf);
                    
                    t.boules = new ArrayList<>();
                    for (int i=5; i<=10; i++) t.boules.add(safeInt(cols[i]));
                    t.numeroDream = safeInt(cols[11]);

                    t.mapRangs = new HashMap<>();
                    int idxStart = 13;
                    for (int r=1; r<=6; r++) {
                        int idx = idxStart + (r-1)*3;
                        double gain = safeDouble(cols[idx+2]);
                        t.mapRangs.put(r, new ResultatRang(r, gain));
                    }
                    tirages.add(t);
                } catch (Exception e) { /* Ignorer ligne erreur */ }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return tirages;
    }

    private int safeInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }
    private double safeDouble(String s) {
        try { return Double.parseDouble(s.replace(",", ".").replace(" ", "").replace("\u00A0", "").trim()); } 
        catch (Exception e) { return 0.0; }
    }
}