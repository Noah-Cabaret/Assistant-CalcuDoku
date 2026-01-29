package fr.univ.calcudoku.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Case {

    private final int x;
    private final int y;
    private final IntegerProperty valeur;

    private GroupementCase groupement;

    public Case(int x,int y){
        this.y = y;
        this.x = x;
        this.valeur = new SimpleIntegerProperty(0);
    }

    public int getX(){ return x;}
    public int getY(){ return y;}

    public int getValeur(){ return valeur.get();}
    public void setValeur(int valeur){this.valeur.set(valeur);}
    public IntegerProperty valeuProperty(){ return valeur;}



}


