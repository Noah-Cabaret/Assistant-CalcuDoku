package fr.univ.calcudoku.utils;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Fabrique centralisée pour générer les pop-ups (fenêtres modales) du jeu.
 */
public class PopupFactory {

    /**
     * Affiche la pop-up de fin de partie (Victoire ou Défaite).
     * @param actionRejouer L'action (méthode) à exécuter quand le joueur clique sur Rejouer.
     * @param actionQuitter L'action (méthode) à exécuter quand le joueur clique sur Quitter.
     */
    public static void afficherPopupFinPartie(String titre, String message, String iconeNom, String couleurHex, boolean victoire, Runnable actionRejouer, Runnable actionQuitter) {
        Platform.runLater(() -> {
            Stage fenetreModale = new Stage(StageStyle.TRANSPARENT);
            
            // La carte principale blanche
            VBox carte = new VBox(20);
            carte.setAlignment(Pos.CENTER);
            carte.getStyleClass().add("popup-fin-carte");
            
            // L'icône
            FontIcon icone = new FontIcon(iconeNom);
            icone.setIconSize(60);
            icone.setIconColor(Color.web(couleurHex));

            // Les textes stylisés
            Label lblTitre = new Label(titre);
            lblTitre.getStyleClass().add("popup-fin-titre");
            
            Label lblMessage = new Label(message);
            lblMessage.getStyleClass().add("popup-fin-message");

            // --- CRÉATION DES BOUTONS ---
            
            // Bouton Rejouer (Coloré — couleur dynamique)
            Button btnRejouer = new Button(victoire ? "Rejouer" : "Réessayer");
            btnRejouer.getStyleClass().add("popup-fin-btn-rejouer");
            btnRejouer.setStyle("-fx-background-color: " + couleurHex + ";");
            btnRejouer.setOnAction(e -> { 
                fenetreModale.close(); 
                if (actionRejouer != null) {
                    actionRejouer.run(); 
                }
            });

            // Bouton Quitter (Gris / Transparent)
            Button btnQuitter = new Button("Menu");
            btnQuitter.getStyleClass().add("popup-fin-btn-quitter");
            btnQuitter.setOnAction(e -> { 
                fenetreModale.close(); 
                if (actionQuitter != null) {
                    actionQuitter.run(); 
                }
            });

            // On place les deux boutons côte à côte dans une HBox
            HBox boxBoutons = new HBox(15);
            boxBoutons.setAlignment(Pos.CENTER);
            boxBoutons.getChildren().addAll(btnQuitter, btnRejouer);
            
            // --- ASSEMBLAGE FINAL ---
            carte.getChildren().addAll(icone, lblTitre, lblMessage, boxBoutons);
            
            // Un fond semi-transparent pour assombrir le jeu derrière la pop-up
            StackPane fond = new StackPane(carte);
            fond.getStyleClass().add("popup-fin-fond");
            fond.setPadding(new Insets(50));
            
            Scene scene = new Scene(fond, Color.TRANSPARENT);
            scene.getStylesheets().add(PopupFactory.class.getResource("/styles/style.css").toExternalForm());
            fenetreModale.setScene(scene);
            
            // Bloque les clics sur la fenêtre principale tant que la pop-up est ouverte
            fenetreModale.initModality(Modality.APPLICATION_MODAL);
            fenetreModale.show();
        });
    }
}