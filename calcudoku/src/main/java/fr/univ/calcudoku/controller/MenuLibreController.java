package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.model.DonneesNiveau;
import fr.univ.calcudoku.utils.CacheRessources;
import fr.univ.calcudoku.utils.Constantes;
import fr.univ.calcudoku.utils.GestionnaireJeu;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.Scanner;

public class MenuLibreController {

    @FXML private ToggleGroup groupeTaille;
    @FXML private ToggleGroup groupeDifficulte;
    @FXML private HBox boxGrilles;
    @FXML private FontIcon imgParametres;
    @FXML private Button btnRetour;

    // LES STYLES VISUELS DES BOUTONS
    private final String STYLE_NORMAL = "-fx-background-color: transparent; -fx-text-fill: #555555; -fx-cursor: hand; -fx-background-radius: 50em; -fx-pref-width: 40px; -fx-pref-height: 40px; -fx-font-size: 14px;";
    private final String STYLE_SELECTIONNE = "-fx-background-color: white; -fx-border-color: black; -fx-border-radius: 50em; -fx-background-radius: 50em; -fx-text-fill: black; -fx-cursor: hand; -fx-pref-width: 40px; -fx-pref-height: 40px; -fx-font-weight: bold; -fx-font-size: 14px;";

    @FXML
    public void initialize() {

        imgParametres.setIconColor(MainApp.modeSombreActif ? Color.WHITE : Color.BLACK);
        configurerToggleGroup(groupeTaille);
        configurerToggleGroup(groupeDifficulte);

        if (btnRetour != null) {
            btnRetour.setStyle("-fx-background-color: transparent; -fx-cursor: hand;"); 
            
            if (btnRetour.getGraphic() instanceof FontIcon) {
                ((FontIcon) btnRetour.getGraphic()).setIconColor(MainApp.modeSombreActif ? Color.WHITE : Color.BLACK);
            }
        }

        rafraichirGrilles();
    }

    private void configurerToggleGroup(ToggleGroup groupe) {
        for (Toggle t : groupe.getToggles()) {
            ToggleButton btn = (ToggleButton) t;
            btn.setStyle(btn.isSelected() ? STYLE_SELECTIONNE : STYLE_NORMAL);
        }

        groupe.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                groupe.selectToggle(oldVal);
                return;
            }

            for (Toggle t : groupe.getToggles()) {
                ToggleButton btn = (ToggleButton) t;
                btn.setStyle(btn.isSelected() ? STYLE_SELECTIONNE : STYLE_NORMAL);
            }

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
            
            DonneesNiveau niveau = GestionnaireJeu.lireDonneesNiveauRessource(nomFichierJson);

            if (niveau != null) {
                VBox carte = creerCarteNiveau(niveau, baseName, i);
                boxGrilles.getChildren().add(carte);
            } else {
                VBox carteVide = fr.univ.calcudoku.utils.CarteUIFactory.creerCarteVide("Grille " + i, boxGrilles);
                boxGrilles.getChildren().add(carteVide);
            }
        }
    }

    private VBox creerCarteNiveau(DonneesNiveau niveau, String baseName, int index) {
        String nomProfil = MainApp.getProfileManager().getProfilActif();

        File fichierJsonSave = new File("profils/" + nomProfil + "/parties/" + baseName + ".json");
        File fichierIniSave = new File("profils/" + nomProfil + "/parties/" + baseName + ".ini");
        File fichierImageSave = new File("profils/" + nomProfil + "/jeu/images/" + baseName + ".png");

        Image imageA_Afficher;
        String texteTemps;
        Runnable actionClic;

        // Récupération du record de la grille
        fr.univ.calcudoku.save.Record rec = fr.univ.calcudoku.save.GestionnaireRecords.getRecord(baseName);

        if (fichierJsonSave.exists() && fichierIniSave.exists()) {
            
            if (fichierImageSave.exists()) {
                imageA_Afficher = new Image(fichierImageSave.toURI().toString());
            } else {
                imageA_Afficher = CacheRessources.getImage("/grilles/images/" + baseName + ".png");
            }
            
            int tempsSave = lireTempsDepuisIni(fichierIniSave);
            texteTemps = String.format("En cours : %02d:%02d", tempsSave / 60, tempsSave % 60);
            
            // On ajoute le record en dessous s'il existe
            if (rec != null) {
                texteTemps += String.format("\n🏆 %s : %d pts (%02d:%02d)", rec.joueur, rec.score, rec.temps / 60, rec.temps % 60);
            }
            
            actionClic = () -> {
                javafx.stage.Stage stage = (javafx.stage.Stage) boxGrilles.getScene().getWindow();
                GestionnaireJeu.chargerPartieDepuisFichier(stage, fichierJsonSave);
            };

        } else {
            
            imageA_Afficher = CacheRessources.getImage("/grilles/images/" + baseName + ".png");
            
            // --- MODIFICATION ICI : On enlève le temps cible ---
            if (rec != null) {
                texteTemps = String.format("🏆 %s : %d pts (%02d:%02d)", rec.joueur, rec.score, rec.temps / 60, rec.temps % 60);
            } else {
                texteTemps = "Nouvelle partie";
            }
            
            actionClic = () -> {
                javafx.stage.Stage stage = (javafx.stage.Stage) boxGrilles.getScene().getWindow();
                GestionnaireJeu.chargerPartie(stage, baseName + ".json");
            };
        }

        return fr.univ.calcudoku.utils.CarteUIFactory.creerCarteGrille(
            "Grille " + index, 
            texteTemps, 
            imageA_Afficher, 
            boxGrilles,
            actionClic
        );
    }

    private int lireTempsDepuisIni(File fichierIni) {
        if (!fichierIni.exists()) return 0;
        try (Scanner sc = new Scanner(fichierIni)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.startsWith("temps=")) {
                    String[] parts = line.split("=");
                    if (parts.length > 1) {
                        return (int) Double.parseDouble(parts[1].trim());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @FXML 
    private void onParametresClick() { 
        ProfilController.pagePrecedente = Constantes.VUE_MENU_LIBRE; 
        MainApp.changerScene(Constantes.VUE_PROFIL); 
    }
    
    @FXML 
    private void onRetourClick() { 
        MainApp.changerScene(Constantes.VUE_MENU); 
    }
}