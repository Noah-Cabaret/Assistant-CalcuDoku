package fr.univ.calcudoku.controller;

import com.google.gson.Gson;
import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.model.DonneesNiveau; // Import du modèle
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.service.JsonToModelAdapter;
import fr.univ.calcudoku.service.ProfileManager;
import fr.univ.calcudoku.utils.CacheRessources;
import fr.univ.calcudoku.view.VueGrille;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Map;

public class ProfilController {

    private static final Gson GSON = new Gson();

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
        File dossierJson = new File("profils/" + nomProfil + "/jeu/json");
        
        if (!dossierJson.exists() || !dossierJson.isDirectory()) return;

        File[] fichiersJson = dossierJson.listFiles((dir, name) -> name.endsWith(".json"));

        if (fichiersJson != null && boxParties != null) {
            boxParties.getChildren().clear();

            for (File fichier : fichiersJson) {
                try (FileReader reader = new FileReader(fichier)) {
                    DonneesNiveau niveau = GSON.fromJson(reader, DonneesNiveau.class);
                    VBox carte = creerCartePartie(niveau, fichier);
                    boxParties.getChildren().add(carte);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private VBox creerCartePartie(DonneesNiveau niveau, File fichierJson) {
        VBox vBox = new VBox(5);
        vBox.setAlignment(Pos.CENTER);

        String nomPng = fichierJson.getName().replace(".json", ".png");

        File dossierJeu = fichierJson.getParentFile().getParentFile();
        
        File dossierImages = new File(dossierJeu, "images");
        File fichierImage = new File(dossierImages, nomPng);

        ImageView vueMiniature = new ImageView();

        if (fichierImage.exists()) {
            vueMiniature.setImage(new Image(fichierImage.toURI().toString(), true));
        } else {
            Image imageParDefaut = CacheRessources.getImage("/grilles/images/" + nomPng);
            if (imageParDefaut != null) {
                vueMiniature.setImage(imageParDefaut);
            }
        }

        vueMiniature.setFitWidth(160);
        vueMiniature.setFitHeight(160);
        vueMiniature.setPreserveRatio(true);

        String nomPropre = fichierJson.getName().replace(".json", "");
        Label titre = new Label("Grille " + nomPropre);
        titre.setStyle("-fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-font-size: 11px;");

        int min = niveau.temps / 60;
        int sec = niveau.temps % 60;
        Label lblTemps = new Label(String.format("Temps : %d:%02d", min, sec));
        lblTemps.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 10px; -fx-text-fill: #333333;");

        vBox.getChildren().addAll(vueMiniature, titre, lblTemps);
        return vBox;
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
        imgAvatar.setImage(CacheRessources.getImage("/images/utilisateur.png"));
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