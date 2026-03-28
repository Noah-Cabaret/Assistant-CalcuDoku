package fr.univ.calcudoku.utils;

import fr.univ.calcudoku.controller.JeuController;
import fr.univ.calcudoku.model.DonneesNiveau;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.service.JsonToModelAdapter;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.google.gson.Gson;

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

    // Nouvelle partie depuis le menu principal
    public static void chargerPartie(Stage stage, String fichierJsonRessource) {
        try {            
            InputStream is = GestionnaireJeu.class.getResourceAsStream("/grilles/json/" + fichierJsonRessource);
            if (is == null) {
                System.err.println("Erreur : Fichier ressource introuvable -> " + fichierJsonRessource);
                return;
            }

            DonneesNiveau data = GSON.fromJson(new InputStreamReader(is), DonneesNiveau.class);
            Grille grille = JsonToModelAdapter.convertir(data);

            lancerPartie(stage, grille, "Partie Libre", fichierJsonRessource.replace(".json", ""), false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Reprise d'une partie depuis le Profil
    public static void chargerPartieDepuisFichier(Stage stage, File fichierSauvegardeJson) {
        if (fichierSauvegardeJson == null || !fichierSauvegardeJson.exists()) return;

        try {
            // 1. Lire la structure de base depuis les ressources internes
            String nomFichierBase = fichierSauvegardeJson.getName();
            InputStream is = GestionnaireJeu.class.getResourceAsStream("/grilles/json/" + nomFichierBase);
            if (is == null) return;
            
            DonneesNiveau data = GSON.fromJson(new InputStreamReader(is), DonneesNiveau.class);
            Grille grille = JsonToModelAdapter.convertir(data);

            // 2. Lancer la partie en spécifiant qu'il faut charger les données du profil
            lancerPartie(stage, grille, "Reprise Partie - " + nomFichierBase.replace(".json", ""), nomFichierBase.replace(".json", ""), true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Grille chargerGrilleSeuleRessource(String fichierJsonRessource) {
        try {
            InputStream is = GestionnaireJeu.class.getResourceAsStream("/grilles/json/" + fichierJsonRessource);
            if (is == null) return null;
            DonneesNiveau data = GSON.fromJson(new InputStreamReader(is), DonneesNiveau.class);
            return JsonToModelAdapter.convertir(data);
        } catch (Exception e) { return null; }
    }

    public static DonneesNiveau lireDonneesNiveauRessource(String fichierJsonRessource) {
        try {
            InputStream is = GestionnaireJeu.class.getResourceAsStream("/grilles/json/" + fichierJsonRessource);
            if (is == null) return null;
            return GSON.fromJson(new InputStreamReader(is), DonneesNiveau.class);
        } catch (Exception e) { return null; }
    }

    public static DonneesNiveau lireDonneesNiveauFichier(File fichier) {
        if (fichier == null || !fichier.exists()) return null;
        try (FileReader reader = new FileReader(fichier)) {
            return GSON.fromJson(reader, DonneesNiveau.class);
        } catch (Exception e) { return null; }
    }

    // Lancement universel de la partie
    public static void lancerPartie(Stage stage, Grille grille, String titre, String idGrille, boolean reprise) {
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

            // Si c'est une reprise, on charge la sauvegarde DANS la grille et l'objet save
            if (reprise) {
                String profilActif = fr.univ.calcudoku.MainApp.getProfileManager().getProfilActif();
                if (profilActif == null) profilActif = "Invité";
                save.charger(profilActif, grille);
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}