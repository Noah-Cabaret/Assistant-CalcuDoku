package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.service.ProfileManager;
import fr.univ.calcudoku.utils.Constantes;
import fr.univ.calcudoku.utils.GestionnaireJeu;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.Cursor;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.paint.Color;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.Optional;

/**
 * Contrôleur pour le menu du mode Aventure.
 * Gère l'affichage du chemin de progression des niveaux, le lancement des parties
 * et la réinitialisation de la progression du joueur.
 */
public class MenuAventureController {

    /** Conteneur pour les boutons de niveau et les ponts de liaison. */
    @FXML private HBox boxNiveaux;
    /** Pane de fond pour la ligne de progression (actuellement non utilisé). */
    @FXML private StackPane ligneFond; 
    /** Icône pour le bouton des paramètres. */
    @FXML private FontIcon imgParametres;
    /** Icône pour le bouton de réinitialisation. */
    @FXML private FontIcon imgReset;
    /** Bouton pour retourner au menu principal. */
    @FXML private Button btnRetour;
    
    /** Nombre total de niveaux dans le mode Aventure. */
    private final int NB_NIVEAUX_TOTAL = 5;

    /**
     * Méthode d'initialisation appelée après le chargement du FXML.
     * Configure les icônes en fonction du thème et charge la progression du joueur.
     */
    @FXML
    public void initialize() {
        Color couleurIcone = MainApp.modeSombreActif ? Color.WHITE : Color.BLACK;
        imgParametres.setIconColor(couleurIcone);
        imgReset.setIconColor(couleurIcone);

        if (ligneFond != null) {
            ligneFond.setVisible(false); 
        }

        boolean sombre = MainApp.modeSombreActif;
        Color couleurC = sombre ? Color.WHITE : Color.BLACK;
        String couleurT = sombre ? "white" : "black";
        if (btnRetour != null) {
            btnRetour.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: " + couleurT + ";");
            if (btnRetour.getGraphic() instanceof FontIcon) {
                ((FontIcon) btnRetour.getGraphic()).setIconColor(couleurC);
            }
        }

        chargerProgression();
    }

    /**
     * Charge la progression actuelle du joueur depuis son profil
     * et lance la génération de l'affichage du chemin des niveaux.
     */
    private void chargerProgression() {
        ProfileManager manager = MainApp.getProfileManager();
        String nomActuel = manager.getProfilActif();

        int progression = 1;
        try {
            progression = Integer.parseInt(manager.lireStatistiques(nomActuel).getOrDefault(Constantes.STAT_PROGRESSION, "1"));
        } catch (Exception e) {}

        genererChemin(progression);
    }

