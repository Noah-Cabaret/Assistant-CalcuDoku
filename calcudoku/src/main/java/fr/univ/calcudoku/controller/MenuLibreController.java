package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.model.DonneesNiveau;
import fr.univ.calcudoku.utils.CacheRessources;
import fr.univ.calcudoku.utils.GestionnaireJeu;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.paint.Color;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

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

        boolean sombre = MainApp.modeSombreActif;
        Color couleurC = sombre ? Color.WHITE : Color.BLACK;
        String couleurT = sombre ? "white" : "black";
        if (btnRetour != null) {
            btnRetour.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: " + couleurT + ";");
            if (btnRetour.getGraphic() instanceof FontIcon) {
                ((FontIcon) btnRetour.getGraphic()).setIconColor(couleurC);
            }
        }
       
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
            // Empêcher la désélection
            if (newVal == null) {
                groupe.selectToggle(oldVal);
                return;
            }

            // Mettre à jour l'apparence des boutons (le cercle noir)
            for (Toggle t : groupe.getToggles()) {
                ToggleButton btn = (ToggleButton) t;
                btn.setStyle(btn.isSelected() ? STYLE_SELECTIONNE : STYLE_NORMAL);
            }

            // Charger les nouvelles grilles
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
        // 1. Récupérer le nom du profil actif
        String nomProfil = MainApp.getProfileManager().getProfilActif();
        if (nomProfil == null) nomProfil = "Invité";

        // 2. Définir les chemins de sauvegarde potentiels
        File fichierJsonSave = new File("profils/" + nomProfil + "/parties/" + baseName + ".json");
        File fichierIniSave = new File("profils/" + nomProfil + "/parties/" + baseName + ".ini");
        File fichierImageSave = new File("profils/" + nomProfil + "/jeu/images/" + baseName + ".png");

        Image imageA_Afficher;
        String texteTemps;
        Runnable actionClic;

        // 3. Vérifier si une sauvegarde existe
        if (fichierJsonSave.exists() && fichierIniSave.exists()) {
            
            // --- CAS A : PARTIE EN COURS ---
            if (fichierImageSave.exists()) {
                imageA_Afficher = new Image(fichierImageSave.toURI().toString());
            } else {
                // Securité si l'image png a été supprimée mais pas le json
                imageA_Afficher = CacheRessources.getImage("/grilles/images/" + baseName + ".png");
            }
            
            int tempsSave = lireTempsDepuisIni(fichierIniSave);
            int min = tempsSave / 60;
            int sec = tempsSave % 60;
            texteTemps = String.format("En cours : %d:%02d", min, sec);
            
            actionClic = () -> {
                javafx.stage.Stage stage = (javafx.stage.Stage) boxGrilles.getScene().getWindow();
                GestionnaireJeu.chargerPartieDepuisFichier(stage, fichierJsonSave);
            };

        } else {
            
            // --- CAS B : NOUVELLE PARTIE ---
            imageA_Afficher = CacheRessources.getImage("/grilles/images/" + baseName + ".png");
            
            int min = niveau.temps / 60;
            int sec = niveau.temps % 60;
            texteTemps = String.format("Temps cible : %d:%02d", min, sec);
            
            actionClic = () -> {
                javafx.stage.Stage stage = (javafx.stage.Stage) boxGrilles.getScene().getWindow();
                GestionnaireJeu.chargerPartie(stage, baseName + ".json");
            };
        }

        // la commande de la carte à Usine {factory pattern} 
        return fr.univ.calcudoku.utils.CarteUIFactory.creerCarteGrille(
            "Grille " + index, 
            texteTemps, 
            imageA_Afficher, 
            boxGrilles,
            actionClic
        );
    }

    // Méthode utilitaire pour lire le temps du fichier INI
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
        // On prévient le Profil la page a revenir
        ProfilController.pagePrecedente = "/fxml/menu_libre.fxml"; 
        MainApp.changerScene("/fxml/profil.fxml"); 
    }
    
    @FXML 
    private void onRetourClick() { 
        MainApp.changerScene("/fxml/menu.fxml"); 
    }
}