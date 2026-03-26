package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.service.aide.visitor.VisiteurChercheurBlocN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TechniqueBlocDe1 implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        VisiteurChercheurBlocN chercheur = new VisiteurChercheurBlocN(1);
        grille.accepter(chercheur);

        List<Indice> indicesErreurs = new ArrayList<>();
        List<Indice> indicesNormaux = new ArrayList<>();

        for (GroupementCases bloc : chercheur.getBlocsTrouves()) {
            Case caseUnique = bloc.getListeCases().get(0);
            int reponseExacte = bloc.getResultatCible();
            int valeurJoueur = caseUnique.getValeur();

            if (valeurJoueur != reponseExacte) {
                boolean contientErreur = (valeurJoueur != 0 && valeurJoueur != caseUnique.getSolution());
                Map<Case, Integer> reponses = new HashMap<>(); 

                if (contientErreur) {
                    String msg = "Erreur détectée ! Ce bloc ne contient qu'une seule case, elle doit donc obligatoirement correspondre au résultat cible.";
                    indicesErreurs.add(new Indice("Bloc à case unique", msg, bloc.getListeCases(), reponses, true));
                } else {
                    String msg = "En commençant par les données : Certains blocs sont constitués d'un seul carré. Il s'agit d'une donnée, le nombre à placer est simplement le résultat affiché dans le coin !";
                    indicesNormaux.add(new Indice("Bloc à case unique", msg, bloc.getListeCases(), reponses, false));
                }
            }
        }

        Random rand = new Random();
        if (!indicesErreurs.isEmpty()) return indicesErreurs.get(rand.nextInt(indicesErreurs.size()));
        if (!indicesNormaux.isEmpty()) return indicesNormaux.get(rand.nextInt(indicesNormaux.size()));
        
        return null; 
    }
}