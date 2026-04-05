package fr.univ.calcudoku;

import fr.univ.calcudoku.service.ProfileManager;
import fr.univ.calcudoku.utils.Constantes;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Classe principale de l'application Calcudoku.
 * Gère le démarrage de l'application JavaFX, la gestion de la scène principale
 * et la navigation entre les différentes vues (FXML).
 */
public class MainApp extends Application {

    /** La scène principale (fenêtre) de l'application. */
    private static Stage primaryStage;
    /** Le gestionnaire de profils, accessible statiquement dans toute l'application. */
    private static ProfileManager profileManager;
    private static boolean modeSombreActif = false;

    public static boolean isModeSombre() {
        return modeSombreActif;
    }

    public static void setModeSombre(boolean actif) {
        modeSombreActif = actif;
    }

    /**
     * Point d'entrée de l'application JavaFX.
     * Initialise la fenêtre principale, le gestionnaire de profils et affiche la première scène (accueil).
     *
     * @param stage La scène principale fournie par le framework JavaFX.
     * @throws Exception si une erreur survient lors du chargement.
     */
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        profileManager = new ProfileManager(); 

        changerScene(Constantes.VUE_ACCUEIL); 
        
        stage.setMinWidth(600);
        stage.setMinHeight(500);
        stage.setTitle("Calcudoku");

        // PLEIN ÉCRAN UNIQUEMENT AU TOUT PREMIER DÉMARRAGE
        stage.setMaximized(true);
        stage.setFullScreenExitHint(""); 
        
        stage.show();
    }

    /**
     * Change la vue affichée dans la scène principale.
     * Charge un nouveau fichier FXML et remplace le contenu de la scène actuelle.
     * Gère également l'application de la feuille de style appropriée (clair ou sombre).
     *
     * @param fxmlPath Le chemin vers le fichier FXML à charger.
     */
    public static void changerScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
            Parent root = loader.load();
            
            Scene scene = primaryStage.getScene();
            
            if (scene == null) {
                scene = new Scene(root, 800, 600);
                primaryStage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            scene.getStylesheets().clear();
            String cssClair = MainApp.class.getResource(Constantes.CHEMIN_CSS_CLAIR).toExternalForm();
            String cssSombre = MainApp.class.getResource(Constantes.CHEMIN_CSS_SOMBRE).toExternalForm();
            if (isModeSombre()) {
                scene.getStylesheets().add(cssSombre);
            } else {
                scene.getStylesheets().add(cssClair);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Impossible de charger le FXML : " + fxmlPath);
        }
    }

    /**
     * Retourne l'instance unique du gestionnaire de profils.
     *
     * @return Le ProfileManager de l'application.
     */
    public static ProfileManager getProfileManager() {
        return profileManager;
    }

    /**
     * Point d'entrée principal du programme.
     * Lance l'application JavaFX.
     *
     * @param args Les arguments de la ligne de commande (non utilisés).
     */
    public static void main(String[] args) {
        launch(args);
    }
}