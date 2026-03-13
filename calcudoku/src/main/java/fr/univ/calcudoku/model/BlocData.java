package fr.univ.calcudoku.model;

import java.util.Map;

/**
 * Représente les données d'un bloc (cage) dans le format JSON.
 * Contient le résultat cible, l'opération et les coordonnées avec solutions des cases.
 */
public class BlocData {
    /** Résultat cible de l'opération */
    public int result;
    /** Symbole de l'opération (+, -, *, /) */
    public String op;
    /** Map associant les coordonnées (x,y) aux valeurs solutions */
    public Map<String, Integer> nums;
}