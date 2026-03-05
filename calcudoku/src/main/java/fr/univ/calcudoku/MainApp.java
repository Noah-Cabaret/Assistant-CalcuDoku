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
        profileManager = new ProfileManager(); // Initialisation du service

        changerScene("/fxml/accueil.fxml"); // Démarrage
        
        stage.setMinWidth(600);
        stage.setMinHeight(500);
        stage.setTitle("Calcudoku");
        stage.show();
    }

    // Méthode pour changer de page
    public static void changerScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
            Parent root = loader.load();
            
            // Si la scène existe déjà, on remplace juste la racine
            if (primaryStage.getScene() == null) {
                primaryStage.setScene(new Scene(root, 800, 600));
            } else {
                primaryStage.getScene().setRoot(root);
            }
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