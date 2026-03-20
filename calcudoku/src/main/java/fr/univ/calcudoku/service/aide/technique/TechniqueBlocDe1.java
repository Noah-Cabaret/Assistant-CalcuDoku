package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.service.aide.visitor.VisiteurChercheurBlocN;

import java.util.HashMap;
import java.util.Map;

public class TechniqueBlocDe1 implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        VisiteurChercheurBlocN chercheur = new VisiteurChercheurBlocN(1);
        grille.accepter(chercheur);

        for (GroupementCases bloc : chercheur.getBlocsTrouves()) {
            Case caseUnique = bloc.getListeCases().get(0);
            
            // LOGIQUE MATHÉMATIQUE : La réponse est le résultat cible
            int reponseExacte = bloc.getResultatCible();
            int valeurJoueur = caseUnique.getValeur();

            if (valeurJoueur != reponseExacte) {
                // VÉRIFICATION ERREUR : La case est remplie mais fausse
                boolean contientErreur = (valeurJoueur != 0 && valeurJoueur != caseUnique.getSolution());

                Map<Case, Integer> reponses = new HashMap<>();
                reponses.put(caseUnique, reponseExacte);

                String msg = contientErreur ? 
                    "Erreur détectée ! Ce bloc ne contient qu'une seule case, elle doit donc obligatoirement valoir " + reponseExacte + "." :
                    "Ce bloc ne contient qu'une seule case. Il n'y a aucun calcul à faire : la réponse est simplement le nombre indiqué !";
                
                return new Indice("Bloc à case unique", msg, bloc.getListeCases(), reponses, contientErreur);
            }
        }
        return null; 
    }
}