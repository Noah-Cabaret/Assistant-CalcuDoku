package fr.univ.calcudoku.service;

import fr.univ.calcudoku.model.BlocData;
import fr.univ.calcudoku.model.DonneesNiveau;
import fr.univ.calcudoku.view.MiniCell;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class MiniGridFactory {

    private static final double TAILLE_GLOBALE = 120.0;

    /**
     * Crée le composant visuel complet (Grille + Titre + Temps)
     */
    public static VBox createMiniature(DonneesNiveau niveau, String nomFichier) {
        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER);

        // 1. La Grille
        GridPane grid = new GridPane();
        grid.setStyle("-fx-border-color: black; -fx-border-width: 1.5px; -fx-background-color: white;");
        
        // Taille fixe du conteneur
        grid.setPrefSize(TAILLE_GLOBALE, TAILLE_GLOBALE);
        grid.setMinSize(TAILLE_GLOBALE, TAILLE_GLOBALE);
        grid.setMaxSize(TAILLE_GLOBALE, TAILLE_GLOBALE);

        // Contraintes Responsive (%)
        setupConstraints(grid, niveau.dim);

        // Remplissage
        remplirCases(grid, niveau);

        // 2. Les Labels (Titre + Temps)
        String nomPropre = nomFichier.replace(".json", "");
        Label titre = new Label("Grille " + nomPropre);
        titre.setStyle("-fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-font-size: 11px;");

        int min = niveau.temps / 60;
        int sec = niveau.temps % 60;
        Label lblTemps = new Label(String.format("Temps : %d:%02d", min, sec));
        lblTemps.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 10px; -fx-text-fill: #333333;");

        container.getChildren().addAll(grid, titre, lblTemps);
        return container;
    }

    private static void setupConstraints(GridPane grid, int dim) {
        for (int i = 0; i < dim; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / dim);
            grid.getColumnConstraints().add(col);
            
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100.0 / dim);
            grid.getRowConstraints().add(row);
        }
    }

    private static void remplirCases(GridPane grid, DonneesNiveau niveau) {
        double tailleCaseTheorique = TAILLE_GLOBALE / niveau.dim;
        double fontSize = Math.max(7, tailleCaseTheorique * 0.35);

        for (int y = 0; y < niveau.dim; y++) {
            for (int x = 0; x < niveau.dim; x++) {
                int indexBloc = getBlocIndex(x, y, niveau);
                BlocData bloc = niveau.blocs.get(indexBloc);

                // Calcul des bordures (KenKen logic)
                String borderStyle = calculerBordures(x, y, indexBloc, niveau);
                
                // Doit-on afficher l'indice ?
                boolean isFirst = isFirstCellOfBlock(x, y, bloc);
                String texteIndice = bloc.result + bloc.op;

                // Création via notre objet Vue
                MiniCell cell = new MiniCell(borderStyle, texteIndice, isFirst, fontSize);
                
                grid.add(cell, x, y);
            }
        }
    }

    private static String calculerBordures(int x, int y, int blocActuel, DonneesNiveau niveau) {
        String styleDroit = "solid";
        int widthDroit = 1;
        if (x < niveau.dim - 1) {
            int voisin = getBlocIndex(x + 1, y, niveau);
            if (voisin == blocActuel) { styleDroit = "dashed"; widthDroit = 1; }
            else { styleDroit = "solid"; widthDroit = 1; }
        }

        String styleBas = "solid";
        int widthBas = 1;
        if (y < niveau.dim - 1) {
            int voisin = getBlocIndex(x, y + 1, niveau);
            if (voisin == blocActuel) { styleBas = "dashed"; widthBas = 1; }
            else { styleBas = "solid"; widthBas = 1; }
        }

        return String.format(
            "-fx-border-color: black; -fx-border-style: solid %s %s solid; -fx-border-width: 0 %d %d 0;",
            styleDroit, styleBas, widthDroit, widthBas
        );
    }

    private static int getBlocIndex(int x, int y, DonneesNiveau niveau) {
        if (x < 0 || y < 0 || x >= niveau.dim || y >= niveau.dim) return -1;
        for (int i = 0; i < niveau.blocs.size(); i++) {
            if (niveau.blocs.get(i).nums.containsKey(x + "," + y)) return i;
        }
        return -1;
    }

    private static boolean isFirstCellOfBlock(int x, int y, BlocData bloc) {
        int minX = 1000, minY = 1000;
        for (String key : bloc.nums.keySet()) {
            String[] parts = key.split(",");
            int cx = Integer.parseInt(parts[0]);
            int cy = Integer.parseInt(parts[1]);
            if (cy < minY) { minY = cy; minX = cx; }
            else if (cy == minY && cx < minX) { minX = cx; }
        }
        return (x == minX && y == minY);
    }
}