package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.service.ProfileManager;
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

import java.io.InputStream;
import java.util.Optional;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;

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

        // CHARGEMENT DE L'IMAGE UTILISATEUR
        Image avatarParDefaut = null;
        try {
            // Le "/" = "racine du dossier resources"
            InputStream is = getClass().getResourceAsStream("/images/utilisateur.png");
            if (is != null) {
                avatarParDefaut = new Image(is);
            } else {
                System.err.println(" Erreur : Image utilisateur.png introuvable !");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        // Style : Rond rouge, texte blanc, petit
        boutonX.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-background-radius: 50%; -fx-min-width: 20px; -fx-min-height: 20px; -fx-alignment: center; -fx-font-weight: bold; -fx-font-size: 10px;");
        
        // Au départ, il est caché
        boutonX.setVisible(false);

        // ACTION DE SUPPRESSION
        boutonX.setOnMouseClicked(e -> {
            // e.consume() empêche le clic de traverser vers la carte (et donc de lancer le jeu)
            e.consume(); 
            
            // Confirmation avant suppression
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Supprimer le profil");
            alert.setHeaderText("Supprimer " + nom + " ?");
            alert.setContentText("Toutes les sauvegardes seront perdues définitivement.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (manager.supprimerProfil(nom)) {
                    rafraichirAffichage(); // On recharge la liste
                }
            }
        });

        // SUPERPOSITION 
        StackPane conteneurImage = new StackPane();
        // On aligne le X en haut à droite
        StackPane.setAlignment(boutonX, Pos.TOP_RIGHT);
        // On ajoute un petit décalage pour qu'il "déborde" un peu ou reste au bord
        StackPane.setMargin(boutonX, new Insets(-5, -5, 0, 0)); 
        
        conteneurImage.getChildren().addAll(view, boutonX);


        // TEXTE
        Label labelNom = new Label(nom);
        labelNom.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        // On ajoute le conteneur (Image+X) et le nom à la carte
        carte.getChildren().addAll(conteneurImage, labelNom);

        // GESTION DU HOVER (SURVOL)
        carte.setOnMouseEntered(e -> {
            carte.setStyle("-fx-cursor: hand; -fx-background-color: #e6e6e6; -fx-padding: 15; -fx-background-radius: 10;");
            boutonX.setVisible(true); // On affiche le X
        });

        carte.setOnMouseExited(e -> {
            carte.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-padding: 15; -fx-background-radius: 10;");
            boutonX.setVisible(false); // On cache le X
        });

        // Clic sur la carte (Connexion)
        carte.setOnMouseClicked(e -> {
            manager.chargerProfil(nom);
            MainApp.changerScene("/fxml/menu.fxml");
        });

        return carte;
    }

    private VBox creerCarteAjout() {
        VBox carte = new VBox(10);
        carte.setAlignment(Pos.CENTER);
        
        // FOND
        carte.setMaxHeight(VBox.USE_PREF_SIZE);

        // Style identique au profil
        carte.setStyle("-fx-cursor: hand; -fx-padding: 15; -fx-background-color: transparent; -fx-background-radius: 10;");

        // Image Plus
        ImageView viewPlus = new ImageView();
        try {
            InputStream is = getClass().getResourceAsStream("/images/plus-symbole-noir.png");
            if (is != null) {
                viewPlus.setImage(new Image(is));
            }
        } catch (Exception e) { e.printStackTrace(); }

        viewPlus.setFitWidth(80);  // Même taille que le profil
        viewPlus.setFitHeight(80);
        viewPlus.setPreserveRatio(true);
        viewPlus.setOpacity(1.0); // Pas transparent

        // Texte "Ajouter"
        Label labelAjout = new Label("Ajouter");
        labelAjout.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        carte.getChildren().addAll(viewPlus, labelAjout);

        // Survol
        carte.setOnMouseEntered(e -> carte.setStyle("-fx-cursor: hand; -fx-background-color: #e6e6e6; -fx-padding: 15; -fx-background-radius: 10;"));
        carte.setOnMouseExited(e -> carte.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-padding: 15; -fx-background-radius: 10;"));

        // Clic
        carte.setOnMouseClicked(e -> gererCreationProfil());

        return carte;
    }

    private void gererCreationProfil() {
        // Création d'une nouvelle fenêtre
        Stage popup = new Stage();
        // On ne peut pas cliquer derrière tant que cette fenêtre est ouverte
        popup.initModality(Modality.APPLICATION_MODAL); 
        popup.setTitle("Nouveau Profil");

        // Le Conteneur Principal (VBox)
        VBox layout = new VBox(20); // 20px d'espace vertical entre les éléments
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-border-color: #ccc; -fx-border-width: 1;");

        // LE TITRE "Calcudoku"
        Label titre = new Label("Calcudoku");
        // Style
        titre.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: black; -fx-border-color: transparent transparent transparent; -fx-border-width: 0 0 2 0;");

        // L'ICÔNE UTILISATEUR
        ImageView iconView = new ImageView();
        try {
            // On réutilise ton image utilisateur.png
            InputStream is = getClass().getResourceAsStream("/images/utilisateur.png");
            if (is != null) iconView.setImage(new Image(is));
        } catch (Exception e) { }
        
        iconView.setFitWidth(60);
        iconView.setFitHeight(60);
        iconView.setPreserveRatio(true);

        Label msgErreur = new Label("Ce nom de profil est déjà pris");
        msgErreur.setStyle("-fx-text-fill: red;"); // On le met en rouge
        msgErreur.setVisible(false); // On le cache au début

        // LE CHAMP DE TEXTE
        TextField champPseudo = new TextField();
        champPseudo.setPromptText("Saisir votre pseudo"); // Le texte gris à l'intérieur
        champPseudo.setFocusTraversable(false); // Pour voir le placeholder au démarrage
        
        // Style du champ : Simple ligne ou encadré léger, centré
        champPseudo.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 10; -fx-font-size: 14px; -fx-alignment: CENTER;");
        // On limite la largeur pour que ça ressemble à ton image
        champPseudo.setMaxWidth(250);

        // LOGIQUE DE VALIDATION
        champPseudo.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String nomSaisi = champPseudo.getText().trim();
                
                if (!nomSaisi.isEmpty()) {
                    // On tente de créer le profil via le Manager
                    boolean succes = manager.creerProfil(nomSaisi);
                    
                    if (succes) {
                        rafraichirAffichage(); // Mettre à jour l'accueil
                        popup.close();         // Fermer la fenêtre
                    } else {
                        // Petit feedback visuel en cas d'erreur (bordure rouge)
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

        // Dès qu'on tape une touche, on remet tout en normal
        champPseudo.setOnKeyTyped(e -> {
            if (msgErreur.isVisible()) {
                champPseudo.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 10; -fx-font-size: 14px; -fx-alignment: CENTER;");
                msgErreur.setVisible(false);
            }
        });

        // Assemblage
        layout.getChildren().addAll(titre, iconView, champPseudo, msgErreur);

        // Affichage de la scène
        Scene scene = new Scene(layout, 400, 300); // Taille de la fenêtre (Largeur, Hauteur)
        popup.setScene(scene);
        popup.setResizable(false); // Empêcher de redimensionner
        popup.showAndWait(); // Affiche et attend
    }
}