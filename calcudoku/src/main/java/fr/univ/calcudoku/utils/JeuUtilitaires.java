package fr.univ.calcudoku.utils;

import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.view.VueCase;
import fr.univ.calcudoku.view.VueGrille;
import fr.univ.calcudoku.MainApp;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import java.io.File;

public class JeuUtilitaires {

    private static Popup calcPopup;
    private static double xOffset = 0, yOffset = 0;

    public static void afficherCalculatrice(ActionEvent event) {
        try {
            if (calcPopup != null && calcPopup.isShowing()) { calcPopup.hide(); return; }
            
            Parent root;
            if (calcPopup == null) {
                root = FXMLLoader.load(JeuUtilitaires.class.getResource(Constantes.VUE_CALCULATRICE));
                root.getStyleClass().add("calculatrice-popup"); // Ligne magique
                
                calcPopup = new Popup();
                calcPopup.getContent().add(root);
                calcPopup.setAutoHide(false); 
                root.setMouseTransparent(false); 
                root.setOnMousePressed(e -> { xOffset = e.getSceneX(); yOffset = e.getSceneY(); });
                root.setOnMouseDragged(e -> { calcPopup.setX(e.getScreenX() - xOffset); calcPopup.setY(e.getScreenY() - yOffset); });
                calcPopup.setX(50); calcPopup.setY(200);
            } else {
                root = (Parent) calcPopup.getContent().get(0);
            }

            // --- CORRECTION ABSOLUE DU THÈME CALCULATRICE ---
            root.getStylesheets().removeIf(s -> s.contains("sombre.css"));
            if (MainApp.isModeSombre()) {
                root.getStylesheets().add(JeuUtilitaires.class.getResource("/styles/sombre.css").toExternalForm());
            }

            Stage mainStage = (Stage) ((Button)event.getSource()).getScene().getWindow();
            calcPopup.show(mainStage);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void cacherCalculatrice() {
        if (calcPopup != null && calcPopup.isShowing()) calcPopup.hide();
    }

    public static void sauvegarderImageGrille(Grille grilleModele, VueGrille vueGrille, VueCase vueCaseSelectionnee, String nomFichier, Runnable masquerAide) {
        try {
            String nomJoueur = MainApp.getProfileManager().getProfilActif();
            File dossierImages = new File(Constantes.DOSSIER_PROFILS + nomJoueur + Constantes.SOUS_DOSSIER_IMAGES);
            dossierImages.mkdirs();
            File fichierFinal = new File(dossierImages, nomFichier.endsWith(".png") ? nomFichier : nomFichier.replace(".json", "") + ".png");

            if (masquerAide != null) masquerAide.run();
            if (vueCaseSelectionnee != null) vueCaseSelectionnee.getStyleClass().remove(Constantes.CSS_CASE_SELECTIONNEE);
            
            for (int y = 0; y < grilleModele.getTaille(); y++) {
                for (int x = 0; x < grilleModele.getTaille(); x++) {
                    vueGrille.getGrilleVueCases(x, y).getStyleClass().remove(Constantes.CSS_CASE_ERREUR);
                }
            }
            
            vueGrille.setStyle("-fx-background-color: white;");
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT); 
            WritableImage image = vueGrille.snapshot(params, null);
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", fichierFinal);
            
            if (vueCaseSelectionnee != null) vueCaseSelectionnee.getStyleClass().add(Constantes.CSS_CASE_SELECTIONNEE);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static Thread hookSauvegardeBrutale;

    public static void installerSecuritesFermeture(Scene scene, Runnable actionSauvegardeNormale, Runnable actionSauvegardeBrutale) {
        hookSauvegardeBrutale = new Thread(() -> actionSauvegardeBrutale.run());
        Runtime.getRuntime().addShutdownHook(hookSauvegardeBrutale);
        Platform.runLater(() -> {
            if (scene != null && scene.getWindow() != null) {
                Stage stage = (Stage) scene.getWindow();
                stage.setOnCloseRequest(e -> actionSauvegardeNormale.run());
            }
        });
    }

    public static void desinstallerSecuritesFermeture(Scene scene) {
        if (hookSauvegardeBrutale != null) {
            try { Runtime.getRuntime().removeShutdownHook(hookSauvegardeBrutale); } catch (Exception e) {}
            hookSauvegardeBrutale = null;
        }
        if (scene != null && scene.getWindow() != null) {
            Stage stage = (Stage) scene.getWindow();
            stage.setOnCloseRequest(null);
        }
    }
}