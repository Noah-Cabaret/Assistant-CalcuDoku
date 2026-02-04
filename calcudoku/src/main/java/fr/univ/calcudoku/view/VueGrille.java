package fr.univ.calcudoku.view;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

public class VueGrille extends GridPane{
    private final Grille grilleModel;
    private final VueCase[][] grilleVueCases;

    public VueGrille(Grille grille){
        this.grilleModel = grille;
        int taille = grilleModel.getTaille();
        this.grilleVueCases = new VueCase[taille][taille];

        this.setAlignment(Pos.CENTER);
        this.setHgap(0);
        this.setVgap(0);

        for (int i = 0; i < taille; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setPercentWidth(100.0 / taille);
            this.getColumnConstraints().add(col);

            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.ALWAYS);
            row.setPercentHeight(100.0 / taille);
            this.getRowConstraints().add(row);
        }
        
        for(int y = 0; y < taille;y++){
            for (int x = 0; x < taille; x++) {
                Case c = grilleModel.getCase(x, y);
                VueCase vc = new VueCase(c);
                grilleVueCases[x][y] = vc;
                this.add(vc,x,y);
            }
        }
    }
    
    public void rafraichirToutesLesBordures() {
        int taille = grilleModel.getTaille();
        for (int y = 0; y < taille; y++) {
            for (int x = 0; x < taille; x++) {
                VueCase vc = grilleVueCases[x][y];
                
                Case haut   = (y > 0) ? grilleModel.getCase(x, y - 1) : null;
                Case bas    = (y < taille - 1) ? grilleModel.getCase(x, y + 1) : null;
                Case gauche = (x > 0) ? grilleModel.getCase(x - 1, y) : null;
                Case droite = (x < taille - 1) ? grilleModel.getCase(x + 1, y) : null;

                vc.appliquerBordures(haut, bas, gauche, droite);
                vc.initialiserIndice();
            }
        }
    }

    public VueCase getGrilleVueCases(int x, int y) {
        return grilleVueCases[x][y];
    }
}
