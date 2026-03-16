package fr.univ.calcudoku.controller;

import com.google.gson.Gson;
import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.model.DonneesNiveau;
import fr.univ.calcudoku.utils.CacheRessources;
import fr.univ.calcudoku.utils.GestionnaireJeu;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.io.InputStreamReader;

public class MenuLibreController {

    @FXML private ToggleGroup groupeTaille;
    @FXML private ToggleGroup groupeDifficulte;
    @FXML private HBox boxGrilles;
    @FXML private ImageView imgParametres; 

    private static final Gson GSON = new Gson();

    // --- LES STYLES VISUELS DES BOUTONS ---
    private final String STYLE_NORMAL = "-fx-background-color: transparent; -fx-text-fill: #555555; -fx-cursor: hand; -fx-background-radius: 50em; -fx-pref-width: 40px; -fx-pref-height: 40px; -fx-font-size: 14px;";
    private final String STYLE_SELECTIONNE = "-fx-background-color: white; -fx-border-color: black; -fx-border-radius: 50em; -fx-background-radius: 50em; -fx-text-fill: black; -fx-cursor: hand; -fx-pref-width: 40px; -fx-pref-height: 40px; -fx-font-weight: bold; -fx-font-size: 14px;";

    @FXML
    public void initialize() {
        imgParametres.setImage(CacheRessources.getImage("/images/parametres.png"));

        configurerToggleGroup(groupeTaille);
        configurerToggleGroup(groupeDifficulte);

        rafraichirGrilles();
    }

    private void configurerToggleGroup(ToggleGroup groupe) {
        // Appliquer le style initial à tous les boutons
        for (Toggle t : groupe.getToggles()) {
            ToggleButton btn = (ToggleButton) t;
            btn.setStyle(btn.isSelected() ? STYLE_SELECTIONNE : STYLE_NORMAL);
        }

        // Écouter les clics de l'utilisateur
        groupe.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            
            // 1. Empêcher la désélection
            if (newVal == null) {
                groupe.selectToggle(oldVal); // On force à rester sélectionné
                return;
            }

            // 2. Mettre à jour l'apparence des boutons (le cercle noir)
            for (Toggle t : groupe.getToggles()) {
                ToggleButton btn = (ToggleButton) t;
                btn.setStyle(btn.isSelected() ? STYLE_SELECTIONNE : STYLE_NORMAL);
            }

            // 3. Charger les nouvelles grilles
            rafraichirGrilles();
        });
    }

    private void rafraichirGrilles() {
        boxGrilles.getChildren().clear();

        if (groupeTaille.getSelectedToggle() == null || groupeDifficulte.getSelectedToggle() == null) return;

        String taille = ((ToggleButton) groupeTaille.getSelectedToggle()).getText();
        String diff = ((ToggleButton) groupeDifficulte.getSelectedToggle()).getText();

        for (int i = 1; i <= 3; i++) {
            String baseName = "libre_" + taille + "_" + diff + "_" + i;
            String nomFichierJson = baseName + ".json";
            
            DonneesNiveau niveau = chargerDonneesNiveauRessource(nomFichierJson);

            if (niveau != null) {
                VBox carte = creerCarteNiveau(niveau, baseName, i);
                boxGrilles.getChildren().add(carte);
            } else {
                // Style pour les grilles qui n'existent pas encore
                VBox carteVide = new VBox(10);
                carteVide.setAlignment(Pos.CENTER);
                carteVide.setPrefSize(180, 200);
                carteVide.setStyle("-fx-border-color: #e0e0e0; -fx-background-color: #fafafa; -fx-border-radius: 10;");
                
                Label lbl = new Label("Grille " + i + "\nIndisponible");
                lbl.setStyle("-fx-text-alignment: center; -fx-text-fill: #aaaaaa; -fx-font-size: 13px;");
                
                carteVide.getChildren().add(lbl);
                boxGrilles.getChildren().add(carteVide);
            }
        }
    }

    private DonneesNiveau chargerDonneesNiveauRessource(String fichierJson) {
        try {
            InputStream is = getClass().getResourceAsStream("/grilles/json/" + fichierJson);
            if (is == null) return null;
            return GSON.fromJson(new InputStreamReader(is), DonneesNiveau.class);
        } catch (Exception e) {
            return null;
        }
    }

    private VBox creerCarteNiveau(DonneesNiveau niveau, String baseName, int index) {
        VBox vBox = new VBox(10);
        vBox.setAlignment(Pos.CENTER);
        vBox.setStyle("-fx-cursor: hand; -fx-padding: 10; -fx-border-color: transparent; -fx-border-radius: 10;");

        vBox.setOnMouseEntered(e -> vBox.setStyle("-fx-cursor: hand; -fx-padding: 10; -fx-background-color: #f5f5f5; -fx-border-color: #cccccc; -fx-border-radius: 10;"));
        vBox.setOnMouseExited(e -> vBox.setStyle("-fx-cursor: hand; -fx-padding: 10; -fx-border-color: transparent;"));

        ImageView vueMiniature = new ImageView();
        Image image = CacheRessources.getImage("/grilles/images/" + baseName + ".png");
        
        if (image != null) {
            vueMiniature.setImage(image);
        } else {
            vueMiniature.setStyle("-fx-background-color: lightgray;");
        }
        
        vueMiniature.setFitWidth(160);
        vueMiniature.setFitHeight(160);
        vueMiniature.setPreserveRatio(true);

        Label titre = new Label("Grille " + index);
        titre.setStyle("-fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-font-size: 14px;");

        int min = niveau.temps / 60;
        int sec = niveau.temps % 60;
        Label lblTemps = new Label(String.format("Temps cible : %d:%02d", min, sec));
        lblTemps.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 12px; -fx-text-fill: #555555;");

        vBox.getChildren().addAll(vueMiniature, titre, lblTemps);

        vBox.setOnMouseClicked(e -> {
            javafx.stage.Stage stage = (javafx.stage.Stage) boxGrilles.getScene().getWindow();
            GestionnaireJeu.chargerPartie(stage, baseName + ".json");
        });

        return vBox;
    }

    @FXML 
    private void onParametresClick() { 
        // On prévient le Profil la page a revenir
        ProfilController.pagePrecedente = "/fxml/menu_libre.fxml"; 
        MainApp.changerScene("/fxml/profil.fxml"); 
    }
    @FXML private void onRetourClick() { MainApp.changerScene("/fxml/menu.fxml"); }
}