package fr.univ.calcudoku.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * Contrôleur pour la calculatrice simple de l'application.
 * Gère les opérations arithmétiques de base.
 */
public class CalculatriceController {

    /** Champ de texte pour afficher les nombres et résultats. */
    @FXML 
    private TextField affichage;
    /** Stocke le résultat courant du calcul. */
    private double total = 0;
    /** Stocke l'opérateur sélectionné (+, -, *, /). */
    private String operateur = "";
    /** Indique si la saisie d'un nouveau nombre commence. */
    private boolean debut = true;

    /**
     * Gère l'événement de clic sur un bouton de chiffre.
     * Ajoute le chiffre à l'affichage.
     *
     * @param event L'événement de clic.
     */
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

    /**
     * Gère l'événement de clic sur un bouton d'opérateur.
     * Effectue le calcul précédent si nécessaire et stocke le nouvel opérateur.
     *
     * @param event L'événement de clic.
     */
    @FXML
    private void handleOperateur(ActionEvent event) {
        String op = ((Button)event.getSource()).getText();
        
        // --- CORRECTION BUG 3 : ENCHAÎNEMENT DES CALCULS ---
        if (!debut) {
            if (!operateur.isEmpty()) {
                calculer(Double.parseDouble(affichage.getText()));
            } else {
                total = Double.parseDouble(affichage.getText());
            }
            afficherResultat();
            debut = true;
        }
        
        if (!"=".equals(op)) {
            operateur = op;
        } else {
            operateur = "";
        }
    }

    /**
     * Effectue le calcul en fonction de l'opérateur stocké et du nouveau nombre.
     *
     * @param n Le nombre sur lequel appliquer l'opération.
     */
    private void calculer(double n) {
        switch (operateur) {
            case "+" -> total += n;
            case "-" -> total -= n;
            case "*" -> total *= n;
            case "/" -> { if (n != 0) total /= n; }
        }
    }

    /**
     * Affiche le résultat total dans le champ de texte, en formatant comme un entier si possible.
     */
    private void afficherResultat() {
        if (total == (long) total) {
            affichage.setText(String.format("%d", (long) total));
        } else {
            affichage.setText(String.valueOf(total));
        }
    }

    /**
     * Gère l'événement de clic sur le bouton 'C' (Effacer).
     * Réinitialise l'état de la calculatrice.
     */
    @FXML
    private void handleEffacer() {
        affichage.setText("");
        total = 0;
        operateur = "";
        debut = true;
    }
}