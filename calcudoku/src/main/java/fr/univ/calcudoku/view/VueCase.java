package fr.univ.calcudoku.view;

import fr.univ.calcudoku.model.Case;
import javafx.beans.binding.Bindings;
import javafx.collections.SetChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

/**
 * Vue JavaFX d'une seule case du Calcudoku.
 * Affiche la valeur, les annotations et appelle les styles CSS dynamiquement.
 */
public class VueCase extends StackPane {
    /** Le modèle de case associé */
    private final Case caseModel;
    /** La dimension de la grille parente */
    private final int tailleGrille;
    /** Label affichant les annotations (petits chiffres) */
    private final Label labelIndice;
    /** Label affichant la valeur principale de la case */
    private final Label labelValeur;
    /** Conteneur GridPane pour les annotations */
    private GridPane conteneurAnnotation = null; 
    /** Largeur courante pour le redimensionnement */
    private double wCourant = 0;

    /**
     * Constructeur d'une vue de case.
     * @param c la case modéliser
     * @param tailleGrille la dimension de la grille
     */
    public VueCase(Case c, int tailleGrille) {
        this.caseModel = c;
        this.tailleGrille = tailleGrille;
        this.getStyleClass().add("case-grille");

        labelIndice = new Label();
        labelIndice.getStyleClass().add("label-indice");
        StackPane.setAlignment(labelIndice, Pos.TOP_LEFT);
        initialiserIndice();

        labelValeur = new Label();
        labelValeur.getStyleClass().add("label-valeur");
        labelValeur.textProperty().bind(c.valeurProperty().asString());
        labelValeur.visibleProperty().bind(c.valeurProperty().isNotEqualTo(0));

        this.getChildren().addAll(labelIndice, labelValeur);

        c.getNotes().addListener((SetChangeListener.Change<? extends Integer> change) -> rafraichirAffichage());
        
        if (!c.getNotes().isEmpty()) {
            rafraichirAffichage();
        }
    }

    /**
     * Rafraîchit l'affichage des annotations (notes) de la case.
     */
    private void rafraichirAffichage() {
        if (!caseModel.getNotes().isEmpty() && conteneurAnnotation == null) {
            creerConteneurAnnotations();
        }
        if (conteneurAnnotation != null) {
            for (int i = 0; i < conteneurAnnotation.getChildren().size(); i++) {
                Label l = (Label) conteneurAnnotation.getChildren().get(i);
                l.setVisible(caseModel.getNotes().contains(i + 1));
            }
        }
    }

    /**
     * Crée le conteneur GridPane pour afficher les annotations (petits chiffres).
     */
    private void creerConteneurAnnotations() {
        int nbCols = (int) Math.ceil(Math.sqrt(tailleGrille));
        conteneurAnnotation = new GridPane();
        conteneurAnnotation.setAlignment(Pos.CENTER);

        for (int i = 0; i < tailleGrille; i++) {
            Label l = new Label(String.valueOf(i + 1));
            l.getStyleClass().add("label-note");
            l.setAlignment(Pos.CENTER);
            l.setVisible(false);
            l.managedProperty().bind(l.visibleProperty());
            conteneurAnnotation.add(l, i % nbCols, i / nbCols);
        }

        conteneurAnnotation.visibleProperty().bind(caseModel.valeurProperty().isEqualTo(0));
        
        this.getChildren().add(1, conteneurAnnotation);

        if (wCourant > 0) {
            appliquerTailleAnnotations(wCourant);
        }
    }

    /**
     * Redimensionne la case en fonction de la largeur fournie.
     * Appelle aussi le redimensionnement des annotations.
     * @param w la nouvelle largeur en pixels
     */
    public void redimensionner(double w) {
        if (w <= 0) return;
        this.wCourant = w; 

        labelIndice.setStyle("-fx-font-size: " + (w / 6) + "px;");
        labelValeur.setStyle("-fx-font-size: " + (w / 2) + "px;");

        if (conteneurAnnotation != null) {
            appliquerTailleAnnotations(w);
        }
    }

    /**
     * Applique les tailles de police pour le conteneur des annotations.
     * @param w la largeur de la case
     */
    private void appliquerTailleAnnotations(double w) {
        double p = w * 0.10; 
        double topShift = w * 0.15; 
        conteneurAnnotation.setPadding(new Insets(topShift, p, p/2, p));
        conteneurAnnotation.setHgap(w / 40);
        conteneurAnnotation.setVgap(w / 40);

        for (int i = 0; i < conteneurAnnotation.getChildren().size(); i++) {
            Label l = (Label) conteneurAnnotation.getChildren().get(i);
            l.setStyle("-fx-font-size: " + (w / 7.5) + "px;");
        }
    }

    /**
     * Applique les bordures de la case selon les groupements voisins.
     * Les bordures sont plus épaisses entre les groupements différents.
     * @param haut la case du haut (peut être null)
     * @param bas la case du bas (peut être null)
     * @param gauche la case de gauche (peut être null)
     * @param droite la case de droite (peut être null)
     * @param w la largeur de la case
     */
    public void appliquerBordures(Case haut, Case bas, Case gauche, Case droite, double w) {
        if (w <= 0) return;

        String sh = (haut != null && haut.getGroupement() == caseModel.getGroupement()) ? "dashed" : "solid";
        String sb = (bas != null && bas.getGroupement() == caseModel.getGroupement()) ? "dashed" : "solid";
        String sg = (gauche != null && gauche.getGroupement() == caseModel.getGroupement()) ? "dashed" : "solid";
        String sd = (droite != null && droite.getGroupement() == caseModel.getGroupement()) ? "dashed" : "solid";

        String epais = (w * 0.04) + "px"; 
        String moyen = (w * 0.02) + "px"; 
        String fin = (w * 0.005) + "px";  

        String th = (haut == null) ? epais : (haut.getGroupement() != caseModel.getGroupement() ? moyen : fin);
        String tb = (bas == null) ? epais : (bas.getGroupement() != caseModel.getGroupement() ? moyen : fin);
        String tg = (gauche == null) ? epais : (gauche.getGroupement() != caseModel.getGroupement() ? moyen : fin);
        String td = (droite == null) ? epais : (droite.getGroupement() != caseModel.getGroupement() ? moyen : fin);

        this.setStyle("-fx-border-color: black; " +
                      "-fx-border-width: " + th + " " + td + " " + tb + " " + tg + "; " +
                      "-fx-border-style: " + sh + " " + sd + " " + sb + " " + sg + ";");
    }

    /**
     * Initialise et affiche l'indice de la cage (résultat et opération).
     * Seule la première case de la cage (en haut à gauche) affiche l'indice.
     */
    public void initialiserIndice() {
        if (caseModel.getGroupement() != null && caseModel == caseModel.getGroupement().getCaseOp()) {
            labelIndice.setText(caseModel.getGroupement().getResultatCible() + caseModel.getGroupement().getOperation().getSymbole());
        } else {
            labelIndice.setText("");
        }
    }
}