    /**
     * Génère et affiche dynamiquement le chemin des niveaux (boutons et ponts).
     * Le style des éléments dépend de la progression du joueur (terminé, actuel, verrouillé).
     *
     * @param progressionActuelle Le niveau le plus élevé atteint par le joueur.
     */
    private void genererChemin(int progressionActuelle) {
        boxNiveaux.getChildren().clear();
        boxNiveaux.setSpacing(0); 

        for (int i = 1; i <= NB_NIVEAUX_TOTAL; i++) {
            
            Button btnNiveau = new Button(String.valueOf(i));
            final int niveauId = i;

            btnNiveau.setMinSize(0, 0); 
            btnNiveau.prefWidthProperty().bind(javafx.beans.binding.Bindings.min(70, boxNiveaux.widthProperty().divide(8)));
            btnNiveau.prefHeightProperty().bind(btnNiveau.prefWidthProperty());

            javafx.beans.binding.StringExpression styleDynamique;

            if (i < progressionActuelle) {
                styleDynamique = javafx.beans.binding.Bindings.concat(
                    "-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50em; -fx-font-size: ", 
                    btnNiveau.widthProperty().divide(3), "px;"
                );
                btnNiveau.styleProperty().bind(styleDynamique);
                btnNiveau.setCursor(Cursor.HAND);
                btnNiveau.setOnAction(e -> lancerNiveauAventure(niveauId));

            } else if (i == progressionActuelle) {
                styleDynamique = javafx.beans.binding.Bindings.concat(
                    "-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50em; -fx-border-color: gray; -fx-border-width: 4px; -fx-border-radius: 50em; -fx-font-size: ", 
                    btnNiveau.widthProperty().divide(2.5), "px;"
                );
                btnNiveau.styleProperty().bind(styleDynamique);
                btnNiveau.setCursor(Cursor.HAND);
                btnNiveau.setOnAction(e -> lancerNiveauAventure(niveauId));

            } else {
                styleDynamique = javafx.beans.binding.Bindings.concat(
                    "-fx-background-color: white; -fx-text-fill: black; -fx-border-color: black; -fx-border-width: 1px; -fx-background-radius: 50em; -fx-border-radius: 50em; -fx-font-size: ", 
                    btnNiveau.widthProperty().divide(3), "px;"
                );
                btnNiveau.styleProperty().bind(styleDynamique);
                btnNiveau.setDisable(true); 
            }

            boxNiveaux.getChildren().add(btnNiveau);

            if (i < NB_NIVEAUX_TOTAL) {
                Region pont = new Region();
                pont.setMinSize(0, 0); 
                
                pont.prefWidthProperty().bind(javafx.beans.binding.Bindings.min(60, boxNiveaux.widthProperty().divide(10)));
                pont.prefHeightProperty().bind(btnNiveau.heightProperty().divide(12));

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

    /**
     * Lance une partie du mode Aventure.
     * Charge une partie sauvegardée si elle existe, sinon commence un nouveau niveau.
     *
     * @param idNiveau L'identifiant du niveau à lancer.
     */
    private void lancerNiveauAventure(int idNiveau) {
        String nomGrille = "aventure_" + idNiveau;
        String nomProfil = MainApp.getProfileManager().getProfilActif();

        File fichierJsonSave = new File("profils/" + nomProfil + "/parties/aventure/" + nomGrille + ".json");
        File fichierIniSave = new File("profils/" + nomProfil + "/parties/aventure/" + nomGrille + ".ini");

        javafx.stage.Stage stage = (javafx.stage.Stage) boxNiveaux.getScene().getWindow();

        if (fichierJsonSave.exists() && fichierIniSave.exists()) {
            GestionnaireJeu.chargerPartieDepuisFichier(stage, fichierJsonSave);
        } else {
            GestionnaireJeu.chargerPartie(stage, nomGrille + ".json");
        }
    }

    /**
     * Gère le clic sur le bouton de réinitialisation.
     * Affiche une alerte de confirmation avant de supprimer les sauvegardes du mode Aventure et de réinitialiser la progression.
     */
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

            // Supprimer toutes les sauvegardes aventure (niveaux 1 à NB_NIVEAUX_TOTAL)
            for (int i = 1; i <= NB_NIVEAUX_TOTAL; i++) {
                String base = "profils/" + nomActuel + "/parties/aventure/aventure_" + i;
                java.io.File fJson = new java.io.File(base + ".json");
                java.io.File fIni = new java.io.File(base + ".ini");
                if (fJson.exists()) fJson.delete();
                if (fIni.exists()) fIni.delete();
            }

            manager.mettreAJourStatistique(nomActuel, Constantes.STAT_PROGRESSION, "1");
            genererChemin(1);
        }
    }

    /**
     * Gère le clic sur le bouton des paramètres, redirigeant vers l'écran de profil.
     */
    @FXML
    private void onParametresClick() {
        ProfilController.pagePrecedente = Constantes.VUE_MENU_AVENTURE;
        MainApp.changerScene(Constantes.VUE_PROFIL);
    }

    /**
     * Gère le clic sur le bouton de retour, redirigeant vers le menu principal.
     */
    @FXML
    private void onRetourClick() {
        MainApp.changerScene(Constantes.VUE_MENU);
    }
}