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
    public static boolean modeSombreActif = false;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        profileManager = new ProfileManager(); 

        changerScene("/fxml/accueil.fxml"); 
        
        stage.setMinWidth(600);
        stage.setMinHeight(500);
        stage.setTitle("Calcudoku");

        //PLEIN ÉCRAN AU DÉMARRAGE
        stage.setMaximized(true);
        stage.setFullScreenExitHint(""); // Cache le message "Appuyez sur Echap"
        
        stage.show();
    }

    // Méthode pour changer de page
    // Méthode pour changer de page
    public static void changerScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
            Parent root = loader.load();
            
            Scene scene = primaryStage.getScene();
            
            // 1. On crée la scène si elle n'existe pas, sinon on remplace juste le contenu (le "root")
            if (scene == null) {
                scene = new Scene(root, 800, 600);
                primaryStage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            // 2. --- GESTION DU THÈME SOMBRE ---
            // Maintenant qu'on est SÛR que la scène existe, on applique ou retire le CSS
            String cssPath = MainApp.class.getResource("/styles/sombre.css").toExternalForm();
            if (modeSombreActif) {
                if (!scene.getStylesheets().contains(cssPath)) {
                    scene.getStylesheets().add(cssPath);
                }
            } else {
                scene.getStylesheets().remove(cssPath); // Sécurité : on l'enlève si le mode clair est actif
            }

            // 3. MAINTENIR LE PLEIN ÉCRAN
            if (primaryStage.isShowing()) {
                primaryStage.setFullScreen(false);
                primaryStage.setMaximized(true);
                primaryStage.setFullScreenExitHint("");
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
