package fr.univ.calcudoku.service.aide.visitor;

import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Case;

public interface VisiteurGrille {
    public void visiter(Grille g);
    public void visiter(GroupementCases groupement);
    public void visiter(Case c);
}