package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.model.DonneesNiveau;
import fr.univ.calcudoku.service.ProfileManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.Map;
import java.util.Scanner;

public class ProfilController {

    public static String pagePrecedente = "/fxml/menu.fxml";

    @FXML private FontIcon imgAvatar;
    @FXML private Label lblNomProfil;
    @FXML private VBox boxCentrale;
    
    // --- NOUVEAU : Labels des statistiques complets ---
    @FXML private Label lblPartiesJouees, lblVictoires, lblTempsMoyen, lblTauxReussite, lblNiveauAventure, lblDifficulteMax, lblMeilleurScore;
    
    @FXML private RadioButton radioSombre, radioClair;
    @FXML private RadioButton radioProfilCombinaisons, radioProfilCalculatrice;
    @FXML private javafx.scene.control.ToggleGroup groupeAide;
    @FXML private HBox boxParties;
    @FXML private javafx.scene.control.ToggleGroup groupeTheme;
    @FXML private Button btnRetour;
    @FXML private Button btnDeconnexion;

    @FXML
    public void initialize() {
        ProfileManager manager = MainApp.getProfileManager();
        String nomActuel = manager.getProfilActif();
        if (nomActuel == null) nomActuel = "Invité";

        lblNomProfil.setText(nomActuel);
        chargerAvatar();
        chargerStatistiquesProfil(nomActuel, manager);
        
        if (boxParties != null) boxParties.getChildren().clear();
        chargerPartiesSauvegardees(nomActuel);
        
        if (radioSombre != null) radioSombre.setOnAction(e -> {
            activerModeSombre(true);
            if (boxParties != null) { boxParties.getChildren().clear(); chargerPartiesSauvegardees(lblNomProfil.getText()); }
            manager.mettreAJourStatistique(lblNomProfil.getText(), "mode_sombre", "true");
        });
        
        if (radioClair != null) radioClair.setOnAction(e -> {
            activerModeSombre(false);
            if (boxParties != null) { boxParties.getChildren().clear(); chargerPartiesSauvegardees(lblNomProfil.getText()); }
            manager.mettreAJourStatistique(lblNomProfil.getText(), "mode_sombre", "false");
        });

        if (radioProfilCombinaisons != null) radioProfilCombinaisons.setOnAction(e -> manager.mettreAJourStatistique(lblNomProfil.getText(), "aide_calcul", "combinaisons"));
        if (radioProfilCalculatrice != null) radioProfilCalculatrice.setOnAction(e -> manager.mettreAJourStatistique(lblNomProfil.getText(), "aide_calcul", "calculatrice"));
    }

    private void chargerPartiesSauvegardees(String nomProfil) {
        File dossierParties = new File("profils/" + nomProfil + "/parties");
        chargerFichiersDossier(nomProfil, dossierParties);

        File dossierAventure = new File("profils/" + nomProfil + "/parties/aventure");
        chargerFichiersDossier(nomProfil, dossierAventure);
    }

    private void chargerFichiersDossier(String nomProfil, File dossier) {
        if (!dossier.exists() || !dossier.isDirectory()) return;

        File[] fichiersJson = dossier.listFiles((dir, name) -> name.endsWith(".json"));

        if (fichiersJson != null && boxParties != null) {
            for (File fichierJson : fichiersJson) {
                DonneesNiveau niveauBase = fr.univ.calcudoku.utils.GestionnaireJeu.lireDonneesNiveauRessource(fichierJson.getName());
                
                if (niveauBase != null) {
                    File fichierIni = new File(dossier, fichierJson.getName().replace(".json", ".ini"));
                    int tempsSauvegarde = lireTempsDepuisIni(fichierIni);
                    
                    VBox carte = creerCartePartie(nomProfil, fichierJson, tempsSauvegarde);
                    boxParties.getChildren().add(carte);
                }
            }
        }
    }

