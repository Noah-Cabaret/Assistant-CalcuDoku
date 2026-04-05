package fr.univ.calcudoku.model;

import java.util.ArrayList;
import java.util.List;
import fr.univ.calcudoku.service.aide.visitor.VisiteurGrille;

/**
 * Représente la grille principale du jeu Calcudoku.
 * Gère les cases, les groupements et la logique de validation.
 */
public class Grille implements ElementVisitable {
    private final int taille;
    private final List<GroupementCases> listeGroupements;
    private final Case[][] matriceGrille;

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

    public void ajouterGroupement(GroupementCases groupement){
        this.listeGroupements.add(groupement);
    }

    public List<GroupementCases> getListeGroupements() {
        return listeGroupements;
    }

    public Case getCase(int x,int y){
        if(x < 0 || x >= taille || y < 0 || y >= taille){
            throw new IllegalArgumentException("Coordonnées hors grille : " + x + "," + y);
        }
        return matriceGrille[x][y];
    }

    public void setCase(int x, int y, Case nouvelleCase) {
        if(x >= 0 && x < taille && y >= 0 && y < taille) {
            this.matriceGrille[x][y] = nouvelleCase;
        }
    }

    public int getTaille(){
        return taille;
    }

    public boolean estGagnee(){
        for (int x = 0; x < taille; x++) {
            for (int y = 0; y < taille; y++) {
                if(!matriceGrille[x][y].estCorrecte()) return false;
            }
        }
        return true;
    }

    public boolean estCoupValide(int x, int y, int valeur) {
        if (valeur == 0) return true; 

        for (int i = 0; i < taille; i++) {
            if (i != x && matriceGrille[i][y].getValeur() == valeur) return false; 
        }

        for (int j = 0; j < taille; j++) {
            if (j != y && matriceGrille[x][j].getValeur() == valeur) return false; 
        }
        return true;
    }

    @Override
    public void accepter(VisiteurGrille visiteur) {
        visiteur.visiter(this);
        for (GroupementCases g : listeGroupements) g.accepter(visiteur);
        for (int x = 0; x < taille; x++) {
            for (int y = 0; y < taille; y++) {
                matriceGrille[x][y].accepter(visiteur);
            }
        }
    }
    
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