package fr.univ.calcudoku.utils;

import fr.univ.calcudoku.challenge.Defi;
import fr.univ.calcudoku.controller.JeuController;
import fr.univ.calcudoku.model.DonneesNiveau;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.service.JsonToModelAdapter;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.google.gson.Gson;
import fr.univ.calcudoku.MainApp;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import fr.univ.calcudoku.save.Sauvegarde;

public class GestionnaireJeu {

    private static final Gson GSON = new Gson();
    private static Parent vueJeuCachee = null;
    private static JeuController controleurJeuCache = null;
    private static Sauvegarde save;

    public static void prechargerPageJeu() {
        javafx.application.Platform.runLater(() -> {
            try {
                if (vueJeuCachee == null) {
                    FXMLLoader loader = new FXMLLoader(GestionnaireJeu.class.getResource("/fxml/partie.fxml")); 
                    vueJeuCachee = loader.load();
                    controleurJeuCache = loader.getController();
                }
            } catch (Exception e) {
                System.err.println("Erreur préchargement du FXML");
            }
        });
    }

    public static void chargerPartie(Stage stage, String fichierJsonRessource) {
        try {            
            InputStream is = GestionnaireJeu.class.getResourceAsStream("/grilles/json/" + fichierJsonRessource);
            if (is == null) return;
            DonneesNiveau data = GSON.fromJson(new InputStreamReader(is), DonneesNiveau.class);
            Grille grille = JsonToModelAdapter.convertir(data);
            
            // --- CORRECTION : On extrait l'ID de la grille pour la sauvegarde ---
            String idGrille = fichierJsonRessource.replace(".json", "");
            
            lancerPartie(stage, grille, "Partie", data, idGrille, false);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void chargerPartieDepuisFichier(Stage stage, File fichier) {
        if (fichier == null || !fichier.exists()) return;
        try {
            // 1. On récupère l'ID du niveau grâce au nom du fichier de sauvegarde
            String idGrille = fichier.getName().replace(".json", "");

            // 2. On charge la grille DE BASE (les cages, le temps cible) depuis les ressources du jeu !
            InputStream is = GestionnaireJeu.class.getResourceAsStream("/grilles/json/" + idGrille + ".json");
            if (is == null) {
                System.err.println("Erreur: Grille de base introuvable pour " + idGrille);
                return;
            }
            DonneesNiveau data = GSON.fromJson(new InputStreamReader(is), DonneesNiveau.class);
            Grille grille = JsonToModelAdapter.convertir(data);
            
            // 3. On lance la partie en mode "reprise" (c'est Sauvegarde.java qui lira le tableau du joueur)
            lancerPartie(stage, grille, "Reprise Partie", data, idGrille, true);
            
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    public static void lancerPartie(Stage stage, Grille grille, String titre, DonneesNiveau data, String idGrille, boolean reprise) {
        try {
            Parent root;
            JeuController controller;
            if (vueJeuCachee != null && controleurJeuCache != null) {
                root = vueJeuCachee;
                controller = controleurJeuCache;
            } else {
                FXMLLoader loader = new FXMLLoader(GestionnaireJeu.class.getResource("/fxml/partie.fxml"));
                root = loader.load();
                controller = loader.getController();
            }

            save = new Sauvegarde();
            save.setIdGrille(idGrille);
            
            // --- CORRECTION : SÉPARATION CLAIRE AVENTURE / LIBRE ---
            if (idGrille.startsWith("aventure")) {
                save.setMode(Sauvegarde.ModeDeJeu.AVEN);
                // On fixe une valeur par défaut silencieuse pour l'aventure
                save.setDiff(Sauvegarde.Difficulte.FACIL); 
            } else if (idGrille.startsWith("libre")) {
                save.setMode(Sauvegarde.ModeDeJeu.LIBR);
                // En libre, on lit la vraie difficulté dans le nom (ex: libre_5_2_1 -> 2 = MOYEN)
                String[] parts = idGrille.split("_");
                if (parts.length >= 3) {
                    if (parts[2].equals("2")) save.setDiff(Sauvegarde.Difficulte.MOYEN);
                    else if (parts[2].equals("3")) save.setDiff(Sauvegarde.Difficulte.DIFFI);
                    else save.setDiff(Sauvegarde.Difficulte.FACIL);
                }
            }

            if (reprise) {
                String nomActuel = MainApp.getProfileManager().getProfilActif();
                if (nomActuel == null) nomActuel = "Invité";
                save.charger(nomActuel, grille);
            } else {
                if (data.defi != null) save.setDefi(data.defi);
                else save.setDefi(Defi.TypeDefi.AUCUN);
                save.setVies(data.vies);
                save.tmp.setTempsMax(data.temps);
            }
            
            controller.initialiserPartie(grille, save);

            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            if (GestionnaireJeu.class.getResource("/styles/style.css") != null) {
                String css = GestionnaireJeu.class.getResource("/styles/style.css").toExternalForm();
                if (!scene.getStylesheets().contains(css)) scene.getStylesheets().add(css);
            }

            stage.setTitle(titre);
            stage.show();

            if (!stage.isMaximized()) stage.setMaximized(true);

        } catch (Exception e) { e.printStackTrace(); }
    }

    public static DonneesNiveau lireDonneesNiveauRessource(String nomFichierJson) {
        try {
            InputStream is = GestionnaireJeu.class.getResourceAsStream("/grilles/json/" + nomFichierJson);
            if (is != null) {
                return GSON.fromJson(new InputStreamReader(is), DonneesNiveau.class);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}