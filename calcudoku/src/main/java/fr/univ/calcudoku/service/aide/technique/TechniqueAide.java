package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.Indice;

public interface TechniqueAide {
    Indice analyser(Grille grille);
}
