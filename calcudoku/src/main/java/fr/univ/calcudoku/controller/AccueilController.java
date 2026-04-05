package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.service.ProfileManager;
import fr.univ.calcudoku.utils.Constantes;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import org.kordamp.ikonli.javafx.FontIcon; 

import java.util.Optional;

/**
 * Contrôleur de l'écran d'accueil de l'application.
 * Gère l'affichage, la sélection, la création et la suppression des profils utilisateurs.
 */
public class AccueilController {

    @FXML
    private FlowPane boxProfils;

    private ProfileManager manager;

    /**
     * Méthode d'initialisation appelée automatiquement après le chargement du fichier FXML.
     * Récupère le gestionnaire de profils et rafraîchit l'affichage.
     */
    @FXML
    public void initialize() {
        manager = MainApp.getProfileManager();
        rafraichirAffichage();
    }

    /**
     * Rafraîchit l'affichage de la liste des profils dans le conteneur principal.
     * Efface les éléments existants et recrée les cartes pour chaque profil,
     * ainsi que le bouton permettant d'ajouter un nouveau profil.
     */
    private void rafraichirAffichage() {
        boxProfils.getChildren().clear();

        for (String nom : manager.listerProfils()) {
            VBox carteProfil = creerCarteProfil(nom);
            boxProfils.getChildren().add(carteProfil);
        }

        boxProfils.getChildren().add(creerCarteAjout());
    }

    /**
     * Crée une carte visuelle représentant un profil utilisateur existant.
     * 
     * @param nom Le nom du profil à afficher.
     * @return Un conteneur VBox représentant la carte du profil interactif.
     */
    private VBox creerCarteProfil(String nom) {
        VBox carte = new VBox(10);
        carte.setAlignment(Pos.CENTER);
        carte.setMaxHeight(VBox.USE_PREF_SIZE);
        carte.setStyle("-fx-cursor: hand; -fx-padding: 15; -fx-background-color: transparent; -fx-background-radius: 10;");

        FontIcon iconProfil = new FontIcon(Constantes.ICONE_UTILISATEUR);
        iconProfil.setIconSize(80);

        Label boutonX = new Label("X");
        boutonX.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-background-radius: 50%; -fx-min-width: 20px; -fx-min-height: 20px; -fx-alignment: center; -fx-font-weight: bold; -fx-font-size: 10px;");
        boutonX.setVisible(false);

        boutonX.setOnMouseClicked(e -> {
            e.consume(); 
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Supprimer le profil");
            alert.setHeaderText("Supprimer " + nom + " ?");
            alert.setContentText("Toutes les sauvegardes seront perdues définitivement.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (manager.supprimerProfil(nom)) {
                    rafraichirAffichage(); 
                }
            }
        });

        StackPane conteneurImage = new StackPane();
        StackPane.setAlignment(boutonX, Pos.TOP_RIGHT);
        StackPane.setMargin(boutonX, new Insets(-5, -5, 0, 0)); 
        conteneurImage.getChildren().addAll(iconProfil, boutonX);

        Label labelNom = new Label(nom);
        labelNom.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        carte.getChildren().addAll(conteneurImage, labelNom);

        carte.setOnMouseEntered(e -> {
            carte.setStyle("-fx-cursor: hand; -fx-background-color: #e6e6e6; -fx-padding: 15; -fx-background-radius: 10;");
            boutonX.setVisible(true); 
        });

        carte.setOnMouseExited(e -> {
            carte.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-padding: 15; -fx-background-radius: 10;");
            boutonX.setVisible(false); 
        });

        carte.setOnMouseClicked(e -> {
            manager.chargerProfil(nom);
            
            java.util.Map<String, String> stats = manager.lireStatistiques(nom);
            MainApp.setModeSombre(Boolean.parseBoolean(stats.getOrDefault(Constantes.OPTION_MODE_SOMBRE, "false")));
            
            MainApp.changerScene(Constantes.VUE_MENU);
        });

