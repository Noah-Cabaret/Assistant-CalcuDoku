package fr.univ.calcudoku.model;

import java.util.HashSet;
//import java.util.Set;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

public class Case {

    private final int x;
    private final int y;
    private final IntegerProperty valeur;
    private final int solution;
    private GroupementCases groupement;

    private final ObservableSet<Integer> annotations = FXCollections.observableSet(new HashSet<>());

    public Case(int x,int y,int solution){
        this.y = y;
        this.x = x;
        this.valeur = new SimpleIntegerProperty(0);
        this.solution = solution;
    }
    public boolean estCorrecte() {
        return valeur.get() == solution;
    }

    public int getX(){ return x;}
    public int getY(){ return y;}

    public int getSolution(){ return solution;}

    public int getValeur(){ return valeur.get();}
    public void setValeur(int valeur){this.valeur.set(valeur);}
    public IntegerProperty valeurProperty(){ return valeur;}

    public void setGroupement(GroupementCases g) { this.groupement = g; }
    public GroupementCases getGroupement() { return groupement; }

    public void basculerNote(int n) {
        if (annotations.contains(n)) {
            annotations.remove(n); 
        } else {
            annotations.add(n);   
        }
    }

    public void effacerNotes() {
        annotations.clear();
    }

    public void ajouterNote(int note) {
        annotations.add(note);
    }

    public ObservableSet<Integer> getNotes() {
        return annotations; 
    }

    @Override
    public String toString() {
        return "Case[" + x + "," + y + "] Val=" + getValeur();
    }
}