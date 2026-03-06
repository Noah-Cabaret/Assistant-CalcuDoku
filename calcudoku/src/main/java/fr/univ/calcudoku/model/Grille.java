package fr.univ.calcudoku.model;

import java.util.ArrayList;
import java.util.List;

public class Grille {
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
    
    public void ajouterGroupement(GroupementCases groupement){
        this.listeGroupements.add(groupement);
    }
    public List<GroupementCases> getListeGroupements() {
        return listeGroupements;
    }

    public Case getCase(int x,int y){
        if(x < 0 || x >= taille || y < 0 || y >= taille){
            throw new IllegalArgumentException("Coordonnées hors grille : " + x + "," + y);     //throw renvoies une erreur propre
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

    //verif sur ligne et colonne 
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
