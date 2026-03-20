package fr.univ.calcudoku.utils;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Fabrique centralisée pour générer les cartes des grilles dans l'interface.
 */
public class CarteUIFactory {

    /**
     * Crée une VBox représentant une grille (image, titre, temps) prête à l'emploi.
     */
    public static VBox creerCarteGrille(String titreText, String tempsText, Image imageGrille, Region conteneurParent, Runnable actionClic) {
        VBox vBox = new VBox(10);
        vBox.setMinSize(0, 0);
        vBox.setAlignment(Pos.CENTER);

        // 1. Les effets de survol de la souris
        String styleNormal = "-fx-cursor: hand; -fx-padding: 10; -fx-background-color: transparent; -fx-border-color: transparent; -fx-border-radius: 10; -fx-background-radius: 10;";
        String styleHover = "-fx-cursor: hand; -fx-padding: 10; -fx-background-color: #f5f5f5; -fx-border-color: #cccccc; -fx-border-radius: 10; -fx-background-radius: 10;";

        vBox.setStyle(styleNormal);
        vBox.setOnMouseEntered(e -> vBox.setStyle(styleHover));
        vBox.setOnMouseExited(e -> vBox.setStyle(styleNormal));

        // 2. L'image de la grille
        ImageView vueMiniature = new ImageView();
        if (imageGrille != null) {
            vueMiniature.setImage(imageGrille);
        } else {
            vueMiniature.setStyle("-fx-background-color: lightgray;");
        }

        // 3. Le rendu Elastique/Responsive !
        javafx.beans.binding.NumberBinding tailleHauteur = conteneurParent.heightProperty().multiply(0.60);
        javafx.beans.binding.NumberBinding tailleLargeur = conteneurParent.widthProperty().divide(3.5);
        javafx.beans.binding.NumberBinding tailleMax = javafx.beans.binding.Bindings.min(tailleHauteur, tailleLargeur);

        vueMiniature.fitHeightProperty().bind(tailleMax);
        vueMiniature.fitWidthProperty().bind(tailleMax);
        vueMiniature.setPreserveRatio(true);

        // 4. Les textes
        Label titre = new Label(titreText);
        titre.setStyle("-fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label lblTemps = new Label(tempsText);
        lblTemps.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 11px; -fx-text-fill: #555555;");

        vBox.getChildren().addAll(vueMiniature, titre, lblTemps);

        // 5. L'action quand on clique sur la carte !
        vBox.setOnMouseClicked(e -> {
            if (actionClic != null) actionClic.run();
        });

        return vBox;
    }

    /**
     * Crée une carte grisée pour les niveaux non disponibles (Menu Libre)
     */
    public static VBox creerCarteVide(String titreText, Region conteneurParent) {
        VBox carteVide = new VBox(10);
        carteVide.setAlignment(Pos.CENTER);
        carteVide.setMinSize(0, 0); 
        
        javafx.beans.binding.NumberBinding tailleHauteur = conteneurParent.heightProperty().multiply(0.60);
        javafx.beans.binding.NumberBinding tailleLargeur = conteneurParent.widthProperty().divide(3.5);
        javafx.beans.binding.NumberBinding tailleMax = javafx.beans.binding.Bindings.min(tailleHauteur, tailleLargeur);

        carteVide.prefWidthProperty().bind(tailleMax);
        carteVide.prefHeightProperty().bind(tailleMax);
        carteVide.setStyle("-fx-border-color: #e0e0e0; -fx-background-color: #fafafa; -fx-border-radius: 10;");
        
        Label lbl = new Label(titreText + "\nIndisponible");
        lbl.setStyle("-fx-text-alignment: center; -fx-text-fill: #aaaaaa; -fx-font-size: 13px;");
        
        carteVide.getChildren().add(lbl);
        return carteVide;
    }
}