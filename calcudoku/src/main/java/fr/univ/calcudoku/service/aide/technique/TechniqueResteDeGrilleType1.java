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
 * Technique 4 : Reste de Grille - Intersection Simple (Type 1 / Innie)
 * Cherche une ligne ou colonne où un seul bloc "déborde", 
 * et ne touche la ligne que par UNE SEULE case.
 */
public class TechniqueResteDeGrilleType1 implements TechniqueAide, VisiteurGrille {

    private Grille grilleActuelle;
    private Indice indiceTrouve;

    @Override
    public Indice analyser(Grille grille) {
        this.grilleActuelle = grille;
        this.indiceTrouve = null;
        grille.accepter(this); // Lance le visiteur
        return indiceTrouve;
    }

    @Override
    public void visiter(Grille g) {
        if (indiceTrouve != null) return;
        int taille = g.getTaille();

        // Analyse de chaque ligne et de chaque colonne
        for (int i = 0; i < taille; i++) {
            analyserLigneOuColonne(i, true);  // Analyse Ligne
            if (indiceTrouve != null) return;
            analyserLigneOuColonne(i, false); // Analyse Colonne
            if (indiceTrouve != null) return;
        }
    }

    @Override
    public void visiter(GroupementCases groupement) {} // Non utilisé ici
    @Override
    public void visiter(Case c) {} // Non utilisé ici

    private void analyserLigneOuColonne(int index, boolean estLigne) {
        int taille = grilleActuelle.getTaille();
        Set<GroupementCases> blocsTouches = new HashSet<>();
        List<Case> casesDeLaZone = new ArrayList<>();

        // 1. Lister toutes les cases de la ligne/colonne et les blocs impliqués
        for (int i = 0; i < taille; i++) {
            int x = estLigne ? i : index;
            int y = estLigne ? index : i;
            Case c = grilleActuelle.getCase(x, y);
            casesDeLaZone.add(c);
            if (c.getGroupement() != null) {
                blocsTouches.add(c.getGroupement());
            }
        }

        // 2. Trouver les blocs qui ne sont PAS entièrement contenus dans la ligne
        List<GroupementCases> blocsPartiels = new ArrayList<>();
        for (GroupementCases bloc : blocsTouches) {
            boolean estEntierementDedans = casesDeLaZone.containsAll(bloc.getListeCases());
            if (!estEntierementDedans) {
                blocsPartiels.add(bloc);
            }
        }

        // 3. Condition Principale : Il ne doit y avoir qu'UN SEUL bloc partiel
        if (blocsPartiels.size() == 1) {
            GroupementCases blocCible = blocsPartiels.get(0);

            // 4. Calcul du "CompteurInterne" (Cases du bloc situées DANS la ligne)
            int compteurInterne = 0;
            Case caseInterne = null;
            for (Case c : blocCible.getListeCases()) {
                if (casesDeLaZone.contains(c)) {
                    compteurInterne++;
                    caseInterne = c;
                }
            }

            // 5. Validation finale de la technique
            if (compteurInterne == 1 && blocCible.getListeCases().size() > 1) {
                
                // Si la case est déjà résolue, on passe
                if (caseInterne.getValeur() != 0) return;

                String nomZone = estLigne ? "la ligne " + (index + 1) : "la colonne " + (index + 1);
                String message = "Technique Reste de Grille (Intersection) sur " + nomZone + ".\n"
                        + "Tous les blocs sont parfaitement contenus dans cette zone, sauf un seul qui n'y possède qu'une case (la \"porte d'entrée\").\n"
                        + "Par déduction mathématique (la somme théorique de la zone par rapport à la somme des blocs), vous pouvez trouver la valeur exacte de cette case !";

                // Surbrillance : La ligne entière + le bloc qui déborde
                List<Case> surbrillance = new ArrayList<>(casesDeLaZone);
                for(Case c : blocCible.getListeCases()) {
                    if(!surbrillance.contains(c)) surbrillance.add(c);
                }

                // On ne donne pas la solution explicite pour faire réfléchir le joueur (Niveau 1)
                Map<Case, Integer> solutionsVides = new HashMap<>();

                this.indiceTrouve = new Indice("Intersection Simple", message, surbrillance, solutionsVides, false);
            }
        }
    }
}