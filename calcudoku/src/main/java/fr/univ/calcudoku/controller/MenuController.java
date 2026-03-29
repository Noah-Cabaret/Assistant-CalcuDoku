package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.utils.Constantes;
import fr.univ.calcudoku.utils.GestionnaireJeu;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.paint.Color;

public class MenuController {

    @FXML
    private HBox boxSousMenu;

    @FXML 
    private FontIcon imgAvatar;
    
    @FXML
    private Label labelNomProfil; 

    @FXML
    public void initialize() {
        // Adaptation de la couleur de l'avatar au thème
        imgAvatar.setIconColor(MainApp.modeSombreActif ? Color.WHITE : Color.BLACK);
        
        String nomActuel = MainApp.getProfileManager().getProfilActif();
        labelNomProfil.setText(nomActuel);

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

        // Optimisation : On précharge la vue du jeu pendant que l'utilisateur est sur le menu
        GestionnaireJeu.prechargerPageJeu();
    }

    @FXML
    private void onJouerClick() {
        boxSousMenu.setVisible(!boxSousMenu.isVisible());
    }

    @FXML
    private void onProfilClick() {
        ProfilController.pagePrecedente = Constantes.VUE_MENU;
        MainApp.changerScene(Constantes.VUE_PROFIL);
    }

    @FXML 
    private void onLibreClick() { 
        MainApp.changerScene(Constantes.VUE_MENU_LIBRE);
    }

    @FXML 
    private void onAventureClick() { 
        MainApp.changerScene(Constantes.VUE_MENU_AVENTURE); 
    }
    
    @FXML
    private void onReglesClick() { 
        MainApp.changerScene(Constantes.VUE_REGLES); 
    }

    @FXML
    private void onQuitterClick() {
        Platform.exit();
    }
}