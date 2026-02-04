package fr.univ.calcudoku.view;

import fr.univ.calcudoku.model.Case;
import javafx.collections.SetChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;

public class VueCase extends StackPane {
    private final Case caseModel;
    private final Label labelIndice;
    private final Label labelValeur;
    private final FlowPane conteneurAnnotation;

    public VueCase(Case c) {
        this.caseModel = c;
        this.getStyleClass().add("case-grille");
        this.setMinSize(10, 10);
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        this.labelIndice = new Label();
        labelIndice.getStyleClass().add("label-indice");
        StackPane.setAlignment(labelIndice, Pos.TOP_LEFT);
        labelIndice.styleProperty().bind(this.widthProperty().divide(5).asString("-fx-font-size: %.0fpx;"));
        initialiserIndice();

        this.labelValeur = new Label();
        labelValeur.getStyleClass().add("label-valeur");
        labelValeur.textProperty().bind(c.valeurProperty().asString());
        labelValeur.visibleProperty().bind(c.valeurProperty().isNotEqualTo(0));
        labelValeur.styleProperty().bind(this.widthProperty().divide(1.8).asString("-fx-font-size: %.0fpx; -fx-font-weight: bold;"));

        this.conteneurAnnotation = new FlowPane();
        conteneurAnnotation.setAlignment(Pos.CENTER);
        conteneurAnnotation.setHgap(1);
        conteneurAnnotation.setVgap(1);

        conteneurAnnotation.maxWidthProperty().bind(this.widthProperty().multiply(0.65));
        conteneurAnnotation.prefWrapLengthProperty().bind(this.widthProperty().multiply(0.65));
        
        conteneurAnnotation.visibleProperty().bind(c.valeurProperty().isEqualTo(0));

        c.getNotes().addListener((SetChangeListener<Integer>) change -> { rafraichirAffichage(); });
        rafraichirAffichage();

        this.getChildren().addAll(labelIndice, conteneurAnnotation, labelValeur);
    }

    private void rafraichirAffichage() {
        conteneurAnnotation.getChildren().clear();
        caseModel.getNotes().stream()
             .sorted()
             .forEach(n -> {
                 Label l = new Label(String.valueOf(n));
                 l.getStyleClass().add("label-note");
                 l.prefWidthProperty().bind(conteneurAnnotation.maxWidthProperty().divide(3.2));                 
                 l.styleProperty().bind(this.widthProperty().divide(8).asString("-fx-font-size: %.0fpx;")); 
                 l.setAlignment(Pos.CENTER);
                 conteneurAnnotation.getChildren().add(l);
             });
    }
    
    public void initialiserIndice() {
        if (caseModel.getGroupement() != null && caseModel == caseModel.getGroupement().getCaseOp()) {
            String texte = caseModel.getGroupement().getResultatCible() + caseModel.getGroupement().getOperation().getSymbole();
            labelIndice.setText(texte);
        } else {
            labelIndice.setText(""); 
        }
    }
    
    public void setSelectionnee(boolean b) {
        if (b) {
            if (!getStyleClass().contains("case-selectionnee")) {
                getStyleClass().add("case-selectionnee");
            }
        } else {
            getStyleClass().remove("case-selectionnee");
        }
    }

    public void appliquerBordures(Case haut, Case bas, Case gauche, Case droite) {
        String th = (haut == null) ? "4px" : (haut.getGroupement() != caseModel.getGroupement() ? "2px" : "0.5px");
        String tb = (bas == null) ? "4px" : (bas.getGroupement() != caseModel.getGroupement() ? "2px" : "0.5px");
        String tg = (gauche == null) ? "4px" : (gauche.getGroupement() != caseModel.getGroupement() ? "2px" : "0.5px");
        String td = (droite == null) ? "4px" : (droite.getGroupement() != caseModel.getGroupement() ? "2px" : "0.5px");

        String sh = (haut != null && haut.getGroupement() == caseModel.getGroupement()) ? "dashed" : "solid";
        String sb = (bas != null && bas.getGroupement() == caseModel.getGroupement()) ? "dashed" : "solid";
        String sg = (gauche != null && gauche.getGroupement() == caseModel.getGroupement()) ? "dashed" : "solid";
        String sd = (droite != null && droite.getGroupement() == caseModel.getGroupement()) ? "dashed" : "solid";

        this.setStyle(
            "-fx-border-color: black; " +
            "-fx-border-width: " + th + " " + td + " " + tb + " " + tg + "; " +
            "-fx-border-style: " + sh + " " + sd + " " + sb + " " + sg + ";"
        );
    }
}