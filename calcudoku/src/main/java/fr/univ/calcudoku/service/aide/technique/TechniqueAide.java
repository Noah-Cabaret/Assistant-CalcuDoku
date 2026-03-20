package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.Indice;

public interface TechniqueAide {
    /**
     * Analyse la grille et retourne un Indice si la technique est applicable.
     * @param grille La grille actuelle à analyser
     * @return L'Indice trouvé, ou null si la technique ne trouve rien.
     */
    Indice analyser(Grille grille);
}