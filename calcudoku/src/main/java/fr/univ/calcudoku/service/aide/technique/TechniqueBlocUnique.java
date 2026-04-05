package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.service.aide.visitor.VisiteurManqueAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Technique d'aide qui encourage le joueur à annoter les blocs 
 * ne possédant qu'une seule combinaison mathématique possible.
 */
public class TechniqueBlocUnique implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        List<GroupementCases> blocsCibles = new ArrayList<>();
        List<Case> casesASurbriller = new ArrayList<>();

        for (GroupementCases bloc : grille.getListeGroupements()) {
            if (bloc.getListeCases().size() <= 1) continue;

            List<List<Integer>> combinaisonsPossibles = bloc.getCombinaisonsMaths();
            
            if (combinaisonsPossibles != null && combinaisonsPossibles.size() == 1) {
                
                boolean estResolu = true;
                for (Case c : bloc.getListeCases()) {
                    if (c.getValeur() == 0 || c.getValeur() != c.getSolution()) {
                        estResolu = false;
                        break;
                    }
                }
                if (estResolu) continue;

                List<Integer> combinaisonUnique = combinaisonsPossibles.get(0);
                VisiteurManqueAnnotations visiteur = new VisiteurManqueAnnotations(combinaisonUnique);
                visiteur.visiter(bloc);

                if (visiteur.isManqueAnnotations()) {
                    blocsCibles.add(bloc);
                    for (Case c : bloc.getListeCases()) {
                        if (c.getValeur() == 0) {
                            casesASurbriller.add(c);
                        }
                    }
                }
            }
        }

        if (blocsCibles.isEmpty()) return null;

        List<String> messages = new ArrayList<>();
        
        messages.add("Un ou plusieurs groupements n'ont qu'une seule combinaison possible. Placer ou corriger vos annotations vous permettrait d'y voir plus clair.");
        
        messages.add("Voici les cases concernées en surbrillance. Leurs annotations actuelles sont incomplètes ou incorrectes pour la seule combinaison possible du bloc.");

        return new Indice("Vérification des annotations", messages, casesASurbriller, new HashMap<>(), false);
    }
}