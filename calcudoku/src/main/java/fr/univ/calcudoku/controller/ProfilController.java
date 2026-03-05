package fr.univ.calcudoku.controller;

import com.google.gson.Gson;
import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.model.DonneesNiveau; // Import du modèle
import fr.univ.calcudoku.service.MiniGridFactory; // Import de la factory
import fr.univ.calcudoku.service.ProfileManager;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Map;

public class ProfilController {

    @FXML private ImageView imgAvatar;
    @FXML private Label lblNomProfil;
    @FXML private Label lblTempsMoyen, lblTauxReussite, lblNiveauAventure, lblDifficulteMax, lblMeilleurScore;
    @FXML private CheckBox checkSombre, checkClair;
    @FXML private HBox boxParties;

    @FXML
    public void initialize() {
        ProfileManager manager = MainApp.getProfileManager();
        String nomActuel = manager.getProfilActif();
        if (nomActuel == null) nomActuel = "Invité";

        lblNomProfil.setText(nomActuel);
        chargerAvatar();
        chargerStatistiquesProfil(nomActuel, manager);
        chargerPartiesSauvegardees(nomActuel);
    }

    private void chargerPartiesSauvegardees(String nomProfil) {
        File dossierJeux = new File("profils/" + nomProfil + "/jeu");
        if (!dossierJeux.exists() || !dossierJeux.isDirectory()) return;

        File[] fichiersJson = dossierJeux.listFiles((dir, name) -> name.endsWith(".json"));

        if (fichiersJson != null && boxParties != null) {
            Gson gson = new Gson();
            boxParties.getChildren().clear(); // Nettoyer avant d'ajouter

            for (File fichier : fichiersJson) {
                try (FileReader reader = new FileReader(fichier)) {
                    // 1. Désérialisation via le Modèle (POO)
                    DonneesNiveau niveau = gson.fromJson(reader, DonneesNiveau.class);
                    
                    // 2. Création visuelle via la Factory (POO)
                    VBox carte = MiniGridFactory.createMiniature(niveau, fichier.getName());
                    
                    // 3. Ajout à la vue
                    boxParties.getChildren().add(carte);

                } catch (Exception e) {
                    System.err.println("Erreur lecture fichier : " + fichier.getName());
                }
            }
        }
    }

    private void chargerStatistiquesProfil(String nom, ProfileManager manager) {
        Map<String, String> stats = manager.lireStatistiques(nom);

        lblTempsMoyen.setText("Temps total : " + formatTemps(stats.getOrDefault("temps_total", "0")));
        
        try {
            double ratio = Double.parseDouble(stats.getOrDefault("ratio_parties", "0")) * 100;
            lblTauxReussite.setText("Taux de réussite : " + (int)ratio + "%");
        } catch(Exception e) { lblTauxReussite.setText("Taux : 0%"); }

        lblNiveauAventure.setText("Niveau aventure : " + stats.getOrDefault("progression", "1"));
        lblMeilleurScore.setText("Meilleur score : " + stats.getOrDefault("score_max", "0"));
        
        String d = stats.getOrDefault("difficulte_max", "1");
        lblDifficulteMax.setText("Difficulté max : " + (d.equals("3") ? "Difficile" : (d.equals("2") ? "Moyenne" : "Facile")));

        boolean isSombre = Boolean.parseBoolean(stats.getOrDefault("mode_sombre", "false"));
        if (checkSombre != null) checkSombre.setSelected(isSombre);
        if (checkClair != null) checkClair.setSelected(!isSombre);
    }
    
    private void chargerAvatar() {
        try {
            InputStream is = getClass().getResourceAsStream("/images/utilisateur.png");
            if (is != null) imgAvatar.setImage(new Image(is));
        } catch (Exception e) { }
    }
    
    private String formatTemps(String s) {
        try {
            int t = Integer.parseInt(s);
            return (t / 3600 > 0 ? t/3600 + "h " : "") + (t % 3600) / 60 + "min";
        } catch (Exception e) { return "0min"; }
    }

    @FXML private void onRetourClick() { MainApp.changerScene("/fxml/menu.fxml"); }
    @FXML private void onDeconnexionClick() { MainApp.changerScene("/fxml/accueil.fxml"); }
}