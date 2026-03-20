package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TechniqueResteDeGrilleType2 implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        int taille = grille.getTaille();
        Indice indiceNormal = null;

        for (int i = 0; i < taille; i++) {
            Indice indLigne = analyserLigneOuColonne(grille, i, true);
            if (indLigne != null) { if (indLigne.aUneErreur()) return indLigne; if (indiceNormal == null) indiceNormal = indLigne; }
            Indice indCol = analyserLigneOuColonne(grille, i, false);
            if (indCol != null) { if (indCol.aUneErreur()) return indCol; if (indiceNormal == null) indiceNormal = indCol; }
        }
        return indiceNormal;
    }

    private Indice analyserLigneOuColonne(Grille grille, int index, boolean estLigne) {
        int taille = grille.getTaille();
        Set<GroupementCases> blocsTouches = new HashSet<>();
        List<Case> casesDeLaZone = new ArrayList<>();

        int nbCasesVides = 0;
        for (int i = 0; i < taille; i++) {
            Case c = grille.getCase(estLigne ? i : index, estLigne ? index : i);
            casesDeLaZone.add(c);
            if (c.getValeur() == 0) nbCasesVides++;
            if (c.getGroupement() != null) blocsTouches.add(c.getGroupement());
        }

        List<GroupementCases> blocsPartiels = new ArrayList<>();
        for (GroupementCases bloc : blocsTouches) {
            if (!casesDeLaZone.containsAll(bloc.getListeCases())) blocsPartiels.add(bloc);
        }

        // LOGIQUE MATHÉMATIQUE : On identifie la forme de la zone (1 case externe)
        if (blocsPartiels.size() == 1) {
            GroupementCases blocCible = blocsPartiels.get(0);
            int compteurExterne = 0;
            Case caseExterne = null;
            
            for (Case c : blocCible.getListeCases()) {
                if (!casesDeLaZone.contains(c)) { compteurExterne++; caseExterne = c; }
            }

            if (compteurExterne == 1 && blocCible.getListeCases().size() > 1) {
                // LOGIQUE MATHÉMATIQUE : Calcul d'algèbre
                int sommeTheoriqueZone = taille * (taille + 1) / 2;
                int sommeTousBlocs = 0;
                
                for (GroupementCases b : blocsTouches) {
                    int sommeBloc = 0;
                    if (b.getCombinaisonsMaths() != null && !b.getCombinaisonsMaths().isEmpty()) {
                        for (int val : b.getCombinaisonsMaths().get(0)) sommeBloc += val;
                    }
                    sommeTousBlocs += sommeBloc;
                }
                
                int reponseExacte = sommeTousBlocs - sommeTheoriqueZone;
                int valeurJoueur = caseExterne.getValeur();
                
                if (valeurJoueur == reponseExacte) return null;
                
                // VÉRIFICATION ERREUR 
                boolean contientErreur = (valeurJoueur != 0 && valeurJoueur != caseExterne.getSolution());
                if (!contientErreur && nbCasesVides <= 1) return null;

                List<Case> surbrillance = new ArrayList<>();
                Map<Case, Integer> solutions = new HashMap<>();
                solutions.put(caseExterne, reponseExacte);

                if (contientErreur) {
                    surbrillance.add(caseExterne); 
                    return new Indice("Exclusion Unique", "Erreur détectée !\nEn soustrayant la somme des blocs à la somme théorique de la zone, la case externe doit valoir " + reponseExacte + ".", surbrillance, solutions, true);
                } else {
                    surbrillance.addAll(casesDeLaZone);
                    for(Case c : blocCible.getListeCases()) if(!surbrillance.contains(c)) surbrillance.add(c);
                    return new Indice("Exclusion Unique", "Ce bloc est entièrement dans cette zone, à l'exception d'une seule case \"orpheline\".\nPar déduction, cette case externe vaut " + reponseExacte + " !", surbrillance, solutions, false);
                }
            }
        }
        return null;
    }
}