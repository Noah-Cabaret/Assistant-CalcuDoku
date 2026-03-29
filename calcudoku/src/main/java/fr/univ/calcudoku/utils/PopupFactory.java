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
            carte.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 30; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 20, 0, 0, 0);");
            
            // L'icône
            FontIcon icone = new FontIcon(iconeNom);
            icone.setIconSize(60);
            icone.setIconColor(Color.web(couleurHex));

            // Les textes stylisés
            Label lblTitre = new Label(titre);
            lblTitre.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #333333;");
            
            Label lblMessage = new Label(message);
            lblMessage.setStyle("-fx-font-size: 15px; -fx-text-fill: #666666; -fx-text-alignment: center;");

            // --- CRÉATION DES BOUTONS ---
            
            // Bouton Rejouer (Coloré)
            Button btnRejouer = new Button(victoire ? "Rejouer" : "Réessayer");
            btnRejouer.setStyle("-fx-background-color: " + couleurHex + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 20; -fx-cursor: hand;");
            btnRejouer.setOnAction(e -> { 
                fenetreModale.close(); 
                if (actionRejouer != null) {
                    actionRejouer.run(); 
                }
            });

            // Bouton Quitter (Gris / Transparent)
            Button btnQuitter = new Button("Menu");
            btnQuitter.setStyle("-fx-background-color: transparent; -fx-border-color: #cccccc; -fx-border-width: 2px; -fx-border-radius: 20; -fx-text-fill: #666666; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8 25; -fx-cursor: hand;");
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
            fond.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);");
            fond.setPadding(new Insets(50));
            
            Scene scene = new Scene(fond, Color.TRANSPARENT);
            fenetreModale.setScene(scene);
            
            // Bloque les clics sur la fenêtre principale tant que la pop-up est ouverte
            fenetreModale.initModality(Modality.APPLICATION_MODAL);
            fenetreModale.show();
        });
    }
}