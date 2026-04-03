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
    public void visiter(Grille g) {
        // Non utilisé
    }

    @Override
    public void visiter(GroupementCases groupement) {
        manqueAnnotations = false;
        
        // 1. On détermine les chiffres de la combinaison qui ne sont pas encore placés
        List<Integer> restants = new ArrayList<>(combinaisonUnique);
        for (Case c : groupement.getListeCases()) {
            if (c.getValeur() != 0) {
                restants.remove(Integer.valueOf(c.getValeur()));
            }
        }
        
        // 2. Les annotations parfaites attendues sont les chiffres uniques restants
        chiffresRequis = new HashSet<>(restants);

        // 3. On vérifie chaque case du bloc
        for (Case c : groupement.getListeCases()) {
            c.accepter(this);
        }
    }

    @Override
    public void visiter(Case c) {
        if (c.getValeur() == 0) {
            // Si les notes de la case ne sont pas EXACTEMENT égales aux chiffres requis
            // (Soit il en manque, soit il y en a en trop)
            if (!c.getNotes().equals(chiffresRequis)) {
                manqueAnnotations = true;
            }
        }
    }

    public boolean isManqueAnnotations() {
        return manqueAnnotations;
    }
}