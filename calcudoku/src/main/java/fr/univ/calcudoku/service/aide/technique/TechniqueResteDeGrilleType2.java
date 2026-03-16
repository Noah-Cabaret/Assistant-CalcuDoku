package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.service.aide.visitor.VisiteurGrille;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Technique 5 : Reste de Grille - Exclusion Unique (Type 2 / Outie)
 * Cherche un bloc presque entièrement contenu dans une ligne/colonne, 
 * sauf pour UNE SEULE case qui déborde à l'extérieur.
 */
public class TechniqueResteDeGrilleType2 implements TechniqueAide, VisiteurGrille {

    private Grille grilleActuelle;
    private Indice indiceTrouve;

    @Override
    public Indice analyser(Grille grille) {
        this.grilleActuelle = grille;
        this.indiceTrouve = null;
        grille.accepter(this);
        return indiceTrouve;
    }

    @Override
    public void visiter(Grille g) {
        if (indiceTrouve != null) return;
        int taille = g.getTaille();

        for (int i = 0; i < taille; i++) {
            analyserLigneOuColonne(i, true);
            if (indiceTrouve != null) return;
            analyserLigneOuColonne(i, false);
            if (indiceTrouve != null) return;
        }
    }

    @Override
    public void visiter(GroupementCases groupement) {}
    @Override
    public void visiter(Case c) {}

    private void analyserLigneOuColonne(int index, boolean estLigne) {
        int taille = grilleActuelle.getTaille();
        Set<GroupementCases> blocsTouches = new HashSet<>();
        List<Case> casesDeLaZone = new ArrayList<>();

        // 1. Lister toutes les cases de la zone
        for (int i = 0; i < taille; i++) {
            int x = estLigne ? i : index;
            int y = estLigne ? index : i;
            Case c = grilleActuelle.getCase(x, y);
            casesDeLaZone.add(c);
            if (c.getGroupement() != null) {
                blocsTouches.add(c.getGroupement());
            }
        }

        // 2. Trouver les blocs partiels
        List<GroupementCases> blocsPartiels = new ArrayList<>();
        for (GroupementCases bloc : blocsTouches) {
            boolean estEntierementDedans = casesDeLaZone.containsAll(bloc.getListeCases());
            if (!estEntierementDedans) {
                blocsPartiels.add(bloc);
            }
        }

        // 3. Condition : Un seul bloc partiel sur toute la ligne
        if (blocsPartiels.size() == 1) {
            GroupementCases blocCible = blocsPartiels.get(0);

            // 4. Calcul du "CompteurExterne" (Cases du bloc situées EN DEHORS de la ligne)
            int compteurExterne = 0;
            Case caseExterne = null;
            for (Case c : blocCible.getListeCases()) {
                if (!casesDeLaZone.contains(c)) {
                    compteurExterne++;
                    caseExterne = c;
                }
            }

            // 5. Validation finale (Le bloc est tout entier dans la ligne, SAUF 1 case)
            if (compteurExterne == 1 && blocCible.getListeCases().size() > 1) {
                
                // Si la case externe est déjà trouvée par le joueur, on passe
                if (caseExterne.getValeur() != 0) return;

                String nomZone = estLigne ? "la ligne " + (index + 1) : "la colonne " + (index + 1);
                String message = "Technique Reste de Grille (Exclusion) sur " + nomZone + ".\n"
                        + "Ce bloc mathématique est presque entièrement contenu dans cette zone, à l'exception d'une seule case \"orpheline\" qui en sort.\n"
                        + "En comparant la somme de la zone avec la somme du bloc, vous pouvez déduire la valeur de cette case externe !";

                // Surbrillance : Ligne entière + le bloc qui déborde
                List<Case> surbrillance = new ArrayList<>(casesDeLaZone);
                for(Case c : blocCible.getListeCases()) {
                    if(!surbrillance.contains(c)) surbrillance.add(c);
                }

                Map<Case, Integer> solutionsVides = new HashMap<>();

                this.indiceTrouve = new Indice("Exclusion Unique", message, surbrillance, solutionsVides, false);
            }
        }
    }
}