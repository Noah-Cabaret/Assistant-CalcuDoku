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
import java.io.InputStream;
import java.io.InputStreamReader;

import fr.univ.calcudoku.save.Sauvegarde;

/**
 * Coordonne le chargement et le lancement des parties de Calcudoku.
 * Gère le préchargement du FXML, la création de la sauvegarde et l'initialisation du contrôleur.
 */
public class GestionnaireJeu {

    private static final Gson GSON = new Gson();
    private static Parent vueJeuCachee = null;
    private static JeuController controleurJeuCache = null;
    private static Sauvegarde save;

    /** Précharge le FXML de la vue de jeu pour accélérer l'affichage. */
    public static void prechargerPageJeu() {
        javafx.application.Platform.runLater(() -> {
            try {
                if (vueJeuCachee == null) {
                    FXMLLoader loader = new FXMLLoader(GestionnaireJeu.class.getResource(Constantes.VUE_PARTIE)); 
                    vueJeuCachee = loader.load();
                    controleurJeuCache = loader.getController();
                }
            } catch (Exception e) {
                System.err.println("Erreur préchargement du FXML");
            }
        });
    }

    /**
     * Charge et lance une nouvelle partie depuis une ressource JSON.
     * @param stage la fenêtre principale
     * @param fichierJsonRessource le nom du fichier JSON de la grille
     */
    public static void chargerPartie(Stage stage, String fichierJsonRessource) {
        try {            
            InputStream is = GestionnaireJeu.class.getResourceAsStream(Constantes.CHEMIN_GRILLES_JSON + fichierJsonRessource);
            if (is == null) return;
            DonneesNiveau data = GSON.fromJson(new InputStreamReader(is), DonneesNiveau.class);
            Grille grille = JsonToModelAdapter.convertir(data);
            
            String idGrille = fichierJsonRessource.replace(".json", "");
            
            lancerPartie(stage, grille, "Partie", data, idGrille, false);
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Charge et reprend une partie sauvegardée depuis un fichier.
     * @param stage la fenêtre principale
     * @param fichier le fichier JSON de la sauvegarde
     */
    public static void chargerPartieDepuisFichier(Stage stage, File fichier) {
        if (fichier == null || !fichier.exists()) return;
        try {
            String idGrille = fichier.getName().replace(".json", "");

            InputStream is = GestionnaireJeu.class.getResourceAsStream(Constantes.CHEMIN_GRILLES_JSON + idGrille + ".json");
            if (is == null) {
                System.err.println("Erreur: Grille de base introuvable pour " + idGrille);
                return;
            }
            DonneesNiveau data = GSON.fromJson(new InputStreamReader(is), DonneesNiveau.class);
            Grille grille = JsonToModelAdapter.convertir(data);
            
            lancerPartie(stage, grille, "Reprise Partie", data, idGrille, true);
            
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    /**
     * Initialise et affiche la scène de jeu.
     * @param stage la fenêtre principale
     * @param grille la grille à jouer
     * @param titre le titre de la fenêtre
     * @param data les données du niveau
     * @param idGrille l'identifiant de la grille
     * @param reprise true si c'est une reprise de partie sauvegardée
     */
    public static void lancerPartie(Stage stage, Grille grille, String titre, DonneesNiveau data, String idGrille, boolean reprise) {
        try {
            Parent root;
            JeuController controller;
            if (vueJeuCachee != null && controleurJeuCache != null) {
                root = vueJeuCachee;
                controller = controleurJeuCache;
            } else {
                FXMLLoader loader = new FXMLLoader(GestionnaireJeu.class.getResource(Constantes.VUE_PARTIE));
                root = loader.load();
                controller = loader.getController();
            }

            save = new Sauvegarde();
            save.setIdGrille(idGrille);
            
            if (idGrille.startsWith("aventure")) {
                save.setMode(Sauvegarde.ModeDeJeu.AVEN);
                save.setDiff(Sauvegarde.Difficulte.FACIL); 
            } else if (idGrille.startsWith("libre")) {
                save.setMode(Sauvegarde.ModeDeJeu.LIBR);
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
                
                if (data.temps != null) save.getTemps().setTempsMax(data.temps);
                
            } else {
                if (data.defi != null) save.setDefi(data.defi);
                else save.setDefi(Defi.TypeDefi.AUCUN);
                save.setVies(data.vies);
                if (data.temps != null) save.getTemps().setTempsMax(data.temps);
            }
            
            controller.initialiserPartie(grille, save);

            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            if (GestionnaireJeu.class.getResource(Constantes.CHEMIN_CSS_CLAIR) != null) {
                String css = GestionnaireJeu.class.getResource(Constantes.CHEMIN_CSS_CLAIR).toExternalForm();
                if (!scene.getStylesheets().contains(css)) scene.getStylesheets().add(css);
            }

            stage.setTitle(titre);
            stage.show();

        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Lit les données d'un niveau depuis les ressources.
     * @param nomFichierJson le nom du fichier JSON
     * @return les données du niveau ou null si introuvable
     */
    public static DonneesNiveau lireDonneesNiveauRessource(String nomFichierJson) {
        try {
            InputStream is = GestionnaireJeu.class.getResourceAsStream(Constantes.CHEMIN_GRILLES_JSON + nomFichierJson);
            if (is != null) {
                return GSON.fromJson(new InputStreamReader(is), DonneesNiveau.class);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}