package fr.univ.calcudoku.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente la grille principale du jeu Calcudoku.
 * Gère les cases, les groupements et la logique de validation.
 */
public class Grille {
    /** Dimension de la grille (5..9) */
    private final int taille;
    /** Liste de tous les groupements (cages) de la grille */
    private final List<GroupementCases> listeGroupements;
    /** Matrice 2D contenant toutes les cases */
    private final Case[][] matriceGrille;

    /**
     * Constructeur simple créant une grille vide.
     * @param taille la dimension de la grille
     */
    public Grille(int taille) {
        this.taille = taille;
        this.matriceGrille = new Case[taille][taille];
        this.listeGroupements = new ArrayList<>();

        for (int x = 0; x < taille; x++) {
            for (int y = 0; y < taille; y++) {
                matriceGrille[x][y] = new Case(x, y, 0); 
            }
        }
    }

    /**
     * Constructeur créant une grille avec les solutions et les valeurs initiales.
     * @param taille la dimension de la grille
     * @param matriceSolution la matrice des solutions
     * @param matriceDepart la matrice des valeurs initiales (peut être null)
     */
    public Grille(int taille, int[][] matriceSolution, int[][] matriceDepart) {
        this.taille = taille;
        this.matriceGrille = new Case[taille][taille];
        this.listeGroupements = new ArrayList<>();

        for (int x = 0; x < taille; x++) {
            for (int y = 0; y < taille; y++) {
                int solution = matriceSolution[x][y];
                int valeurInitiale = (matriceDepart == null) ? 0 : matriceDepart[x][y];
                
                matriceGrille[x][y] = new Case(x, y, solution);
                matriceGrille[x][y].setValeur(valeurInitiale); 
            }
        }
    }

    /**
     * Constructeur de copie. Crée une copie indépendante de la grille.
     * @param source la grille à copier
     */
    public Grille(Grille source) {
        this.taille = source.getTaille();
        this.matriceGrille = new Case[taille][taille];
        this.listeGroupements = new ArrayList<>();

        for (int x = 0; x < taille; x++) {
            for (int y = 0; y < taille; y++) {
                this.matriceGrille[x][y] = new Case(source.getCase(x, y));
            }
        }

        for (GroupementCases gSource : source.getListeGroupements()) {
            
            GroupementCases gNouveau = new GroupementCases(gSource);
            
            for (Case cSource : gSource.getListeCases()) {
                Case cNouveau = this.getCase(cSource.getX(), cSource.getY());
                
                gNouveau.ajouterCase(cNouveau); 
                cNouveau.setGroupement(gNouveau);
            }
            this.ajouterGroupement(gNouveau);
        }
    }

    /**
     * Ajoute un groupement à la grille.
     * @param groupement le groupement (cage) à ajouter
     */
    public void ajouterGroupement(GroupementCases groupement){
        this.listeGroupements.add(groupement);
    }
    /**
     * Retourne la liste de tous les groupements de la grille.
     * @return la liste des groupements
     */
    public List<GroupementCases> getListeGroupements() {
        return listeGroupements;
    }

    /**
     * Récupère la case aux coordonnées spécifiées.
     * @param x la coordonnée x
     * @param y la coordonnée y
     * @return la case demandée
     * @throws IllegalArgumentException si les coordonnées sont hors grille
     */
    public Case getCase(int x,int y){
        if(x < 0 || x >= taille || y < 0 || y >= taille){
            throw new IllegalArgumentException("Coordonnées hors grille : " + x + "," + y);     //throw renvoies une erreur propre
        }
        return matriceGrille[x][y];
    }
    /**
     * Remplace la case à une position donnée.
     * @param x la coordonnée x
     * @param y la coordonnée y
     * @param nouvelleCase la nouvelle case
     */
    public void setCase(int x, int y, Case nouvelleCase) {
        if(x >= 0 && x < taille && y >= 0 && y < taille) {
            this.matriceGrille[x][y] = nouvelleCase;
        }
    }

    /**
     * Retourne la dimension de la grille.
     * @return la taille (nombre de lignes/colonnes)
     */
    public int getTaille(){
        return taille;
    }

    /**
     * Vérifie si la grille est complètement remplie et correcte (partie gagnée).
     * @return true si toutes les cases sont correctes
     */
    public boolean estGagnee(){
        for (int x = 0; x < taille; x++) {
            for (int y = 0; y < taille; y++) {
                if(!matriceGrille[x][y].estCorrecte()) return false;
            }
        }
        return true;
    }

    /**
     * Vérifie si un coup est valide (pas de doublons sur ligne ou colonne).
     * @param x la coordonnée x
     * @param y la coordonnée y
     * @param valeur la valeur à placer
     * @return true si le coup est valide
     */
    public boolean estCoupValide(int x, int y, int valeur) {
    if (valeur == 0) return true; 

    for (int i = 0; i < taille; i++) {
        if (i != x && matriceGrille[i][y].getValeur() == valeur) {
            return false; 
        }
    }

    for (int j = 0; j < taille; j++) {
        if (j != y && matriceGrille[x][j].getValeur() == valeur) {
            return false; 
        }
    }
    return true;
}
    
    /**
     * Retourne une représentation textuelle de la grille.
     * @return la chaîne affichant la grille avec les valeurs actuelles
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < taille; y++) {
            for (int x = 0; x < taille; x++) {
                sb.append(matriceGrille[x][y].getValeur()).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    

}
