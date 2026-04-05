package fr.univ.calcudoku.service.aide.visitor;

import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Case;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Visiteur qui vérifie si les cases d'un groupement contiennent 
 * exactement les annotations correspondantes à une combinaison unique.
 */
public class VisiteurManqueAnnotations implements VisiteurGrille {
    
    private final List<Integer> combinaisonUnique;
    private boolean manqueAnnotations = false;
    private Set<Integer> chiffresRequis;

    public VisiteurManqueAnnotations(List<Integer> combinaisonUnique) {
        this.combinaisonUnique = combinaisonUnique;
    }

    @Override
    public void visiter(Grille g) {}

    @Override
    public void visiter(GroupementCases groupement) {
        manqueAnnotations = false;
        
        List<Integer> restants = new ArrayList<>(combinaisonUnique);
        for (Case c : groupement.getListeCases()) {
            if (c.getValeur() != 0) {
                restants.remove(Integer.valueOf(c.getValeur()));
            }
        }
        
        chiffresRequis = new HashSet<>(restants);

        for (Case c : groupement.getListeCases()) {
            c.accepter(this);
        }
    }

    @Override
    public void visiter(Case c) {
        if (c.getValeur() == 0) {
            if (!c.getNotes().equals(chiffresRequis)) {
                manqueAnnotations = true;
            }
        }
    }

    public boolean isManqueAnnotations() {
        return manqueAnnotations;
    }
}