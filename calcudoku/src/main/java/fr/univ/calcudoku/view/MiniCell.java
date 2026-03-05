package fr.univ.calcudoku.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.geometry.Insets;

public class MiniCell extends StackPane {

    public MiniCell(String borderStyle, String indice, boolean showIndice, double fontSize) {
        // 1. Appliquer le style de bordure (CSS)
        this.setStyle("-fx-background-color: white; " + borderStyle);

        // 2. Ajouter l'indice si nécessaire
        if (showIndice) {
            Label l = new Label(indice);
            l.setFont(new Font("Arial", fontSize));
            l.setStyle("-fx-font-weight: bold;");
            
            StackPane.setAlignment(l, Pos.TOP_LEFT);
            StackPane.setMargin(l, new Insets(1, 0, 0, 2));
            this.getChildren().add(l);
        }
    }
}