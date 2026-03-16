package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.service.aide.visitor.VisiteurGrille;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Technique du Bloc Unique (Cage Logic).
 * Analyse les cages (groupements) de 2 cases ou plus pour trouver 
 * celles qui n'ont qu'une seule combinaison mathématique possible.
 */
public class TechniqueBlocUnique implements TechniqueAide, VisiteurGrille {

    private Indice indiceTrouve;

    // ==========================================
    // IMPLÉMENTATION STRATÉGIE
    // ==========================================

    @Override
    public Indice analyser(Grille grille) {
        this.indiceTrouve = null;
        
        // Le Visiteur va parcourir la grille et déléguer l'analyse des blocs 
        // à la méthode visiter(GroupementCases) ci-dessous.
        grille.accepter(this);

        return indiceTrouve;
    }

    // ==========================================
    // IMPLÉMENTATION VISITEUR
    // ==========================================

    @Override
    public void visiter(Grille g) {
        // Inutile ici, on se concentre sur les Groupements
    }

    @Override
    public void visiter(Case c) {
        // Inutile ici, on analyse les blocs entiers
    }

    @Override
    public void visiter(GroupementCases groupement) {
        // Si on a déjà trouvé une aide, on arrête l'exploration
        if (indiceTrouve != null) {
            return;
        }

        List<Case> casesDuBloc = groupement.getListeCases();

        // NOUVEAU FILTRE : On ignore volontairement les cages de 1 seule case !
        if (casesDuBloc.size() <= 1) {
            return;
        }

        // On vérifie d'abord s'il reste des cases vides dans ce bloc.
        // Si le bloc est déjà entièrement rempli, on passe au suivant.
        boolean contientCasesVides = false;
        for (Case c : casesDuBloc) {
            if (c.getValeur() == 0) {
                contientCasesVides = true;
                break;
            }
        }
        if (!contientCasesVides) return;

        // --------------------------------------------------------
        // ANALYSE : Bloc avec UNE SEULE combinaison mathématique
        // --------------------------------------------------------
        List<List<Integer>> combinaisonsPossibles = groupement.getCombinaisonsMaths();
        
        // S'il n'y a qu'une seule façon d'atteindre le résultat avec l'opération donnée
        if (combinaisonsPossibles != null && combinaisonsPossibles.size() == 1) {
            
            List<Integer> lUniqueCombinaison = combinaisonsPossibles.get(0);
            
            // On prépare le texte avec les chiffres de la combinaison
            StringBuilder chiffresTexte = new StringBuilder("{");
            for (int i = 0; i < lUniqueCombinaison.size(); i++) {
                chiffresTexte.append(lUniqueCombinaison.get(i));
                if (i < lUniqueCombinaison.size() - 1) chiffresTexte.append(", ");
            }
            chiffresTexte.append("}");

            String message = "Regardez ce bloc (Cible : " + groupement.getResultatCible() 
                           + groupement.getOperation().getSymbole() + ").\n"
                           + "Pour atteindre ce résultat avec " + casesDuBloc.size() + " cases, "
                           + "il n'existe mathématiquement qu'une seule combinaison de chiffres : " + chiffresTexte.toString() + ".\n"
                           + "Vous devez placer ces chiffres dans ce bloc !";

            // On ne donne pas la position exacte des chiffres (c'est à l'utilisateur de réfléchir),
            // donc on passe une Map de solutions vide. L'aide se contentera de mettre le bloc en surbrillance.
            Map<Case, Integer> solutionsVides = new HashMap<>();

            this.indiceTrouve = new Indice("Combinaison Unique", message, casesDuBloc, solutionsVides, false);
        }
    }
}