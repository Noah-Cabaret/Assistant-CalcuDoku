package fr.univ.calcudoku.model;

import java.util.HashSet;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import fr.univ.calcudoku.service.aide.visitor.VisiteurGrille;

/**
 * Représente une case de la grille de Calcudoku.
 * Gère sa position, sa valeur actuelle, la solution et les annotations de l'utilisateur.
 */

public class Case implements ElementVisitable {

    private final int x;
    private final int y;
    private final IntegerProperty valeur;
    private final int solution;
    private GroupementCases groupement;
    private boolean validee = false; // utilisée pour la validation et le système de points

    private final ObservableSet<Integer> annotations = FXCollections.observableSet(new HashSet<>());

    /**
     * Constructeur d'une case à une position donnée.
     * @param x la coordonnée x
     * @param y la coordonnée y
     * @param solution la valeur solution de cette case
     */
    public Case(int x,int y,int solution){
        this.y = y;
        this.x = x;
        this.valeur = new SimpleIntegerProperty(0);
        this.solution = solution;
    }

    /**
     * Constructeur de copie. Crée une copie indépendante d'une case.
     * @param source la case à copier
     */
    public Case(Case source) {
        this.x = source.getX();
        this.y = source.getY();
        this.solution = source.getSolution(); 
        
        this.valeur = new SimpleIntegerProperty(source.getValeur());
        this.annotations.addAll(source.getNotes());
    }
    
    /**
     * Vérifie si la valeur saisie est correcte.
     * @return true si la valeur correspond à la solution
     */
    public boolean estCorrecte() {
        return valeur.get() == solution;
    }

    /**
     * Retourne la coordonnée x de la case.
     * @return la coordonnée x
     */
    public int getX(){ return x;}
    /**
     * Retourne la coordonnée y de la case.
     * @return la coordonnée y
     */
    public int getY(){ return y;}

    /**
     * Retourne la solution pour cette case.
     * @return la valeur solution
     */
    public int getSolution(){ return solution;}

    /**
     * Retourne la valeur actuellement saisie.
     * @return la valeur de la case
     */
    public int getValeur(){ return valeur.get();}
    /**
     * Définit la valeur de la case.
     * @param valeur la nouvelle valeur
     */
    public void setValeur(int valeur){this.valeur.set(valeur);}
    /**
     * Retourne la propriété observable de la valeur.
     * @return la propriété IntegerProperty
     */
    public IntegerProperty valeurProperty(){ return valeur;}

    /**
     * Assigne le groupement (cage) auquel appartient cette case.
     * @param g le groupement
     */
    public void setGroupement(GroupementCases g) { this.groupement = g; }
    /**
     * Retourne le groupement (cage) auquel appartient cette case.
     * @return le groupement
     */
    public GroupementCases getGroupement() { return groupement; }

    /**
     * Assigne le nouvel état de validation de la case.
     * @param newValidee le nouvel état de validation
     */
    public void setValidee(boolean newValidee) { this.validee = newValidee; }
    /**
     * Retourne l'état de validation actuel de la case.
     * @return l'état actuel de validation
     */
    public boolean getValidee() { return validee; }

    /**
     * Bascule la visibilité d'une annotation (note) pour la case.
     * @param n le numéro de l'annotation à basculer (1-9)
     */
    public void basculerNote(int n) {
        if (annotations.contains(n)) {
            annotations.remove(n); 
        } else {
            annotations.add(n);   
        }
    }

    /**
     * Efface toutes les annotations de la case.
     */
    public void effacerNotes() {
        annotations.clear();
    }

    /**
     * Ajoute une annotation à la case.
     * @param note le numéro de l'annotation (1-9)
     */
    public void ajouterNote(int note) {
        annotations.add(note);
    }

    /**
     * Retourne l'ensemble observable des annotations de la case.
     * @return l'ensemble des numéros notés
     */
    public ObservableSet<Integer> getNotes() {
        return annotations; 
    }

    /**
     * Retourne une représentation textuelle de la case.
     * @return la chaîne [x,y] Val=valeur
     */
    @Override
    public String toString() {
        return "Case[" + x + "," + y + "] Val=" + getValeur();
    }

    @Override
    public void accepter(VisiteurGrille visiteur) {
        visiteur.visiter(this);
    }
}
