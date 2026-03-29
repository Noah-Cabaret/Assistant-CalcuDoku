package fr.univ.calcudoku.controller;

import java.io.File;

import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.utils.CacheRessources;
import fr.univ.calcudoku.utils.GestionnaireJeu;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.paint.Color;

public class MenuController {

    @FXML
    private HBox boxSousMenu;

    @FXML private FontIcon imgAvatar;
    
    @FXML
    private Label labelNomProfil; 

    @FXML
    public void initialize() {

        imgAvatar.setIconColor(MainApp.modeSombreActif ? Color.WHITE : Color.BLACK);
        String nomActuel = MainApp.getProfileManager().getProfilActif();
        if (nomActuel != null) {
            labelNomProfil.setText(nomActuel);
        } else {
            labelNomProfil.setText("Invité");
        }

        Platform.runLater(() -> {
            javafx.scene.Scene scene = boxSousMenu.getScene();
            if (scene != null) {
                String cssPath = getClass().getResource("/styles/sombre.css").toExternalForm();
                if (MainApp.modeSombreActif && !scene.getStylesheets().contains(cssPath)) {
                    scene.getStylesheets().add(cssPath);
                } else if (!MainApp.modeSombreActif) {
                    scene.getStylesheets().remove(cssPath);
                }
                
                // --- CORRECTION DU TEXTE DES BOUTONS QUI SE GRISE ---
                scene.getRoot().lookupAll(".button").forEach(noeud -> {
                    Button btn = (Button) noeud;
                    if (btn.getText() != null && !btn.getText().isEmpty()) {
                        String couleurT = MainApp.modeSombreActif ? "white" : "black";
                        // On ajoute !important virtuellement en forçant le style
                        btn.setStyle(btn.getStyle() + " -fx-text-fill: " + couleurT + ";");
                    }
                });
            }
        });

        GestionnaireJeu.prechargerPageJeu();
    }

    @FXML
    private void onJouerClick() {
        boxSousMenu.setVisible(!boxSousMenu.isVisible());
    }

    //Gestion du Clic sur le Profil (Retour Accueil)
    @FXML
    private void onProfilClick() {
        // On prévient le Profil qu'il faudra revenir au menu
        ProfilController.pagePrecedente = "/fxml/menu.fxml";
        MainApp.changerScene("/fxml/profil.fxml");
    }

    @FXML 
    private void onLibreClick() { 
        MainApp.changerScene("/fxml/menu_libre.fxml");
    }

    @FXML private void onAventureClick() { MainApp.changerScene("/fxml/menu_aventure.fxml"); }
    
    
    @FXML
    private void onQuitterClick() {
        System.exit(0);
    }

    @FXML 
    private void onReglesClick() { 
        MainApp.changerScene("/fxml/reglesTechniques.fxml"); 
    }
}
