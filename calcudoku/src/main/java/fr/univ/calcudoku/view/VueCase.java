package fr.univ.calcudoku.view;

import fr.univ.calcudoku.model.Case;
import javafx.beans.binding.Bindings;
import javafx.collections.SetChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

public class VueCase extends StackPane {
    private final Case caseModel;
    private final Label labelIndice, labelValeur;
    private final GridPane conteneurAnnotation; 

    public VueCase(Case c, int tailleGrille) {
        this.caseModel = c;
        this.getStyleClass().add("case-grille");

        labelIndice = new Label();
        labelIndice.getStyleClass().add("label-indice");
        StackPane.setAlignment(labelIndice, Pos.TOP_LEFT);
        
        labelIndice.styleProperty().bind(
            Bindings.concat("-fx-font-size: ", this.widthProperty().divide(6).asString(), "px;")
        );
        initialiserIndice();

        labelValeur = new Label();
        labelValeur.getStyleClass().add("label-valeur");
        labelValeur.textProperty().bind(c.valeurProperty().asString());
        labelValeur.visibleProperty().bind(c.valeurProperty().isNotEqualTo(0));
        
        labelValeur.styleProperty().bind(
            Bindings.concat("-fx-font-size: ", this.widthProperty().divide(2).asString(), "px;")
        );

        int nbCols = (int) Math.ceil(Math.sqrt(tailleGrille));
        conteneurAnnotation = new GridPane();
        conteneurAnnotation.setAlignment(Pos.CENTER);

        conteneurAnnotation.paddingProperty().bind(Bindings.createObjectBinding(() -> {
            double p = this.getWidth() * 0.10; 
            double topShift = this.getHeight() * 0.15; 
            return new Insets(topShift, p, p/2, p);
        }, this.widthProperty(), this.heightProperty()));

        conteneurAnnotation.hgapProperty().bind(this.widthProperty().divide(40));
        conteneurAnnotation.vgapProperty().bind(this.heightProperty().divide(40));

        for (int i = 0; i < tailleGrille; i++) {
            Label l = new Label(String.valueOf(i + 1));
            l.getStyleClass().add("label-note");
            
            l.styleProperty().bind(
                Bindings.concat("-fx-font-size: ", this.widthProperty().divide(7.5).asString(), "px;")
            );

            l.setAlignment(Pos.CENTER);
            l.setVisible(false);
            l.managedProperty().bind(l.visibleProperty());

            conteneurAnnotation.add(l, i % nbCols, i / nbCols);
        }

        conteneurAnnotation.visibleProperty().bind(c.valeurProperty().isEqualTo(0));
        c.getNotes().addListener((SetChangeListener.Change<? extends Integer> change) -> rafraichirAffichage());
        
        rafraichirAffichage();
        this.getChildren().addAll(labelIndice, conteneurAnnotation, labelValeur);
    }

    private void rafraichirAffichage() {
        for (int i = 0; i < conteneurAnnotation.getChildren().size(); i++) {
            Label l = (Label) conteneurAnnotation.getChildren().get(i);
            l.setVisible(caseModel.getNotes().contains(i + 1));
        }
    }

    public void appliquerBordures(Case haut, Case bas, Case gauche, Case droite) {
        String sh = (haut != null && haut.getGroupement() == caseModel.getGroupement()) ? "dashed" : "solid";
        String sb = (bas != null && bas.getGroupement() == caseModel.getGroupement()) ? "dashed" : "solid";
        String sg = (gauche != null && gauche.getGroupement() == caseModel.getGroupement()) ? "dashed" : "solid";
        String sd = (droite != null && droite.getGroupement() == caseModel.getGroupement()) ? "dashed" : "solid";

        this.styleProperty().bind(Bindings.createStringBinding(() -> {
            double w = this.getWidth();
            
            if (w == 0) return "-fx-border-color: transparent;";

            String epais = (w * 0.04) + "px"; 
            String moyen = (w * 0.02) + "px"; 
            String fin = (w * 0.005) + "px";  

            String th = (haut == null) ? epais : (haut.getGroupement() != caseModel.getGroupement() ? moyen : fin);
            String tb = (bas == null) ? epais : (bas.getGroupement() != caseModel.getGroupement() ? moyen : fin);
            String tg = (gauche == null) ? epais : (gauche.getGroupement() != caseModel.getGroupement() ? moyen : fin);
            String td = (droite == null) ? epais : (droite.getGroupement() != caseModel.getGroupement() ? moyen : fin);

            return "-fx-border-color: black; " +
                   "-fx-border-width: " + th + " " + td + " " + tb + " " + tg + "; " +
                   "-fx-border-style: " + sh + " " + sd + " " + sb + " " + sg + ";";
                   
        }, this.widthProperty())); 
    }

    public void initialiserIndice() {
        if (caseModel.getGroupement() != null && caseModel == caseModel.getGroupement().getCaseOp()) {
            labelIndice.setText(caseModel.getGroupement().getResultatCible() + caseModel.getGroupement().getOperation().getSymbole());
        } else {
            labelIndice.setText("");
        }
    }
}