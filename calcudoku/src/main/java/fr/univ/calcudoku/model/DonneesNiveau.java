package fr.univ.calcudoku.model;

import java.util.List;
import fr.univ.calcudoku.challenge.Defi;

/**
 * Représente les données d'un niveau chargé à partir d'un fichier JSON.
 * Contient la dimension de la grille, le temps limite et la liste des blocs.
 */
public class DonneesNiveau {
    /** Temps limite en secondes pour compléter le niveau */
    public Double temps;
    /** Dimension de la grille (ex: 6x6, 9x9) */
    public int dim;
    /** Défi imposant des restrictions durant la partie */
    public Defi.TypeDefi defi;
    /** Nombre d'erreurs autorisées au maximum (utilisé si defi vaut SURVI) */
    public int vies;
    /** Liste des blocs (cages) du niveau */
    public List<BlocData> blocs;
}