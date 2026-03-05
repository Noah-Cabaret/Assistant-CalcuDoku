package fr.univ.calcudoku;

import fr.univ.calcudoku.service.ProfileManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    private static Stage primaryStage;
    private static ProfileManager profileManager;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        profileManager = new ProfileManager(); 

        changerScene("/fxml/accueil.fxml"); 
        
        stage.setMinWidth(600);
        stage.setMinHeight(500);
        stage.setTitle("Calcudoku");

        // --- 1. PLEIN ÉCRAN AU DÉMARRAGE ---
        stage.setMaximized(true);
        stage.setFullScreenExitHint(""); // Cache le message "Appuyez sur Echap"
        // ------------------------------------
        
        stage.show();
    }

    // Méthode pour changer de page
    public static void changerScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
            Parent root = loader.load();
            
            if (primaryStage.getScene() == null) {
                primaryStage.setScene(new Scene(root, 800, 600));
            } else {
                primaryStage.getScene().setRoot(root);
            }

            // --- 2. MAINTENIR LE PLEIN ÉCRAN ---
            // On le réactive à chaque changement de page par sécurité
            if (primaryStage.isShowing()) {
                primaryStage.setFullScreen(false);
                primaryStage.setMaximized(true);
                primaryStage.setFullScreenExitHint("");
            }
            // -----------------------------------

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Impossible de charger le FXML : " + fxmlPath);
        }
    }

    public static ProfileManager getProfileManager() {
        return profileManager;
    }

    public static void main(String[] args) {
        launch(args);
    }
}