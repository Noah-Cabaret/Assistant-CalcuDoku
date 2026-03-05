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

public class GestionnaireJeu {

    /**
     * Charger une partie depuis les RESSOURCES (src/main/resources/json/)
     * Utilisé pour le mode "Jeu Libre" de base.
     */
    public static void chargerPartie(Stage stage, String fichierJsonRessource) {
        try {
            Gson gson = new Gson();
            
            // On lit le fichier qui est DANS le .jar (dossier resources)
            InputStream is = GestionnaireJeu.class.getResourceAsStream("/json/" + fichierJsonRessource);
            if (is == null) {
                System.err.println("Erreur : Fichier ressource introuvable -> " + fichierJsonRessource);
                return;
            }

            // Lecture JSON -> DTO
            DonneesNiveau data = gson.fromJson(new InputStreamReader(is), DonneesNiveau.class);

            // Conversion DTO -> Modèle (Via l'Adaptateur)
            Grille grille = JsonToModelAdapter.convertir(data);

            // Lancement
            lancerPartie(stage, grille, "Partie Libre");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la ressource : " + e.getMessage());
        }
    }

    /**
     * Charger une partie depuis un FICHIER EXTERNE (profils/Nom/jeu/1.json)
     * Utilisé pour reprendre une sauvegarde ou lancer un niveau spécifique du profil.
     */
    public static void chargerPartieDepuisFichier(Stage stage, File fichier) {
        if (fichier == null || !fichier.exists()) {
            System.err.println("Erreur : Le fichier de sauvegarde n'existe pas.");
            return;
        }

        try {
            Gson gson = new Gson();

            // Lecture JSON -> DTO (Depuis le disque dur)
            DonneesNiveau data = gson.fromJson(new FileReader(fichier), DonneesNiveau.class);

            // Conversion DTO -> Modèle (Via l'Adaptateur)
            Grille grille = JsonToModelAdapter.convertir(data);

            //Lancement
            lancerPartie(stage, grille, "Reprise Partie - " + fichier.getName());

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du fichier : " + e.getMessage());
        }
    }

    /**
     * Méthode commune pour initialiser la fenêtre de jeu (FXML + Controller)
     */
    public static void lancerPartie(Stage stage, Grille grille, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(GestionnaireJeu.class.getResource("/fxml/VuePartie.fxml"));
            Parent root = loader.load();

            // On récupère le contrôleur du jeu pour lui passer la grille
            JeuController controller = loader.getController();
            controller.initialiserPartie(grille);

            Scene scene = new Scene(root, 1000, 800);

            // Chargement du CSS s'il existe
            if (GestionnaireJeu.class.getResource("/style.css") != null) {
                scene.getStylesheets().add(GestionnaireJeu.class.getResource("/style.css").toExternalForm());
            }

            stage.setTitle(titre);
            stage.setScene(scene);
            // stage.setFullScreen(true); // Décommentez si vous voulez le plein écran
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'ouverture de la fenêtre de jeu : " + e.getMessage());
        }
    }
}