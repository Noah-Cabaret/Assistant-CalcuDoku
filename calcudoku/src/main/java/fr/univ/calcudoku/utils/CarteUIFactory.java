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
     * Crée une carte cliquable avec miniature, titre et temps.
     * @param titreText le titre de la grille
     * @param tempsText le temps affiché sous la miniature
     * @param imageGrille l'image miniature de la grille
     * @param conteneurParent le conteneur parent (pour le binding de taille)
     * @param actionClic l'action au clic sur la carte
     * @return la VBox de la carte
     */
    public static VBox creerCarteGrille(String titreText, String tempsText, Image imageGrille, Region conteneurParent, Runnable actionClic) {
        VBox vBox = new VBox(10);
        vBox.setMinSize(0, 0);
        vBox.setAlignment(Pos.CENTER);

        String styleNormal = "-fx-cursor: hand; -fx-padding: 10; -fx-background-color: transparent; -fx-border-color: transparent; -fx-border-radius: 10; -fx-background-radius: 10;";
        String styleHover = "-fx-cursor: hand; -fx-padding: 10; -fx-background-color: #f5f5f5; -fx-border-color: #cccccc; -fx-border-radius: 10; -fx-background-radius: 10;";

        if (fr.univ.calcudoku.MainApp.isModeSombre()) {
            styleHover = "-fx-cursor: hand; -fx-padding: 10; -fx-background-color: #444444; -fx-border-color: #777777; -fx-border-radius: 10; -fx-background-radius: 10;";
        }

        vBox.setStyle(styleNormal);
        String finalStyleHover = styleHover;
        vBox.setOnMouseEntered(e -> vBox.setStyle(finalStyleHover));
        vBox.setOnMouseExited(e -> vBox.setStyle(styleNormal));

        ImageView vueMiniature = new ImageView();
        if (imageGrille != null) {
            vueMiniature.setImage(imageGrille);
        } else {
            vueMiniature.setStyle("-fx-background-color: lightgray;");
        }

        javafx.beans.binding.NumberBinding tailleHauteur = conteneurParent.heightProperty().multiply(0.60);
        javafx.beans.binding.NumberBinding tailleLargeur = conteneurParent.widthProperty().divide(3.5);
        javafx.beans.binding.NumberBinding tailleMax = javafx.beans.binding.Bindings.min(tailleHauteur, tailleLargeur);

        vueMiniature.fitHeightProperty().bind(tailleMax);
        vueMiniature.fitWidthProperty().bind(tailleMax);
        vueMiniature.setPreserveRatio(true);

        Label titre = new Label(titreText);
        Label lblTemps = new Label(tempsText);

        if (fr.univ.calcudoku.MainApp.isModeSombre()) {
            titre.setStyle("-fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: white;");
            lblTemps.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 11px; -fx-text-fill: #cccccc;");
        } else {
            titre.setStyle("-fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: black;");
            lblTemps.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 11px; -fx-text-fill: #555555;");
        }

        vBox.getChildren().addAll(vueMiniature, titre, lblTemps);

        vBox.setOnMouseClicked(e -> {
            if (actionClic != null) actionClic.run();
        });

        return vBox;
    }

    /**
     * Crée une carte vide (grille indisponible).
     * @param titreText le titre à afficher
     * @param conteneurParent le conteneur parent
     * @return la VBox de la carte vide
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