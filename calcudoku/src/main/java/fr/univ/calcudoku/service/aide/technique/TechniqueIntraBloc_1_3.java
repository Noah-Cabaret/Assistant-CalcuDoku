package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.service.aide.visitor.VisiteurChercheurBloc3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TechniqueIntraBloc_1_3 implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        VisiteurChercheurBloc3 chercheur = new VisiteurChercheurBloc3();
        grille.accepter(chercheur);

        for (GroupementCases bloc : chercheur.getBlocsTrouves()) {
            
            // On vérifie d'abord la logique mathématique stricte (1 seule combinaison avec doublon)
            boolean aUnDoublon = verifierDoublonMathematique(bloc);
            
            // Puis on vérifie la topologie en "L" ou "V"
            boolean formeValide = verifierTopologieL(bloc);

            if (aUnDoublon && formeValide) {
                String message = "Ce bloc de 3 cases nécessite obligatoirement un chiffre en double à ses extrémités.";
                String nom = "Technique Intra-Bloc 1-3";
                return new Indice(
                    nom,
                    message, 
                    bloc.getListeCases(), 
                    new HashMap<>()                 
                );
            }
        }
        
        return null; 
    }

    /**
     * Vérifie qu'il n'y a qu'UNE combinaison possible et qu'elle contient un doublon.
     */
    private boolean verifierDoublonMathematique(GroupementCases bloc) {
        List<List<Integer>> combinaisons = bloc.getCombinaisonsMaths();
        
        // 1. Condition stricte : Il ne doit y avoir qu'UNE SEULE combinaison valide
        if (combinaisons == null || combinaisons.size() != 1) {
            return false;
        }

        // On récupère cette unique combinaison (ex: [2, 2, 3])
        List<Integer> combinaisonUnique = combinaisons.get(0);

        // 2. Vérifier si cette combinaison contient un doublon
        Set<Integer> valeursUniques = new HashSet<>();
        for (Integer valeur : combinaisonUnique) {
            // La méthode add() renvoie false si l'élément existe déjà dans le Set
            if (!valeursUniques.add(valeur)) {
                return true; // Doublon trouvé !
            }
        }
        
        return false; // Pas de doublon
    }

   /**
     * Vérifie que le bloc de 3 cases a une forme de "L" ou "V" 
     * (et non une forme de ligne droite).
     */
    private boolean verifierTopologieL(GroupementCases bloc) {
        List<Case> cases = bloc.getListeCases();
        
        // Sécurité supplémentaire
        if (cases.size() != 3) {
            return false;
        }

        Case c1 = cases.get(0);
        Case c2 = cases.get(1);
        Case c3 = cases.get(2);

        // Vérifie si les 3 cases partagent la même coordonnée X (Ligne droite verticale)
        boolean estLigneVerticale = (c1.getX() == c2.getX() && c2.getX() == c3.getX());
        
        // Vérifie si les 3 cases partagent la même coordonnée Y (Ligne droite horizontale)
        boolean estLigneHorizontale = (c1.getY() == c2.getY() && c2.getY() == c3.getY());

        // Si ce n'est ni vertical ni horizontal, alors c'est forcément un angle (L ou V) !
        return !estLigneVerticale && !estLigneHorizontale;
    }
}