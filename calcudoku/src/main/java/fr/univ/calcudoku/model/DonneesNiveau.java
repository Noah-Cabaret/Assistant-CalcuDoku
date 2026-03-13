package fr.univ.calcudoku.model;

import java.util.List;

/**
 * Représente les données d'un niveau chargé à partir d'un fichier JSON.
 * Contient la dimension de la grille, le temps limite et la liste des blocs.
 */
public class DonneesNiveau {
    /** Temps limite en secondes pour compléter le niveau */
    public int temps;
    /** Dimension de la grille (ex: 6x6, 9x9) */
    public int dim;
    /** Liste des blocs (cages) du niveau */
    public List<BlocData> blocs;
}