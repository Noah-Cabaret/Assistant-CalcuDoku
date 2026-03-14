package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.view.VueCase;
import fr.univ.calcudoku.view.VueGrille;
import fr.univ.calcudoku.save.Sauvegarde;
import fr.univ.calcudoku.save.Etape;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.control.Label;

public class JeuController {

    @FXML private StackPane conteneurGrille;
    @FXML private HBox conteneurBoutonsNombres;
    
    @FXML private Button btnUndo;
    @FXML private Button btnRedo;
    @FXML private Button btnAnnoter;
    @FXML private Button btnEffacer;
    @FXML private Button btnCalculatrice;
    @FXML private Label labelChrono;

    private Grille grilleModele;
    private VueGrille vueGrille;
    private boolean modeAnnotationActif = false;

    private VueCase vueCaseSelectionnee = null;
    private Case caseModeleSelectionnee = null;

    private Sauvegarde save = null;

    private Timeline timeline;
    private int secondesEcoulees = 0;

    public void initialiserPartie(Grille grille) {
        this.grilleModele = grille;
        
        if(save == null)
        {
            save = new Sauvegarde();
            save.setIdGrille(1);
            //save.charger(null, grille);
        }

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
        demarrerChrono();
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

    private void demarrerChrono() {
        secondesEcoulees = 0; 
        
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            secondesEcoulees++;
            
            int minutes = secondesEcoulees / 60;
            int secondes = secondesEcoulees % 60;
            
            labelChrono.setText(String.format("%02d:%02d", minutes, secondes));
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);

        timeline.play();

        save.tmp.lancer();
    }

    private void selectionnerCase(VueCase vueCase, Case modeleCase) {
        if (vueCaseSelectionnee != null) {
            vueCaseSelectionnee.getStyleClass().remove("case-selectionnee");
        }

        this.vueCaseSelectionnee = vueCase;
        this.caseModeleSelectionnee = modeleCase;

        vueCaseSelectionnee.getStyleClass().add("case-selectionnee");
    }

    
    private void actionChiffreClique(int valeur) {
        if (caseModeleSelectionnee != null) {
            if (modeAnnotationActif) {
                caseModeleSelectionnee.basculerNote(valeur);
                if(caseModeleSelectionnee.getValeur() < 10)
                    save.hist.addEtape(caseModeleSelectionnee.getX(), caseModeleSelectionnee.getY(), valeur + 10);
            } else {
                save.hist.addEtape(caseModeleSelectionnee.getX(), caseModeleSelectionnee.getY(), valeur);
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
            save.hist.addEtape(caseModeleSelectionnee.getX(), caseModeleSelectionnee.getY(), 0);
        }
    }

    /* Isolement du undo pour l'utiliser à la fois pour le bouton undo et pour le rollback */
    void undo()
    {
        if(save.hist.getIndex() > 0)
        {
            Etape etapeCourante = save.hist.getEtapeCourante();
            Etape etapePrecedente;
            int i = save.hist.getIndex();
            boolean etapeExiste = false;
            do
                etapePrecedente = save.hist.precedent();
            while((save.hist.getIndex() > 1) && !(etapeCourante.getX() == etapePrecedente.getX() && etapeCourante.getY() == etapePrecedente.getY()));

            if(etapeCourante.getX() == etapePrecedente.getX() && etapeCourante.getY() == etapePrecedente.getY())
                etapeExiste = true;

            if(etapeCourante.normale())
            {
                if(etapeExiste)
                    grilleModele.getCase(etapePrecedente.getX(), etapePrecedente.getY()).setValeur(etapePrecedente.getN());
                else
                    grilleModele.getCase(etapeCourante.getX(), etapeCourante.getY()).setValeur(0);
            }
            else if(etapeCourante.annotation())
                grilleModele.getCase(etapeCourante.getX(), etapeCourante.getY()).basculerNote(etapeCourante.getN() - 10);

            save.hist.setIndex(i - 1);
        }
    }

    void rollback(int x, int y, int valeur)
    {
        Etape etapeCourante = save.hist.getEtapeCourante();
        while((save.hist.getIndex() > 0) && (etapeCourante.getX() != x || etapeCourante.getY() != y || etapeCourante.getN() != valeur))
        {
            undo();
            etapeCourante = save.hist.getEtapeCourante();
        }
        save.hist.viderQueue();
    }

    @FXML
    void actionUndo(ActionEvent event) {
        undo();
    }

    @FXML
    void actionRedo(ActionEvent event) {
        if(save.hist.getIndex() < save.hist.taille() - 1)
        {
            Etape etapeSuivante = save.hist.suivant();
            if(etapeSuivante.normale())
                grilleModele.getCase(etapeSuivante.getX(), etapeSuivante.getY()).setValeur(etapeSuivante.getN());
            else if(etapeSuivante.annotation())
                grilleModele.getCase(etapeSuivante.getX(), etapeSuivante.getY()).basculerNote(etapeSuivante.getN() - 10);
        }
    }

    @FXML
    void actionCalculatrice(ActionEvent event) {
        System.out.println("Calculatrice cliquée");
    }
}
