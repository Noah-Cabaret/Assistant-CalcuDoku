package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.utils.Constantes;
import fr.univ.calcudoku.utils.GestionnaireJeu;
import javafx.application.Platform;
import javafx.fxml.FXML;
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