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

/**
 * Contrôleur de la vue du menu principal.
 * Gère la navigation vers les différents modes de jeu (Libre, Aventure),
 * le profil, les règles et la sortie de l'application.
 */
public class MenuController {

    /**
     * Conteneur pour les boutons de sous-menu (Libre, Aventure).
     */
    @FXML
    private HBox boxSousMenu;

    /**
     * Icône représentant l'avatar du profil utilisateur.
     */
    @FXML 
    private FontIcon imgAvatar;
    
    /**
     * Label affichant le nom du profil actuellement connecté.
     */
    @FXML
    private Label labelNomProfil; 

    /**
     * Méthode d'initialisation appelée après le chargement du FXML.
     * Configure l'affichage en fonction du profil actif et du thème (sombre/clair),
     * et précharge la vue du jeu pour une meilleure performance.
     */
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
                        String couleurT = MainApp.modeSombreActif ? "white" : "white";
                        // On ajoute !important virtuellement en forçant le style
                        btn.setStyle(btn.getStyle() + " -fx-text-fill: " + couleurT + ";");
                    }
                });
            }
        });

        // Optimisation : On précharge la vue du jeu pendant que l'utilisateur est sur le menu
        GestionnaireJeu.prechargerPageJeu();
    }

    /**
     * Gère le clic sur le bouton "Jouer".
     * Affiche ou masque le sous-menu contenant les modes de jeu.
     */
    @FXML
    private void onJouerClick() {
        boxSousMenu.setVisible(!boxSousMenu.isVisible());
    }

    /**
     * Gère le clic sur le bouton du profil.
     * Redirige l'utilisateur vers la page de son profil.
     */
    @FXML
    private void onProfilClick() {
        ProfilController.pagePrecedente = Constantes.VUE_MENU;
        MainApp.changerScene(Constantes.VUE_PROFIL);
    }

    /**
     * Gère le clic sur le bouton "Mode Libre".
     * Redirige l'utilisateur vers le menu du mode libre.
     */
    @FXML 
    private void onLibreClick() { 
        MainApp.changerScene(Constantes.VUE_MENU_LIBRE);
    }

    /**
     * Gère le clic sur le bouton "Mode Aventure".
     * Redirige l'utilisateur vers le menu du mode aventure.
     */
    @FXML 
    private void onAventureClick() { 
        MainApp.changerScene(Constantes.VUE_MENU_AVENTURE); 
    }
    
    /**
     * Gère le clic sur le bouton "Règles".
     * Redirige l'utilisateur vers la page des règles et techniques.
     */
    @FXML
    private void onReglesClick() { 
        MainApp.changerScene(Constantes.VUE_REGLES); 
    }

    /**
     * Gère le clic sur le bouton "Quitter".
     * Ferme l'application.
     */
    @FXML
    private void onQuitterClick() {
        Platform.exit();
    }
}