package fr.univ.calcudoku.service.aide.visitor;

import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Case;
import java.util.ArrayList;
import java.util.List;

public class VisiteurChercheurBlocN implements VisiteurGrille {
    
    private final int tailleCible;
    private List<GroupementCases> blocsTrouves = new ArrayList<>();

    // Le constructeur prend la taille recherchée en paramètre
    public VisiteurChercheurBlocN(int tailleCible) {
        this.tailleCible = tailleCible;
    }

    @Override
    public void visiter(Grille g) {
    }

    @Override
    public void visiter(GroupementCases groupement) {
        if (groupement.getListeCases().size() == tailleCible) {
            blocsTrouves.add(groupement);
        }
    }

    @Override
    public void visiter(Case c) {
    }

    public List<GroupementCases> getBlocsTrouves() {
        return blocsTrouves;
    }
}