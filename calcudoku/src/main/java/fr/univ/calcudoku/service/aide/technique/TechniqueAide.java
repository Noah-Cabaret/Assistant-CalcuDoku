package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.Indice;

/**
 * Interface définissant le contrat pour toutes les techniques de résolution.
 * Chaque technique doit pouvoir analyser la grille et proposer une aide progressive.
 */
public interface TechniqueAide {
    /**
     * Analyse la grille et retourne un Indice contenant des messages progressifs 
     * si la technique est applicable.
     * * @param grille La grille actuelle à analyser.
     * @return L'Indice trouvé (avec ses différents niveaux de messages), 
     * ou null si la technique ne détecte aucune opportunité.
     */
    Indice analyser(Grille grille);
}