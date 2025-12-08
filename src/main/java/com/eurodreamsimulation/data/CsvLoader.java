package com.eurodreamsimulation.data;

import com.eurodreamsimulation.model.ResultatRang;
import com.eurodreamsimulation.model.Tirage;
// 1. Imports Log4j
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    
    // 2. Déclaration du Logger
    private static final Logger logger = LogManager.getLogger(CsvLoader.class);
    
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Charge l'historique depuis un fichier local (src/main/resources)
     */
    public List<Tirage> chargerDepuisRessources(String nomFichier) {
        List<Tirage> tirages = new ArrayList<>();
        InputStream is = getClass().getClassLoader().getResourceAsStream(nomFichier);

        if (is == null) {
            // Remplacement de System.err par logger.error
            logger.error("Fichier '{}' introuvable dans le dossier resources.", nomFichier);
            return tirages;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            chargerDonnees(br, tirages);
        } catch (IOException e) {
            // On loggue l'exception proprement
            logger.error("Erreur de lecture du fichier ressource : {}", e.getMessage(), e);
        }
        return tirages;
    }

    /**
     * Télécharge et lit le CSV directement depuis l'URL de la FDJ (ZIP)
     */
    public List<Tirage> chargerDepuisURL(String urlAdresse) {
        List<Tirage> tirages = new ArrayList<>();
        logger.info("Tentative de téléchargement des données depuis l'URL FDJ...");

        try {
            URL url = new URL(urlAdresse);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            // On ouvre le flux et on le traite comme un ZIP
            try (InputStream in = connection.getInputStream();
                 ZipInputStream zis = new ZipInputStream(in)) {

                ZipEntry entry;
                // On parcourt les fichiers dans le ZIP
                while ((entry = zis.getNextEntry()) != null) {
                    if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".csv")) {
                        logger.info("Fichier CSV trouvé dans l'archive : {}", entry.getName());
                        
                        BufferedReader br = new BufferedReader(new InputStreamReader(zis, StandardCharsets.UTF_8));
                        chargerDonnees(br, tirages);
                        
                        break; // Fichier trouvé et lu, on arrête
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Échec du téléchargement ou de la lecture URL : {}", e.getMessage());
            // On ne printStackTrace que si on est en mode debug pour ne pas polluer les logs de prod
            logger.debug("Stacktrace détaillé : ", e);
        }
        
        if (!tirages.isEmpty()) {
            logger.info("{} tirages chargés avec succès depuis l'URL.", tirages.size());
        } else {
            logger.warn("Aucun tirage n'a pu être extrait de l'URL.");
        }
        
        return tirages;
    }

    /**
     * Méthode commune pour lire les lignes
     */
    private void chargerDonnees(BufferedReader br, List<Tirage> tirages) throws IOException {
        String ligne;
        boolean header = true;
        while ((ligne = br.readLine()) != null) {
            if (header) { header = false; continue; }
            
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
            t.idTirage = Long.parseLong(clean(cols[0]));
            t.dateTirage = LocalDate.parse(clean(cols[2]), dtf);
            
            t.boules = new ArrayList<>();
            for (int i=5; i<=10; i++) t.boules.add(safeInt(cols[i]));
            t.numeroDream = safeInt(cols[11]);

            t.mapRangs = new HashMap<>();
            
            return t;
        } catch (Exception e) { 
            // On peut logguer en DEBUG les lignes ignorées si besoin
            // logger.debug("Ligne ignorée (format incorrect) : {}", ligne);
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
    
    private double safeDouble(String s) {
        try { return Double.parseDouble(s.replace(",", ".").replace(" ", "").replace("\u00A0", "").trim()); } 
        catch (Exception e) { return 0.0; }
    }
}