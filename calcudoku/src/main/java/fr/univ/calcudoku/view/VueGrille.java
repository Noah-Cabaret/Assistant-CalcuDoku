package fr.univ.calcudoku.view;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

public class VueGrille extends GridPane {
    private final Grille grilleModel;
    private final VueCase[][] grilleVueCases;

    public VueGrille(Grille grille) {
        this.grilleModel = grille;
        int taille = grilleModel.getTaille();
        this.grilleVueCases = new VueCase[taille][taille];
        this.setAlignment(Pos.CENTER);

        for (int i = 0; i < taille; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / taille);
            this.getColumnConstraints().add(col);

            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100.0 / taille);
            this.getRowConstraints().add(row);
        }

        for (int y = 0; y < taille; y++) {
            for (int x = 0; x < taille; x++) {
                VueCase vc = new VueCase(grilleModel.getCase(x, y), taille);
                grilleVueCases[x][y] = vc;
                this.add(vc, x, y);
            }
        }
    }

    public void rafraichirToutesLesBordures() {
        int t = grilleModel.getTaille();
        for (int y = 0; y < t; y++) {
            for (int x = 0; x < t; x++) {
                Case h = (y > 0) ? grilleModel.getCase(x, y-1) : null;
                Case b = (y < t-1) ? grilleModel.getCase(x, y+1) : null;
                Case g = (x > 0) ? grilleModel.getCase(x-1, y) : null;
                Case d = (x < t-1) ? grilleModel.getCase(x+1, y) : null;
                grilleVueCases[x][y].appliquerBordures(h, b, g, d);
                grilleVueCases[x][y].initialiserIndice();
            }
        }
    }

    public VueCase getGrilleVueCases(int x, int y) {
        return grilleVueCases[x][y];
    }
}