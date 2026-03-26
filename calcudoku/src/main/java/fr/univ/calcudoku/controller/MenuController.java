package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.utils.CacheRessources;
import fr.univ.calcudoku.utils.GestionnaireJeu;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class MenuController {

    @FXML
    private HBox boxSousMenu;

    @FXML
    private ImageView imgAvatar;
    
    @FXML
    private Label labelNomProfil; 

    @FXML
    public void initialize() {

        imgAvatar.setImage(CacheRessources.getImage("/images/utilisateur.png"));

        // Récupérer le nom du profil connecté via le Manager
        String nomActuel = MainApp.getProfileManager().getProfilActif();
        if (nomActuel != null) {
            labelNomProfil.setText(nomActuel);
        } else {
            labelNomProfil.setText("Invité");
        }

        fr.univ.calcudoku.utils.ThemeUtil.appliquerFiltreBlancSiSombre(imgAvatar);
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
        // On récupère le nom du joueur
        String nomJoueur = MainApp.getProfileManager().getProfilActif();
        if (nomJoueur == null) nomJoueur = "Invité";
        
        // On cible le fichier de sauvegarde
        File fichier = new File("profils/" + nomJoueur + "/jeu/json/libre_5_2_1.json");

        // le Gestionnaire tout faire !
        Stage stage = (Stage) boxSousMenu.getScene().getWindow();
        GestionnaireJeu.chargerPartieDepuisFichier(stage, fichier);
    }

    @FXML private void onAventureClick() { MainApp.changerScene("/fxml/menu_aventure.fxml"); }
    
    
    @FXML
    private void onQuitterClick() {
        System.exit(0);
    }
}
