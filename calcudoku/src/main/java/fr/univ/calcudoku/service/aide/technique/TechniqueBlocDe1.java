package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.model.Case;
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
            int reponseExacte = bloc.getResultatCible();
            int valeurJoueur = caseUnique.getValeur();

            if (valeurJoueur != reponseExacte) {
                Map<Case, Integer> reponses = new HashMap<>();
                reponses.put(caseUnique, reponseExacte);

                boolean contientErreur = (valeurJoueur != 0);

                String nom = "Bloc à case unique";
                String message = "Ce bloc ne contient qu'une seule case. Il n'y a aucun calcul à faire : " +
                                 "la réponse est simplement le nombre indiqué dans le coin supérieur gauche du bloc !";
                
                return new Indice(nom, message, bloc.getListeCases(), reponses, contientErreur);
            }
        }
        
        return null; 
    }
}