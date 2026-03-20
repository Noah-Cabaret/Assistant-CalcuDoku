package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.service.aide.visitor.VisiteurGrille;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TechniqueBlocUnique implements TechniqueAide, VisiteurGrille {

    private Indice indiceTrouve;

    @Override
    public Indice analyser(Grille grille) {
        this.indiceTrouve = null;
        grille.accepter(this);
        return indiceTrouve;
    }

    @Override
    public void visiter(Grille g) {}

    @Override
    public void visiter(Case c) {}

    @Override
    public void visiter(GroupementCases groupement) {
        if (indiceTrouve != null) {
            return;
        }

        List<Case> casesDuBloc = groupement.getListeCases();

        if (casesDuBloc.size() <= 1) {
            return;
        }

        // MODIFICATION (Filtre anti-doublon) : 
        // Si le bloc est quasi-plein (1 seule case vide ou 0), ce n'est plus un problème de combinaison,
        // c'est un problème de déduction finale. On laisse "TechniqueDerniereCaseBloc" agir.
        int nbCasesVides = 0;
        for (Case c : casesDuBloc) {
            if (c.getValeur() == 0) nbCasesVides++;
        }
        
        if (nbCasesVides <= 1) {
            return;
        }

        List<List<Integer>> combinaisonsPossibles = groupement.getCombinaisonsMaths();
        
        if (combinaisonsPossibles != null && combinaisonsPossibles.size() == 1) {
            List<Integer> lUniqueCombinaison = combinaisonsPossibles.get(0);
            
            boolean contientErreur = false;
            List<Integer> chiffresRestants = new ArrayList<>(lUniqueCombinaison);

            // Vérification des valeurs déjà posées pour détecter une erreur
            for (Case c : casesDuBloc) {
                if (c.getValeur() != 0) {
                    if (chiffresRestants.contains(c.getValeur())) {
                        chiffresRestants.remove((Integer) c.getValeur());
                    } else {
                        // Le joueur a placé un chiffre qui n'est pas dans la combinaison stricte
                        contientErreur = true;
                        break;
                    }
                }
            }

            if (!contientErreur && chiffresRestants.isEmpty()) {
                return;
            }

            StringBuilder chiffresTexte = new StringBuilder("{");
            for (int i = 0; i < lUniqueCombinaison.size(); i++) {
                chiffresTexte.append(lUniqueCombinaison.get(i));
                if (i < lUniqueCombinaison.size() - 1) chiffresTexte.append(", ");
            }
            chiffresTexte.append("}");

            String nom = "Combinaison Unique";
            String message;

            if (contientErreur) {
                message = "Erreur détectée ! Ce bloc (Cible : " + groupement.getResultatCible() + groupement.getOperation().getSymbole() + ") "
                        + "ne peut être résolu qu'avec la combinaison stricte : " + chiffresTexte.toString() + ".\n"
                        + "Un ou plusieurs chiffres que vous avez placés sont donc incorrects.";
            } else {
                message = "Regardez ce bloc (Cible : " + groupement.getResultatCible() + groupement.getOperation().getSymbole() + ").\n"
                        + "Pour atteindre ce résultat, il n'existe mathématiquement qu'une seule combinaison de chiffres : " + chiffresTexte.toString() + ".\n"
                        + "Vous devez placer ces chiffres dans ce bloc !";
            }

            Map<Case, Integer> solutionsVides = new HashMap<>();

            this.indiceTrouve = new Indice(nom, message, casesDuBloc, solutionsVides, contientErreur);
        }
    }
}