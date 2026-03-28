package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.service.ProfileManager;
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
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.paint.Color;

import java.util.Optional;

public class MenuAventureController {

    @FXML private HBox boxNiveaux;
    @FXML private StackPane ligneFond; // récupère l'ancienne ligne du FXML
    @FXML private FontIcon imgParametres;
    @FXML private FontIcon imgReset;
    
    private final int NB_NIVEAUX_TOTAL = 5;

    @FXML
    public void initialize() {
        Color couleurIcone = MainApp.modeSombreActif ? Color.WHITE : Color.BLACK;
        imgParametres.setIconColor(couleurIcone);
        imgReset.setIconColor(couleurIcone);

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
        boxNiveaux.setSpacing(0); 

        for (int i = 1; i <= NB_NIVEAUX_TOTAL; i++) {
            
            Button btnNiveau = new Button(String.valueOf(i));
            final int niveauId = i;

            // RESPONSIVE : Rendre le bouton élastique
            btnNiveau.setMinSize(0, 0); // Autorise le rétrécissement total
            
            // La largeur s'adapte (environ 12% de l'écran dispo) mais ne dépasse jamais 70 pixels
            btnNiveau.prefWidthProperty().bind(javafx.beans.binding.Bindings.min(70, boxNiveaux.widthProperty().divide(8)));
            
            // La hauteur est exactement égale à la largeur pour forcer un rond parfait
            btnNiveau.prefHeightProperty().bind(btnNiveau.prefWidthProperty());

            // Le texte doit aussi rétrécir, Dynamiquement lié à la largeur
            javafx.beans.binding.StringExpression styleDynamique;

            if (i < progressionActuelle) {
                // NIVEAU DÉJÀ RÉUSSI
                styleDynamique = javafx.beans.binding.Bindings.concat(
                    "-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50em; -fx-font-size: ", 
                    btnNiveau.widthProperty().divide(3), "px;"
                );
                btnNiveau.styleProperty().bind(styleDynamique);
                btnNiveau.setCursor(Cursor.HAND);
                btnNiveau.setOnAction(e -> lancerNiveauAventure(niveauId));

            } else if (i == progressionActuelle) {
                // NIVEAU ACTUEL
                styleDynamique = javafx.beans.binding.Bindings.concat(
                    "-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50em; -fx-border-color: gray; -fx-border-width: 4px; -fx-border-radius: 50em; -fx-font-size: ", 
                    btnNiveau.widthProperty().divide(2.5), "px;"
                );
                btnNiveau.styleProperty().bind(styleDynamique);
                btnNiveau.setCursor(Cursor.HAND);
                btnNiveau.setOnAction(e -> lancerNiveauAventure(niveauId));

            } else {
                // NIVEAU BLOQUÉ
                styleDynamique = javafx.beans.binding.Bindings.concat(
                    "-fx-background-color: white; -fx-text-fill: black; -fx-border-color: black; -fx-border-width: 1px; -fx-background-radius: 50em; -fx-border-radius: 50em; -fx-font-size: ", 
                    btnNiveau.widthProperty().divide(3), "px;"
                );
                btnNiveau.styleProperty().bind(styleDynamique);
                btnNiveau.setDisable(true); 
            }

            boxNiveaux.getChildren().add(btnNiveau);

            // RESPONSIVE : Le pont (la ligne entre les niveaux)
            if (i < NB_NIVEAUX_TOTAL) {
                Region pont = new Region();
                pont.setMinSize(0, 0); 
                
                pont.prefWidthProperty().bind(javafx.beans.binding.Bindings.min(60, boxNiveaux.widthProperty().divide(10)));
                pont.prefHeightProperty().bind(btnNiveau.heightProperty().divide(12));

                // POUR BLOQUER L'ÉTIREMENT GÉANT 
                pont.maxHeightProperty().bind(btnNiveau.heightProperty().divide(12));
                pont.maxWidthProperty().bind(javafx.beans.binding.Bindings.min(60, boxNiveaux.widthProperty().divide(10)));

                if (i < progressionActuelle) {
                    pont.setStyle("-fx-background-color: black;");
                } else {
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

    //ACTIONS DES BOUTONS

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

            // On modifie le fichier de sauvegarde
            manager.mettreAJourStatistique(nomActuel, "progression", "1");
            
            // On force l'interface à se redessiner immédiatement au niveau 1
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