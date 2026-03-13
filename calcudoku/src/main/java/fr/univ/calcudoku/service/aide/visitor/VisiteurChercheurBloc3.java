package fr.univ.calcudoku.service.aide.visitor;

import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Case;
import java.util.ArrayList;
import java.util.List;

public class VisiteurChercheurBloc3 implements VisiteurGrille {
    
    // État interne pour stocker les résultats de la fouille
    private List<GroupementCases> blocsDeTrois = new ArrayList<>();

    @Override
    public void visiter(Grille g) {
        // On ne cherche rien au niveau global de la grille
    }

    @Override
    public void visiter(GroupementCases groupement) {
        // On filtre uniquement les blocs faisant exactement 3 cases
        if (groupement.getListeCases().size() == 3) {
            blocsDeTrois.add(groupement);
        }
    }

    @Override
    public void visiter(Case c) {
        // On ignore les cases individuelles
    }

    // Méthode pour récupérer les données une fois la visite terminée
    public List<GroupementCases> getBlocsTrouves() {
        return blocsDeTrois;
    }
}