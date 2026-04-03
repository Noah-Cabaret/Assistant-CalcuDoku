package fr.univ.calcudoku.service.aide.visitor;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Visiteur qui parcourt un axe (ligne ou colonne) pour trouver les cases 
 * possédant des annotations interdites, tout en ignorant un bloc spécifique.
 */
public class VisiteurScannerAxe implements VisiteurGrille {
    private final int indexAxe;
    private final boolean estLigne;
    private final Collection<Case> casesAIgnorer;
    private final Collection<Integer> chiffresInterdits;
    private final List<Case> casesANettoyer = new ArrayList<>();

    public VisiteurScannerAxe(int indexAxe, boolean estLigne, Collection<Case> casesAIgnorer, Collection<Integer> chiffresInterdits) {
        this.indexAxe = indexAxe;
        this.estLigne = estLigne;
        this.casesAIgnorer = casesAIgnorer;
        this.chiffresInterdits = chiffresInterdits;
    }

    @Override
    public void visiter(Grille g) {
        for (int i = 0; i < g.getTaille(); i++) {
            int x = estLigne ? i : indexAxe;
            int y = estLigne ? indexAxe : i;
            g.getCase(x, y).accepter(this);
        }
    }

    @Override
    public void visiter(GroupementCases groupement) {}

    @Override
    public void visiter(Case c) {
        if (!casesAIgnorer.contains(c) && c.getValeur() == 0) {
            for (int chiffre : chiffresInterdits) {
                if (c.getNotes().contains(chiffre)) {
                    casesANettoyer.add(c);
                    break;
                }
            }
        }
    }

    public List<Case> getCasesANettoyer() {
        return casesANettoyer;
    }
}