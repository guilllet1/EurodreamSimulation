package com.eurodreamsimulation.simulation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class GeminiClient {

    private static final Logger logger = LogManager.getLogger(GeminiClient.class);
    private static final String API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";
    private static final Properties CONFIG = new Properties();

    static {
        try (InputStream input = GeminiClient.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) CONFIG.load(input);
        } catch (IOException ex) {
            logger.error("Impossible de charger config.properties", ex);
        }
    }

    public static String analyserTirage(String descriptionTirage) {
        String apiKey = CONFIG.getProperty("gemini.api.key");
        String model = CONFIG.getProperty("gemini.model", "gemini-1.5-flash");

        if (apiKey == null || apiKey.length() < 10) {
            return "⚠️ Clé API Gemini manquante ou invalide.";
        }

        try {
            // 1. Prompt (On précise que c'est une analyse statistique pour éviter le blocage "Jeu d'argent")
            String prompt = "Agis comme un statisticien. Analyse froidement ce tirage de loterie : " + descriptionTirage + ". " +
                    "Donne un commentaire factuel court (3 phrases max) sur la distribution des chiffres (pairs/impairs, dizaines...). " +
                    "Ne donne aucun conseil de jeu.";

            // 2. JSON Body
            JSONObject textPart = new JSONObject().put("text", prompt);
            JSONObject parts = new JSONObject().put("parts", new JSONArray().put(textPart));
            JSONObject content = new JSONObject().put("contents", new JSONArray().put(parts));

            // 3. Envoi
            String url = String.format(API_URL_TEMPLATE, model, apiKey);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(content.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 4. Analyse Robuste de la réponse
            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                
                // Vérification de sécurité : Est-ce qu'il y a des candidats ?
                JSONArray candidates = jsonResponse.optJSONArray("candidates");
                if (candidates == null || candidates.isEmpty()) {
                    return "⚠️ Réponse vide de l'IA.";
                }

                JSONObject firstCandidate = candidates.getJSONObject(0);
                
                // Cas 1 : Réponse bloquée par la sécurité
                if (firstCandidate.has("finishReason") && !"STOP".equals(firstCandidate.getString("finishReason"))) {
                    return "⚠️ Analyse bloquée par l'IA (Raison: " + firstCandidate.getString("finishReason") + ")";
                }

                // Cas 2 : Réponse valide
                if (firstCandidate.has("content") && firstCandidate.getJSONObject("content").has("parts")) {
                    return firstCandidate.getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");
                }
                
                return "⚠️ Structure JSON inattendue.";
                
            } else {
                return "Erreur HTTP " + response.statusCode() + " : " + response.body();
            }

        } catch (Exception e) {
            logger.error("Erreur technique Gemini", e);
            return "Erreur technique : " + e.getMessage();
        }
    }
}