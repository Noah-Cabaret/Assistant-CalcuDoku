package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import java.io.InputStream;

public class MenuController {

    @FXML
    private HBox boxSousMenu;

    @FXML
    private ImageView imgAvatar;
    
    @FXML
    private Label labelNomProfil; // Pour changer le texte "Nom"

    @FXML
    public void initialize() {
        // Charger l'image
        try {
            InputStream is = getClass().getResourceAsStream("/images/utilisateur.png");
            if (is != null) imgAvatar.setImage(new Image(is));
        } catch (Exception e) { }

        // Récupérer le nom du profil connecté via le Manager
        String nomActuel = MainApp.getProfileManager().getProfilActif();
        if (nomActuel != null) {
            labelNomProfil.setText(nomActuel);
        } else {
            labelNomProfil.setText("Invité");
        }
    }

    @FXML
    private void onJouerClick() {
        boxSousMenu.setVisible(!boxSousMenu.isVisible());
    }

    //Gestion du Clic sur le Profil (Retour Accueil)
    @FXML
    private void onProfilClick() {
        // Ça va vers la page de statistiques/profil
        MainApp.changerScene("/fxml/profil.fxml");
    }

    @FXML private void onLibreClick() { System.out.println("Mode Libre"); }
    @FXML private void onAventureClick() { System.out.println("Mode Aventure"); }
    
    
    @FXML
    private void onQuitterClick() {
        System.exit(0);
    }
}