package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.InputStream;

public class ProfilController {

    @FXML private ImageView imgAvatar;
    @FXML private Label lblNomProfil;

    // Labels de statistiques (à remplir plus tard avec les vraies données)
    @FXML private Label lblTempsMoyen;
    @FXML private Label lblTauxReussite;
    @FXML private Label lblNiveauAventure;
    @FXML private Label lblDifficulteMax;
    @FXML private Label lblMeilleurScore;

    @FXML
    public void initialize() {
        // 1. Charger l'image de profil
        try {
            InputStream is = getClass().getResourceAsStream("/images/utilisateur.png");
            if (is != null) imgAvatar.setImage(new Image(is));
        } catch (Exception e) { }

        // 2. Charger le nom du joueur connecté
        String nomActuel = MainApp.getProfileManager().getProfilActif();
        lblNomProfil.setText(nomActuel != null ? nomActuel : "Invité");

        // ICI : Plus tard, tu récupéreras les vraies stats via ProfileManager
        // Exemple : Stats stats = MainApp.getProfileManager().getStats(nomActuel);
        // lblTempsMoyen.setText("Temps moyen : " + stats.getMoyen());
    }

    @FXML
    private void onRetourClick() {
        // Retour au MENU PRINCIPAL
        MainApp.changerScene("/fxml/menu.fxml");
    }

    @FXML
    private void onDeconnexionClick() {
        // Déconnexion -> Retour à l'ACCUEIL (Choix des profils)
        System.out.println("Déconnexion...");
        MainApp.changerScene("/fxml/accueil.fxml");
    }
}