    private int lireTempsDepuisIni(File fichierIni) {
        if (!fichierIni.exists()) return 0;
        try (Scanner sc = new Scanner(fichierIni)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.startsWith("temps=")) return (int) Double.parseDouble(line.split("=")[1].trim());
            }
        } catch (Exception e) {}
        return 0;
    }

    private VBox creerCartePartie(String nomProfil, File fichierJson, int temps) {
        File fichierImage = new File("profils/" + nomProfil + "/jeu/images/" + fichierJson.getName().replace(".json", ".png"));
        ImageView imgView = new ImageView();
        if (fichierImage.exists()) imgView.setImage(new Image(fichierImage.toURI().toString()));
        else imgView.setStyle("-fx-background-color: lightgray;");

        imgView.setFitHeight(150); imgView.setFitWidth(150); imgView.setPreserveRatio(true);

        String nomPropre = fichierJson.getName().replace(".json", "");
        Label lblTitre = new Label("Grille " + nomPropre);
        Label lblTemps = new Label(String.format("Temps : %d:%02d", temps / 60, temps % 60));

        if (MainApp.modeSombreActif) {
            lblTitre.setStyle("-fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: white;");
            lblTemps.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 11px; -fx-text-fill: #cccccc;");
        } else {
            lblTitre.setStyle("-fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: black;");
            lblTemps.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 11px; -fx-text-fill: #555555;");
        }

        VBox carte = new VBox(10, imgView, lblTitre, lblTemps);
        carte.setAlignment(Pos.CENTER); carte.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        String styleNormal = "-fx-cursor: hand; -fx-padding: 10; -fx-background-color: transparent; -fx-border-color: transparent; -fx-border-radius: 10; -fx-background-radius: 10;";
        String styleHover = "-fx-cursor: hand; -fx-padding: 10; -fx-background-color: #f5f5f5; -fx-border-color: #cccccc; -fx-border-radius: 10; -fx-background-radius: 10;";

        if (MainApp.modeSombreActif) styleHover = "-fx-cursor: hand; -fx-padding: 10; -fx-background-color: #444444; -fx-border-color: #777777; -fx-border-radius: 10; -fx-background-radius: 10;";

        carte.setStyle(styleNormal);
        String finalStyleHover = styleHover;
        carte.setOnMouseEntered(e -> carte.setStyle(finalStyleHover));
        carte.setOnMouseExited(e -> carte.setStyle(styleNormal));

        carte.setOnMouseClicked(e -> {
            javafx.stage.Stage stage = (javafx.stage.Stage) boxParties.getScene().getWindow();
            fr.univ.calcudoku.utils.GestionnaireJeu.chargerPartieDepuisFichier(stage, fichierJson);
        });

        return carte;
    }

    private void chargerStatistiquesProfil(String nom, ProfileManager manager) {
        Map<String, String> stats = manager.lireStatistiques(nom);

        // --- NOUVEAU : Affichage de toutes les statistiques ---
        if (lblPartiesJouees != null) lblPartiesJouees.setText("Parties jouées : " + stats.getOrDefault("parties_jouees", "0"));
        if (lblVictoires != null) lblVictoires.setText("Victoires : " + stats.getOrDefault("victoires", "0"));
        lblTempsMoyen.setText("Temps moyen : " + formatTemps(stats.getOrDefault("temps_moyen", "0")));
        
        try {
            double ratio = Double.parseDouble(stats.getOrDefault("ratio_parties", "0")) * 100;
            lblTauxReussite.setText("Taux de réussite : " + (int)ratio + "%");
        } catch(Exception e) { lblTauxReussite.setText("Taux : 0%"); }

        lblNiveauAventure.setText("Niveau aventure : " + stats.getOrDefault("progression", "1"));
        lblMeilleurScore.setText("Meilleur score : " + stats.getOrDefault("score_max", "0"));
        
        String d = stats.getOrDefault("difficulte_max", "1");
        lblDifficulteMax.setText("Difficulté max : " + (d.equals("3") ? "Difficile" : (d.equals("2") ? "Moyenne" : "Facile")));

        boolean isSombre = Boolean.parseBoolean(stats.getOrDefault("mode_sombre", "false"));
        if (radioSombre != null) radioSombre.setSelected(isSombre);
        if (radioClair != null) radioClair.setSelected(!isSombre);

        String aide = stats.getOrDefault("aide_calcul", "combinaisons");
        if (aide.equals("calculatrice") && radioProfilCalculatrice != null) radioProfilCalculatrice.setSelected(true);
        else if (radioProfilCombinaisons != null) radioProfilCombinaisons.setSelected(true);
    }
    
    private void chargerAvatar() { imgAvatar.setIconColor(MainApp.modeSombreActif ? Color.WHITE : Color.BLACK); }
    
    private String formatTemps(String s) {
        try {
            int totalSecondes = Integer.parseInt(s);
            int minutes = totalSecondes / 60;
            int secondes = totalSecondes % 60;
            // %02d permet de forcer l'affichage sur 2 chiffres (ex: 05:09 au lieu de 5:9)
            return String.format("%02d:%02d", minutes, secondes);
        } catch (Exception e) { 
            return "00:00"; 
        }
    }

    private void activerModeSombre(boolean activer) {
        MainApp.modeSombreActif = activer;
        javafx.scene.Scene sceneActuelle = boxParties.getScene();
        if (sceneActuelle != null) {
            String cssPath = getClass().getResource("/styles/sombre.css").toExternalForm();
            if (activer) {
                if (!sceneActuelle.getStylesheets().contains(cssPath)) sceneActuelle.getStylesheets().add(cssPath);
            } else {
                sceneActuelle.getStylesheets().remove(cssPath);
            }
        }

        Color couleurC = activer ? Color.WHITE : Color.BLACK;
        String couleurT = activer ? "white" : "black";

        // --- CORRECTION DE LA FLÈCHE DE RETOUR ---
        if (btnRetour != null) {
            btnRetour.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: " + couleurT + ";");
            if (btnRetour.getGraphic() instanceof FontIcon) {
                ((FontIcon) btnRetour.getGraphic()).setIconColor(couleurC);
            }
        }

        // --- CORRECTION DU BOUTON DÉCONNEXION (Version Simplifiée) ---
        if (btnDeconnexion != null) {
            if (btnDeconnexion.getGraphic() instanceof FontIcon) {
                ((FontIcon) btnDeconnexion.getGraphic()).setIconColor(couleurC);
            }
        }
        
        // --- CORRECTION DE L'AVATAR ---
        if (imgAvatar != null) {
            imgAvatar.setIconColor(couleurC);
        }
    }

    @FXML private void onRetourClick() { MainApp.changerScene(pagePrecedente); }
    @FXML private void onDeconnexionClick() { MainApp.changerScene("/fxml/accueil.fxml"); MainApp.modeSombreActif = false; }
}