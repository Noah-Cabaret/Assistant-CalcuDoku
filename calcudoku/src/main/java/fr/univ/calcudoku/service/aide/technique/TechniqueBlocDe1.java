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

/**
 * Technique d'aide analysant les blocs constitués d'une seule case.
 * Dans ces blocs, la valeur de la case correspond obligatoirement au résultat cible.
 */
public class TechniqueBlocDe1 implements TechniqueAide {

    /**
     * Analyse la grille et retourne un Indice progressif pour guider le joueur
     * sur la résolution des blocs de taille 1.
     * * @param grille La grille actuelle à analyser
     * @return L'Indice contenant les messages progressifs, ou null si la technique ne s'applique pas
     */
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
                List<String> messages = new ArrayList<>();

                if (contientErreur) {
                    // Progression de l'indice en cas d'erreur du joueur
                    messages.add("Attention, il semble qu'il y ait une erreur dans l'un de vos blocs. Observez bien sa taille.");
                    messages.add("Rappelez-vous : un bloc qui ne contient qu'une seule case possède une règle très simple pour sa solution.");
                    messages.add("Erreur détectée ! La case en surbrillance est seule dans son bloc, elle doit donc obligatoirement correspondre au résultat cible.");
                    indicesErreurs.add(new Indice("Bloc à case unique", messages, bloc.getListeCases(), reponses, true));
                } else {
                    // Progression de l'indice de manière générale (case vide)
                    messages.add("Avez-vous remarqué les blocs constitués d'une seule case sur la grille ?");
                    messages.add("Dans un bloc d'une seule case, aucun calcul n'est nécessaire. Le résultat est affiché de manière explicite.");
                    messages.add("Vous pouvez remplir la case en surbrillance avec son chiffre cible sans faire de calcul !");
                    indicesNormaux.add(new Indice("Bloc à case unique", messages, bloc.getListeCases(), reponses, false));
                }
            }
        }

        Random rand = new Random();
        if (!indicesErreurs.isEmpty()) return indicesErreurs.get(rand.nextInt(indicesErreurs.size()));
        if (!indicesNormaux.isEmpty()) return indicesNormaux.get(rand.nextInt(indicesNormaux.size()));
        
        return null; 
    }
}