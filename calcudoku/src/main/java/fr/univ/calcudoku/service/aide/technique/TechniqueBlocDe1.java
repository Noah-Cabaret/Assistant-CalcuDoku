package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.service.aide.visitor.VisiteurChercheurBlocN;

import java.util.HashMap;
import java.util.Map;

/**
 * Technique : Données de départ.
 * Identifie les blocs d'une seule case qui n'ont besoin d'aucun calcul.
 */
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
                boolean contientErreur = (valeurJoueur != 0 && valeurJoueur != caseUnique.getSolution());

                Map<Case, Integer> reponses = new HashMap<>(); // Vide : ne donne plus la solution exacte

                String msg = contientErreur ? 
                    "Erreur détectée ! Ce bloc ne contient qu'une seule case, elle doit donc obligatoirement correspondre au résultat cible." :
                    "En commençant par les données : Certains blocs sont constitués d'un seul carré. Il s'agit d'une donnée, le nombre à placer est simplement le résultat affiché dans le coin !";
                
                return new Indice("Bloc à case unique", msg, bloc.getListeCases(), reponses, contientErreur);
            }
        }
        return null; 
    }
}