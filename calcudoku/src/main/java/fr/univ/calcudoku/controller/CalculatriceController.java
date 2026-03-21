package fr.univ.calcudoku.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class CalculatriceController {

    @FXML private TextField affichage;
    private double total = 0;
    private String operateur = "";
    private boolean debut = true;

    @FXML
    private void handleChiffre(ActionEvent event) {
        String valeur = ((Button)event.getSource()).getText();
        if (debut) {
            affichage.setText(valeur);
            debut = false;
        } else {
            affichage.appendText(valeur);
        }
    }

    @FXML
    private void handleOperateur(ActionEvent event) {
        String op = ((Button)event.getSource()).getText();
        if (!"=".equals(op)) {
            operateur = op;
            total = Double.parseDouble(affichage.getText());
            debut = true;
        } else {
            calculer(Double.parseDouble(affichage.getText()));
            operateur = "";
            debut = true;
        }
    }

    private void calculer(double n) {
        switch (operateur) {
            case "+" -> total += n;
            case "-" -> total -= n;
            case "*" -> total *= n;
            case "/" -> { if (n != 0) total /= n; }
        }
        affichage.setText(String.valueOf(total));
    }

    @FXML
    private void handleEffacer() {
        affichage.setText("");
        total = 0;
        debut = true;
    }
}