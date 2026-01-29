package fr.univ.calcudoku;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Operation;

public class MainTest {

    public static void main(String[] args) {
        afficherTitre("TEST CALCUDOKU (Corrigé)");

        // 1. SOLUTION DÉFINIE PAR LIGNES (Comme on lit naturellement)
        int[][] solutionVisuelle = {
            {2, 1, 4, 3}, // Ligne 0
            {4, 3, 2, 1}, // Ligne 1
            {1, 2, 3, 4}, // Ligne 2
            {3, 4, 1, 2}  // Ligne 3
        };
        
        // Conversion pour la Grille
        int[][] solutionPourGrille = transposer(solutionVisuelle);

        // 2. CRÉATION
        Grille grille = new Grille(4, solutionPourGrille, null);
        
        // 3. GROUPEMENTS 
        // Cage "3+" : Cases (0,0) et (1,0)
        GroupementCases cage3Plus = new GroupementCases(Operation.ADDITION, 3);
        cage3Plus.ajouterCase(grille.getCase(0, 0));
        cage3Plus.ajouterCase(grille.getCase(1, 0));
        grille.ajouterGroupement(cage3Plus);

        // Cage "7+" : Cases (2,0) et (3,0)
        GroupementCases cage7Plus = new GroupementCases(Operation.ADDITION, 7);
        cage7Plus.ajouterCase(grille.getCase(2, 0));
        cage7Plus.ajouterCase(grille.getCase(3, 0));
        grille.ajouterGroupement(cage7Plus);
        
        // Cage "6x" : Cases (1,1) et (1,2)
        GroupementCases cage6Fois = new GroupementCases(Operation.MULTIPLICATION, 6);
        cage6Fois.ajouterCase(grille.getCase(1, 1));
        cage6Fois.ajouterCase(grille.getCase(1, 2));
        grille.ajouterGroupement(cage6Fois);

        // AFFICHAGE CONFIGURATION
        afficherGrilleSolution(grille); // J'ai renommé pour bien distinguer
        afficherGroupements(grille);

        // 4. SIMULATION DE JEU
        System.out.println("\n-> Le joueur joue 2 en (0,0) et 1 en (1,0)...");
        grille.getCase(0, 0).setValeur(2);
        grille.getCase(1, 0).setValeur(1);

        // --- AJOUT ICI : AFFICHAGE DE CE QUE LE JOUEUR A ÉCRIT ---
        afficherGrilleJoueur(grille); 
        // ---------------------------------------------------------

        // VÉRIFICATION
        System.out.println("-> Vérification Cage '3+' (2+1) : " + (cage3Plus.groupementValide() ? "OK" : "ERREUR"));
    }

    // --- OUTILS ---

    private static int[][] transposer(int[][] lignes) {
        int w = lignes[0].length;
        int h = lignes.length;
        int[][] cols = new int[w][h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                cols[x][y] = lignes[y][x];
            }
        }
        return cols;
    }

    // Affiche la solution (triche)
    private static void afficherGrilleSolution(Grille g) {
        System.out.println("\n[ SOLUTION COMPLETE (Triche) ]");
        System.out.println("    0 1 2 3 (x)");
        for (int y = 0; y < g.getTaille(); y++) {
            System.out.print(y + " | ");
            for (int x = 0; x < g.getTaille(); x++) {
                System.out.print(g.getCase(x, y).getSolution() + " ");
            }
            System.out.println();
        }
        System.out.println("(y)\n");
    }

    // NOUVELLE MÉTHODE : Affiche les valeurs entrées par le joueur
    private static void afficherGrilleJoueur(Grille g) {
        System.out.println("[ GRILLE DU JOUEUR (État actuel) ]");
        System.out.println("    0 1 2 3 (x)");
        System.out.println("  +---------+");
        for (int y = 0; y < g.getTaille(); y++) {
            System.out.print(y + " | ");
            for (int x = 0; x < g.getTaille(); x++) {
                int val = g.getCase(x, y).getValeur();
                // Si la valeur est 0 (vide), on affiche un point '.' pour la lisibilité
                String affichage = (val == 0) ? "." : String.valueOf(val);
                System.out.print(affichage + " ");
            }
            System.out.println("|");
        }
        System.out.println("  +---------+");
        System.out.println("(y)\n");
    }

    private static void afficherGroupements(Grille g) {
        System.out.println("[ GROUPEMENTS ]");
        for (GroupementCases gc : g.getListeGroupements()) {
            System.out.print("• " + gc.getResultatCible() + gc.getOperation().getSymbole() + " : ");
            for (Case c : gc.getListeCases()) {
                System.out.print("(" + c.getX() + "," + c.getY() + ") ");
            }
            System.out.println();
        }
    }
    
    private static void afficherTitre(String s) {
        System.out.println("=== " + s + " ===");
    }
}