package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.model.DonneesNiveau;
import fr.univ.calcudoku.service.ProfileManager;
import fr.univ.calcudoku.utils.CacheRessources;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.Map;

public class ProfilController {

    // mémorise la page de provenance (par défaut, le menu principal)
    public static String pagePrecedente = "/fxml/menu.fxml";

    @FXML private ImageView imgAvatar;
    @FXML private Label lblNomProfil;
    @FXML private VBox boxCentrale;
    @FXML private Label lblTempsMoyen, lblTauxReussite, lblNiveauAventure, lblDifficulteMax, lblMeilleurScore;
    @FXML private RadioButton radioSombre, radioClair;
    @FXML private HBox boxParties;
    @FXML private javafx.scene.control.ToggleGroup groupeTheme;

    @FXML
    public void initialize() {
        ProfileManager manager = MainApp.getProfileManager();
        String nomActuel = manager.getProfilActif();
        if (nomActuel == null) nomActuel = "Invité";

        lblNomProfil.setText(nomActuel);
        chargerAvatar();
        chargerStatistiquesProfil(nomActuel, manager);
        chargerPartiesSauvegardees(nomActuel);
        //ÉCOUTEUR POUR LE MODE SOMBRE/CLAIR EN DIRECT 
        groupeTheme.selectedToggleProperty().addListener((observable, ancienneValeur, nouvelleValeur) -> {
            if (nouvelleValeur == radioSombre) {
                activerModeSombre(true);
            } else if (nouvelleValeur == radioClair) {
                activerModeSombre(false);
            }
        });
    }

    private void chargerPartiesSauvegardees(String nomProfil) {
        File dossierJson = new File("profils/" + nomProfil + "/jeu/json");
        
        if (!dossierJson.exists() || !dossierJson.isDirectory()) return;

        File[] fichiersJson = dossierJson.listFiles((dir, name) -> name.endsWith(".json"));

        if (fichiersJson != null && boxParties != null) {
            boxParties.getChildren().clear();

            for (File fichier : fichiersJson) {
                DonneesNiveau niveau = fr.univ.calcudoku.utils.GestionnaireJeu.lireDonneesNiveauFichier(fichier);
                
                if (niveau != null) {
                    VBox carte = creerCartePartie(niveau, fichier);
                    boxParties.getChildren().add(carte);
                }
            }
        }
    }

    private VBox creerCartePartie(DonneesNiveau niveau, File fichierJson) {
        File dossierJeu = fichierJson.getParentFile().getParentFile(); 
        File fichierImage = new File(dossierJeu, "images/" + fichierJson.getName().replace(".json", ".png"));
        
        Image image = null;
        if (fichierImage.exists()) {
            image = new Image(fichierImage.toURI().toString());
        }

        String nomPropre = fichierJson.getName().replace(".json", "");
        int min = niveau.temps / 60;
        int sec = niveau.temps % 60;

        // la commande de la carte à Usine {factory pattern} 
        return fr.univ.calcudoku.utils.CarteUIFactory.creerCarteGrille(
            "Grille " + nomPropre, 
            String.format("Temps : %d:%02d", min, sec), 
            image, boxParties,
            () -> {
                javafx.stage.Stage stage = (javafx.stage.Stage) boxParties.getScene().getWindow();
                fr.univ.calcudoku.utils.GestionnaireJeu.chargerPartieDepuisFichier(stage, fichierJson);
            }
        );
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
        if (radioSombre != null) radioSombre.setSelected(isSombre);
        if (radioClair != null) radioClair.setSelected(!isSombre);
    }
    
    private void chargerAvatar() {
        imgAvatar.setImage(CacheRessources.getImage("/images/utilisateur.png"));
        
        // --- RESPONSIVE AVATAR ---
        if (boxCentrale != null) {
            // L'avatar fera toujours 20% de la hauteur dispo, mais ne dépassera jamais 90 pixels
            imgAvatar.fitHeightProperty().bind(javafx.beans.binding.Bindings.min(90, boxCentrale.heightProperty().multiply(0.2)));
            // On garde un carré parfait
            imgAvatar.fitWidthProperty().bind(imgAvatar.fitHeightProperty());
        }
    }
    
    private String formatTemps(String s) {
        try {
            int t = Integer.parseInt(s);
            return (t / 3600 > 0 ? t/3600 + "h " : "") + (t % 3600) / 60 + "min";
        } catch (Exception e) { return "0min"; }
    }

    private void activerModeSombre(boolean activer) {
        MainApp.modeSombreActif = activer;
        
        // 1. Appliquer le CSS pour le fond et les textes
        javafx.scene.Scene sceneActuelle = boxParties.getScene();
        if (sceneActuelle != null) {
            String cssPath = getClass().getResource("/styles/sombre.css").toExternalForm();
            if (activer) {
                if (!sceneActuelle.getStylesheets().contains(cssPath)) {
                    sceneActuelle.getStylesheets().add(cssPath);
                }
            } else {
                sceneActuelle.getStylesheets().remove(cssPath);
            }
        }
        
        // 2. --- MAGIE JAVA CENTRALISÉE POUR RENDRE L'ICÔNE BLANCHE ---
        fr.univ.calcudoku.utils.ThemeUtil.appliquerFiltreBlancSiSombre(imgAvatar);
    }

    @FXML 
    private void onRetourClick() { 
        MainApp.changerScene(pagePrecedente); 
    }
    @FXML private void onDeconnexionClick() { 
        MainApp.changerScene("/fxml/accueil.fxml"); 
        MainApp.modeSombreActif = false;
    }
}