package fr.univ.calcudoku.service.aide.visitor;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;

/**
 * Visiteur qui détermine si les cases d'un bloc sont parfaitement alignées.
 */
public class VisiteurAlignementBloc implements VisiteurGrille {
    private boolean aligneLigne = true;
    private boolean aligneColonne = true;
    private int xCommun = -1;
    private int yCommun = -1;

    @Override
    public void visiter(Grille g) {}

    @Override
    public void visiter(GroupementCases groupement) {
        if (groupement.getListeCases().isEmpty()) return;

        xCommun = groupement.getListeCases().get(0).getX();
        yCommun = groupement.getListeCases().get(0).getY();

        for (Case c : groupement.getListeCases()) {
            if (c.getX() != xCommun) aligneColonne = false;
            if (c.getY() != yCommun) aligneLigne = false;
        }
    }

    @Override
    public void visiter(Case c) {}

    public boolean isAligneLigne() { return aligneLigne; }
    public boolean isAligneColonne() { return aligneColonne; }
    public int getXCommun() { return xCommun; }
    public int getYCommun() { return yCommun; }
}