package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.model.DonneesNiveau;
import fr.univ.calcudoku.service.ProfileManager;
import fr.univ.calcudoku.utils.CacheRessources;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
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
        // --- ÉCOUTEUR POUR LE MODE SOMBRE/CLAIR EN DIRECT ---
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
                // On délègue la lecture du fichier au GestionnaireJeu !
                DonneesNiveau niveau = fr.univ.calcudoku.utils.GestionnaireJeu.lireDonneesNiveauFichier(fichier);
                
                if (niveau != null) {
                    VBox carte = creerCartePartie(niveau, fichier);
                    boxParties.getChildren().add(carte);
                }
            }
        }
    }

    private VBox creerCartePartie(DonneesNiveau niveau, File fichierJson) {
        VBox vBox = new VBox(10);
        vBox.setAlignment(Pos.CENTER);
        
        // --- 1. Autoriser la VBox à rétrécir à l'infini
        vBox.setMinSize(0, 0); 

        ImageView vueMiniature = new ImageView();
        
        // On déduit le dossier "images" à partir de l'emplacement du fichier JSON
        // fichierJson est dans "jeu/json/", donc getParentFile().getParentFile() remonte à "jeu/"
        File dossierJeu = fichierJson.getParentFile().getParentFile(); 
        File fichierImage = new File(dossierJeu, "images/" + fichierJson.getName().replace(".json", ".png"));
        
        // On charge l'image depuis le disque dur (en rajoutant "file:" devant le chemin)
        if (fichierImage.exists()) {
            Image image = new Image(fichierImage.toURI().toString());
            vueMiniature.setImage(image);
        } else {
            // S'il n'y a pas d'image de sauvegarde, on met un fond gris par défaut
            vueMiniature.setStyle("-fx-background-color: lightgray;");
        }
        
        // --- 2. RESPONSIVE : Les images suivent la hauteur ET la largeur ! ---
        // On calcule la taille idéale en hauteur (55% de la boîte)
        javafx.beans.binding.NumberBinding tailleHauteur = boxParties.heightProperty().multiply(0.75);
        
        // On calcule la taille idéale en largeur (environ 28% de la boîte pour en afficher ~3 sans déborder)
        javafx.beans.binding.NumberBinding tailleLargeur = boxParties.widthProperty().divide(4.0);
        
        // L'image prendra TOUJOURS la plus petite des deux valeurs pour ne jamais déborder !
        javafx.beans.binding.NumberBinding tailleMax = javafx.beans.binding.Bindings.min(tailleHauteur, tailleLargeur);
        
        vueMiniature.fitHeightProperty().bind(tailleMax);
        vueMiniature.fitWidthProperty().bind(tailleMax); // Reste un carré parfait
        vueMiniature.setPreserveRatio(true);

        String nomPropre = fichierJson.getName().replace(".json", "");
        Label titre = new Label("Grille " + nomPropre);
        titre.setStyle("-fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-font-size: 11px;");

        int min = niveau.temps / 60;
        int sec = niveau.temps % 60;
        Label lblTemps = new Label(String.format("Temps : %d:%02d", min, sec));
        lblTemps.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 10px; -fx-text-fill: #333333;");

        vBox.getChildren().addAll(vueMiniature, titre, lblTemps);

        // --- NOUVEAU CODE À AJOUTER ICI ---

        // 1. Ajouter l'effet visuel (curseur en forme de main, fond gris et Bords + Fonds Arrondis !)
        String styleNormal = "-fx-cursor: hand; -fx-padding: 10; -fx-background-color: transparent; -fx-border-color: transparent; -fx-border-radius: 10; -fx-background-radius: 10;";
        String styleHover = "-fx-cursor: hand; -fx-padding: 10; -fx-background-color: #f5f5f5; -fx-border-color: #cccccc; -fx-border-radius: 10; -fx-background-radius: 10;";

        vBox.setStyle(styleNormal);
        vBox.setOnMouseEntered(e -> vBox.setStyle(styleHover));
        vBox.setOnMouseExited(e -> vBox.setStyle(styleNormal));

        // 2. Ajouter l'action au clic
        vBox.setOnMouseClicked(e -> {
            javafx.stage.Stage stage = (javafx.stage.Stage) boxParties.getScene().getWindow();
            fr.univ.calcudoku.utils.GestionnaireJeu.chargerPartieDepuisFichier(stage, fichierJson);
        });

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