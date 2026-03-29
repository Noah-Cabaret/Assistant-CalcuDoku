package fr.univ.calcudoku.service.aide.visitor;

import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Case;

/**
 * Interface définissant les méthodes de visite pour les composants de la grille.
 */
public interface VisiteurGrille {
    void visiter(Grille g);
    void visiter(GroupementCases groupement);
    void visiter(Case c);
}