        return carte;
    }

    /**
     * Crée une carte visuelle servant de bouton pour ajouter un nouveau profil.
     * 
     * @return Un conteneur VBox représentant la carte d'ajout.
     */
    private VBox creerCarteAjout() {
        VBox carte = new VBox(10);
        carte.setAlignment(Pos.CENTER);
        carte.setMaxHeight(VBox.USE_PREF_SIZE);
        carte.setStyle("-fx-cursor: hand; -fx-padding: 15; -fx-background-color: transparent; -fx-background-radius: 10;");

        FontIcon iconPlus = new FontIcon(Constantes.ICONE_PLUS);
        iconPlus.setIconSize(80);

        Label labelAjout = new Label("Ajouter");
        labelAjout.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        carte.getChildren().addAll(iconPlus, labelAjout);

        carte.setOnMouseEntered(e -> carte.setStyle("-fx-cursor: hand; -fx-background-color: #e6e6e6; -fx-padding: 15; -fx-background-radius: 10;"));
        carte.setOnMouseExited(e -> carte.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-padding: 15; -fx-background-radius: 10;"));
        carte.setOnMouseClicked(e -> gererCreationProfil());

        return carte;
    }

    /**
     * Gère l'ouverture de la fenêtre contextuelle (popup) permettant la création d'un nouveau profil.
     * Affiche un champ de saisie pour le pseudo et gère les erreurs si le champ est vide
     * ou si le nom de profil existe déjà.
     */
    private void gererCreationProfil() {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL); 
        popup.setTitle("Nouveau Profil");

        VBox layout = new VBox(20); 
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-border-color: #ccc; -fx-border-width: 1;");

        Label titre = new Label("Calcudoku");
        titre.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: black; -fx-border-color: transparent transparent transparent; -fx-border-width: 0 0 2 0;");

        FontIcon iconPopup = new FontIcon(Constantes.ICONE_UTILISATEUR);
        iconPopup.setIconSize(60);

        Label msgErreur = new Label("Ce nom de profil est déjà pris");
        msgErreur.setStyle("-fx-text-fill: red;"); 
        msgErreur.setVisible(false); 

        TextField champPseudo = new TextField();
        champPseudo.setPromptText("Saisir votre pseudo"); 
        champPseudo.setFocusTraversable(false); 
        champPseudo.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 10; -fx-font-size: 14px; -fx-alignment: CENTER;");
        champPseudo.setMaxWidth(250);

        champPseudo.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String nomSaisi = champPseudo.getText().trim();
                
                if (!nomSaisi.isEmpty()) {
                    boolean succes = manager.creerProfil(nomSaisi);
                    
                    if (succes) {
                        rafraichirAffichage(); 
                        popup.close();         
                    } else {
                        champPseudo.setStyle("-fx-background-color: #fff0f0; -fx-border-color: red; -fx-border-radius: 5; -fx-padding: 10; -fx-font-size: 14px; -fx-alignment: CENTER;");
                        champPseudo.setText("");
                        msgErreur.setVisible(true);
                        layout.requestFocus();
                    }
                } else {
                    champPseudo.setStyle("-fx-background-color: #fff0f0; -fx-border-color: red; -fx-border-radius: 5; -fx-padding: 10; -fx-font-size: 14px; -fx-alignment: CENTER;");
                    msgErreur.setText("Veuillez saisir votre pseudo");
                    msgErreur.setVisible(true);
                    layout.requestFocus();
                }
            }
        });

        champPseudo.setOnKeyTyped(e -> {
            if (msgErreur.isVisible()) {
                champPseudo.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 10; -fx-font-size: 14px; -fx-alignment: CENTER;");
                msgErreur.setVisible(false);
            }
        });

        layout.getChildren().addAll(titre, iconPopup, champPseudo, msgErreur);

        Scene scene = new Scene(layout, 400, 300); 
        popup.setScene(scene);
        popup.setResizable(false); 
        popup.showAndWait(); 
    }
}