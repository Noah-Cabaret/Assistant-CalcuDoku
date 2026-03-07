package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.utils.CacheRessources;
//import fr.univ.calcudoku.model.DonneesNiveau;
//import fr.univ.calcudoku.model.Grille;
//import fr.univ.calcudoku.service.JsonToModelAdapter;
import fr.univ.calcudoku.utils.GestionnaireJeu;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.File;
//import java.io.FileReader;
import java.io.InputStream;

//import com.google.gson.Gson;

public class MenuController {

    @FXML
    private HBox boxSousMenu;

    @FXML
    private ImageView imgAvatar;
    
    @FXML
    private Label labelNomProfil; // Pour changer le texte "Nom"

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

    //@FXML private void onLibreClick() { System.out.println("Mode Libre"); }
    @FXML 
    private void onLibreClick() { 
        // On récupère le nom du joueur
        String nomJoueur = MainApp.getProfileManager().getProfilActif();
        if (nomJoueur == null) nomJoueur = "Invité";
        
        // On cible le fichier de sauvegarde
        File fichier = new File("profils/" + nomJoueur + "/jeu/json/1.json");

        // le Gestionnaire tout faire !
        Stage stage = (Stage) boxSousMenu.getScene().getWindow();
        GestionnaireJeu.chargerPartieDepuisFichier(stage, fichier);
    }

    @FXML private void onAventureClick() { System.out.println("Mode Aventure"); }
    
    
    @FXML
    private void onQuitterClick() {
        System.exit(0);
    }
}