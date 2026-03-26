package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.service.ProfileManager;
import fr.univ.calcudoku.utils.CacheRessources;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;

import java.util.Optional;

public class AccueilController {

    @FXML
    private FlowPane boxProfils;

    private ProfileManager manager;

    @FXML
    public void initialize() {
        manager = MainApp.getProfileManager();
        rafraichirAffichage();
    }

    private void rafraichirAffichage() {
        boxProfils.getChildren().clear();

        // CHARGEMENT OPTIMISÉ DE L'IMAGE UTILISATEUR
        Image avatarParDefaut = CacheRessources.getImage("/images/utilisateur.png");

        // Créer une carte pour chaque profil avec l'image chargée
        for (String nom : manager.listerProfils()) {
            VBox carteProfil = creerCarteProfil(nom, avatarParDefaut);
            boxProfils.getChildren().add(carteProfil);
        }

        // Ajouter le bouton "+"
        boxProfils.getChildren().add(creerCarteAjout());
    }

    private VBox creerCarteProfil(String nom, Image img) {
        VBox carte = new VBox(10);
        carte.setAlignment(Pos.CENTER);
        carte.setMaxHeight(VBox.USE_PREF_SIZE);
        carte.setStyle("-fx-cursor: hand; -fx-padding: 15; -fx-background-color: transparent; -fx-background-radius: 10;");

        // CRÉATION DE L'IMAGE
        ImageView view = new ImageView();
        if (img != null) {
            view.setImage(img);
        } else {
            view.setStyle("-fx-background-color: grey;");
        }
        view.setFitWidth(80);
        view.setFitHeight(80);
        view.setPreserveRatio(true);

        // CRÉATION DU BOUTON X
        Label boutonX = new Label("X");
        boutonX.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-background-radius: 50%; -fx-min-width: 20px; -fx-min-height: 20px; -fx-alignment: center; -fx-font-weight: bold; -fx-font-size: 10px;");
        boutonX.setVisible(false);

        // ACTION DE SUPPRESSION
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

        // SUPERPOSITION 
        StackPane conteneurImage = new StackPane();
        StackPane.setAlignment(boutonX, Pos.TOP_RIGHT);
        StackPane.setMargin(boutonX, new Insets(-5, -5, 0, 0)); 
        conteneurImage.getChildren().addAll(view, boutonX);

        // TEXTE
        Label labelNom = new Label(nom);
        labelNom.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        carte.getChildren().addAll(conteneurImage, labelNom);

        // GESTION DU HOVER
        carte.setOnMouseEntered(e -> {
            carte.setStyle("-fx-cursor: hand; -fx-background-color: #e6e6e6; -fx-padding: 15; -fx-background-radius: 10;");
            boutonX.setVisible(true); 
        });

        carte.setOnMouseExited(e -> {
            carte.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-padding: 15; -fx-background-radius: 10;");
            boutonX.setVisible(false); 
        });

        // Clic sur la carte
        carte.setOnMouseClicked(e -> {
            manager.chargerProfil(nom);
            MainApp.changerScene("/fxml/menu.fxml");
        });

        return carte;
    }

    private VBox creerCarteAjout() {
        VBox carte = new VBox(10);
        carte.setAlignment(Pos.CENTER);
        carte.setMaxHeight(VBox.USE_PREF_SIZE);
        carte.setStyle("-fx-cursor: hand; -fx-padding: 15; -fx-background-color: transparent; -fx-background-radius: 10;");

        // IMAGE "+" OPTIMISÉE
        ImageView viewPlus = new ImageView();
        viewPlus.setImage(CacheRessources.getImage("/images/plus-symbole-noir.png"));

        viewPlus.setFitWidth(80);  
        viewPlus.setFitHeight(80);
        viewPlus.setPreserveRatio(true);
        viewPlus.setOpacity(1.0); 

        Label labelAjout = new Label("Ajouter");
        labelAjout.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        carte.getChildren().addAll(viewPlus, labelAjout);

        carte.setOnMouseEntered(e -> carte.setStyle("-fx-cursor: hand; -fx-background-color: #e6e6e6; -fx-padding: 15; -fx-background-radius: 10;"));
        carte.setOnMouseExited(e -> carte.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-padding: 15; -fx-background-radius: 10;"));
        carte.setOnMouseClicked(e -> gererCreationProfil());

        return carte;
    }

    private void gererCreationProfil() {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL); 
        popup.setTitle("Nouveau Profil");

        VBox layout = new VBox(20); 
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-border-color: #ccc; -fx-border-width: 1;");

        Label titre = new Label("Calcudoku");
        titre.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: black; -fx-border-color: transparent transparent transparent; -fx-border-width: 0 0 2 0;");

        // IMAGE AVATAR OPTIMISÉE POUR LE POPUP 
        ImageView iconView = new ImageView();
        iconView.setImage(CacheRessources.getImage("/images/utilisateur.png"));
        
        iconView.setFitWidth(60);
        iconView.setFitHeight(60);
        iconView.setPreserveRatio(true);

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
                    
                }else{
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

        layout.getChildren().addAll(titre, iconView, champPseudo, msgErreur);

        Scene scene = new Scene(layout, 400, 300); 
        popup.setScene(scene);
        popup.setResizable(false); 
        popup.showAndWait(); 
    }
}