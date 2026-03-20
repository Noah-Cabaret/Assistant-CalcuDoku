package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.GroupementCases;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Classe parente abstraite pour toutes les techniques Intra-Bloc.
 * Fournit les outils mathématiques partagés pour repérer les combinaisons et les doublons.
 */
public abstract class TechniqueIntraBloc implements TechniqueAide {

    /**
     * Vérifie si la combinaison mathématique contient un doublon (ex: 2, 2, 4).
     */
    protected boolean aDesChiffresIdentiques(List<Integer> combinaison) {
        if (combinaison == null || combinaison.isEmpty()) return false;
        
        Set<Integer> valeursVues = new HashSet<>();
        for (Integer valeur : combinaison) {
            if (!valeursVues.add(valeur)) {
                return true; // Un doublon a été trouvé
            }
        }
        return false;
    }

    /**
     * Renvoie l'unique combinaison possible du bloc si elle existe, sinon null.
     */
    protected List<Integer> getUniqueCombinaison(GroupementCases bloc) {
        List<List<Integer>> combinaisons = bloc.getCombinaisonsMaths();
        if (combinaisons != null && combinaisons.size() == 1) {
            return combinaisons.get(0);
        }
        return null;
    }
}