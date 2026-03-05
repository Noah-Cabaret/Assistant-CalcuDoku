package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.view.VueCase;
import fr.univ.calcudoku.view.VueGrille;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class JeuController {

    @FXML private StackPane conteneurGrille;
    @FXML private HBox conteneurBoutonsNombres;
    
    @FXML private Button btnUndo;
    @FXML private Button btnRedo;
    @FXML private Button btnAnnoter;
    @FXML private Button btnEffacer;
    @FXML private Button btnCalculatrice;
    @FXML private Button btnVerif;

    private Grille grilleModele;
    private VueGrille vueGrille;
    private boolean modeAnnotationActif = false;

    private VueCase vueCaseSelectionnee = null;
    private Case caseModeleSelectionnee = null;

    public void initialiserPartie(Grille grille) {
        this.grilleModele = grille;
        
        this.vueGrille = new VueGrille(grille);
        vueGrille.rafraichirToutesLesBordures();
        conteneurGrille.getChildren().add(vueGrille);

        NumberBinding tailleCarree = Bindings.min(conteneurGrille.widthProperty(), conteneurGrille.heightProperty());
        vueGrille.prefWidthProperty().bind(tailleCarree);
        vueGrille.prefHeightProperty().bind(tailleCarree);
        vueGrille.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        for (int y = 0; y < grille.getTaille(); y++) {
            for (int x = 0; x < grille.getTaille(); x++) {
                VueCase vc = vueGrille.getGrilleVueCases(x, y);
                final Case modeleCase = grille.getCase(x, y);
                
                vc.setOnMouseClicked(event -> selectionnerCase(vc, modeleCase));
            }
        }

        genererBoutonsNombres(grille.getTaille());
    }

    private void genererBoutonsNombres(int taille) {
        conteneurBoutonsNombres.getChildren().clear(); 
        
        for (int i = 1; i <= taille; i++) {
            Button btnChiffre = new Button(String.valueOf(i));
            
            btnChiffre.setMinSize(55, 55); 
            btnChiffre.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

            final int valeur = i; 
            btnChiffre.setOnAction(e -> actionChiffreClique(valeur));
            
            conteneurBoutonsNombres.getChildren().add(btnChiffre);
        }
    }

    private void selectionnerCase(VueCase vueCase, Case modeleCase) {
        if (vueCaseSelectionnee != null) {
            vueCaseSelectionnee.getStyleClass().remove("case-selectionnee");
        }

        this.vueCaseSelectionnee = vueCase;
        this.caseModeleSelectionnee = modeleCase;

        vueCaseSelectionnee.getStyleClass().add("case-selectionnee");
    }

    //ACTIONS DES BOUTONS
    private void actionChiffreClique(int valeur) {
        if (caseModeleSelectionnee != null) {
            if (modeAnnotationActif) {
                caseModeleSelectionnee.basculerNote(valeur); 
            } else {
                caseModeleSelectionnee.setValeur(valeur);   
                
                if (grilleModele.estGagnee()) {
                    System.out.println("VICTOIRE ! La grille est complétée correctement !");
                }
            }
        }
    }

    @FXML
    void actionBasculeAnnotation(ActionEvent event) {
        modeAnnotationActif = !modeAnnotationActif;
        if(modeAnnotationActif) {
            btnAnnoter.setStyle("-fx-background-color: #bbdefb;"); 
        } else {
            btnAnnoter.setStyle(""); 
        }
    }

    @FXML
    void actionEffacer(ActionEvent event) {
        if (caseModeleSelectionnee != null) {
            caseModeleSelectionnee.setValeur(0);
            caseModeleSelectionnee.effacerNotes();
        }
    }

    @FXML
    void actionUndo(ActionEvent event) {
        System.out.println("Undo cliqué");
    }

    @FXML
    void actionRedo(ActionEvent event) {
        System.out.println("Redo cliqué");
    }

    @FXML
    void actionCalculatrice(ActionEvent event) {
        System.out.println("Calculatrice cliquée");
    }

    @FXML 
    void actionVerifErreurs(ActionEvent event) {
        System.out.println("vérification cliquée");
    }
}