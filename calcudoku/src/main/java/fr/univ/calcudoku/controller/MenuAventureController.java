package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.service.ProfileManager;
import fr.univ.calcudoku.utils.CacheRessources;
import fr.univ.calcudoku.utils.GestionnaireJeu;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.Cursor;

import java.util.Optional;

public class MenuAventureController {

    @FXML private HBox boxNiveaux;
    @FXML private StackPane ligneFond; // On récupère l'ancienne ligne du FXML
    @FXML private ImageView imgParametres;
    @FXML private ImageView imgReset;
    
    private final int NB_NIVEAUX_TOTAL = 5;

    @FXML
    public void initialize() {
        imgParametres.setImage(CacheRessources.getImage("/images/parametres.png"));
        imgReset.setImage(CacheRessources.getImage("/images/restart.png"));

        // On désactive l'ancienne ligne buggée du FXML (pour laisser place à nos nouveaux ponts)
        if (ligneFond != null) {
            ligneFond.setVisible(false); 
        }

        chargerProgression();
    }

    private void chargerProgression() {
        ProfileManager manager = MainApp.getProfileManager();
        String nomActuel = manager.getProfilActif();
        if (nomActuel == null) nomActuel = "Invité";

        int progression = 1;
        try {
            progression = Integer.parseInt(manager.lireStatistiques(nomActuel).getOrDefault("progression", "1"));
        } catch (Exception e) {}

        genererChemin(progression);
    }

    private void genererChemin(int progressionActuelle) {
        boxNiveaux.getChildren().clear();
        
        // On retire l'espacement automatique car ce sont nos "ponts" qui vont espacer les boutons
        boxNiveaux.setSpacing(0); 

        for (int i = 1; i <= NB_NIVEAUX_TOTAL; i++) {
            
            // --- 1. CRÉATION DU BOUTON CIRCULAIRE ---
            Button btnNiveau = new Button(String.valueOf(i));
            btnNiveau.setPrefSize(70, 70);
            btnNiveau.setMinSize(70, 70); // Force la taille pour éviter l'écrasement
            
            final int niveauId = i;

            if (i < progressionActuelle) {
                // NIVEAU DÉJÀ RÉUSSI
                btnNiveau.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-background-radius: 50em;");
                btnNiveau.setCursor(Cursor.HAND);
                btnNiveau.setOnAction(e -> lancerNiveauAventure(niveauId));

            } else if (i == progressionActuelle) {
                // NIVEAU ACTUEL
                btnNiveau.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold; -fx-background-radius: 50em; -fx-border-color: gray; -fx-border-width: 4px; -fx-border-radius: 50em;");
                btnNiveau.setCursor(Cursor.HAND);
                btnNiveau.setOnAction(e -> lancerNiveauAventure(niveauId));

            } else {
                // NIVEAU BLOQUÉ
                btnNiveau.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-font-size: 20px; -fx-border-color: black; -fx-border-width: 1px; -fx-background-radius: 50em; -fx-border-radius: 50em;");
                btnNiveau.setDisable(true); 
            }

            boxNiveaux.getChildren().add(btnNiveau);

            // --- 2. CRÉATION DU PONT (LIGNE) VERS LE NIVEAU SUIVANT ---
            // On ne met pas de pont après le dernier niveau (le niveau 5)
            if (i < NB_NIVEAUX_TOTAL) {
                Region pont = new Region();
                // Le pont fera 60 pixels de long et 6 pixels d'épaisseur
                pont.setPrefSize(60, 6);
                pont.setMinSize(60, 6);
                pont.setMaxSize(60, 6);

                if (i < progressionActuelle) {
                    // Le pont est noir si le joueur a validé cette étape
                    pont.setStyle("-fx-background-color: black;");
                } else {
                    // Le pont est gris clair pour montrer le chemin restant
                    pont.setStyle("-fx-background-color: #e0e0e0;"); 
                }
                
                boxNiveaux.getChildren().add(pont);
            }
        }
    }

    private void lancerNiveauAventure(int idNiveau) {
        String fichier = "aventure_" + idNiveau + ".json";
        javafx.stage.Stage stage = (javafx.stage.Stage) boxNiveaux.getScene().getWindow();
        GestionnaireJeu.chargerPartie(stage, fichier);
    }

    // --- ACTIONS DES BOUTONS ---

    @FXML
    private void onResetClick() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Réinitialiser l'aventure");
        alert.setHeaderText("Repartir à zéro ?");
        alert.setContentText("Voulez-vous vraiment recommencer le mode Aventure depuis le niveau 1 ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            
            ProfileManager manager = MainApp.getProfileManager();
            String nomActuel = manager.getProfilActif();
            if (nomActuel == null) nomActuel = "Invité";

            // 1. On modifie le fichier de sauvegarde
            manager.mettreAJourStatistique(nomActuel, "progression", "1");
            
            // 2. CORRECTION DU BUG : On force l'interface à se redessiner immédiatement au niveau 1
            // Ainsi, le jeu n'attend pas de relire le fichier et met tout à jour visuellement.
            genererChemin(1);
        }
    }

    @FXML
    private void onParametresClick() {
        ProfilController.pagePrecedente = "/fxml/menu_aventure.fxml";
        MainApp.changerScene("/fxml/profil.fxml");
    }

    @FXML
    private void onRetourClick() {
        MainApp.changerScene("/fxml/menu.fxml");
    }
}