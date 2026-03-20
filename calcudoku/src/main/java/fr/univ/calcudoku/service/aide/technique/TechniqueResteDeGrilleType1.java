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

/**
 * Technique : Reste de Grille 1 (Intersection).
 * Utilise la somme/produit théorique d'une zone face aux blocs complets.
 * Identifie un bloc qui n'a qu'une seule case à l'intérieur de la zone.
 */
public class TechniqueResteDeGrilleType1 implements TechniqueAide {

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

        // Si la forme pédagogique "Innie" est détectée
        if (blocsPartiels.size() == 1) {
            GroupementCases blocCible = blocsPartiels.get(0);
            int compteurInterne = 0;
            Case caseInterne = null;
            
            for (Case c : blocCible.getListeCases()) {
                if (casesDeLaZone.contains(c)) { compteurInterne++; caseInterne = c; }
            }

            // On vérifie qu'on peut faire le calcul purement mathématique (somme ou produit constant)
            if (compteurInterne == 1 && blocCible.getListeCases().size() > 1) {
                
                boolean calculSommeValide = true;
                int sommeBlocsInternes = 0;
                for (GroupementCases b : blocsTouches) {
                    if (b != blocCible) {
                        int s = getSommeBlocConstante(b);
                        if (s == -1) { calculSommeValide = false; break; }
                        sommeBlocsInternes += s;
                    }
                }

                int reponseExacte = -1;
                
                if (calculSommeValide) {
                    int sommeTheoriqueZone = taille * (taille + 1) / 2;
                    reponseExacte = sommeTheoriqueZone - sommeBlocsInternes;
                } else {
                    boolean calculProdValide = true;
                    int prodBlocsInternes = 1;
                    for (GroupementCases b : blocsTouches) {
                        if (b != blocCible) {
                            int p = getProduitBlocConstant(b);
                            if (p == -1) { calculProdValide = false; break; }
                            prodBlocsInternes *= p;
                        }
                    }
                    if (calculProdValide) {
                        int prodTheorique = 1;
                        for(int i=1; i<=taille; i++) prodTheorique *= i;
                        if (prodBlocsInternes != 0 && prodTheorique % prodBlocsInternes == 0) {
                            reponseExacte = prodTheorique / prodBlocsInternes;
                        }
                    }
                }

                if (reponseExacte < 1 || reponseExacte > taille) return null;

                int valeurJoueur = caseInterne.getValeur();
                if (valeurJoueur == reponseExacte) return null;
                
                boolean contientErreur = (valeurJoueur != 0 && valeurJoueur != caseInterne.getSolution());
                if (!contientErreur && nbCasesVides <= 1) return null;

                List<Case> surbrillance = new ArrayList<>();
                Map<Case, Integer> solutions = new HashMap<>();
                solutions.put(caseInterne, reponseExacte);

                if (contientErreur) {
                    surbrillance.add(caseInterne); 
                    return new Indice("Reste de Grille (Intérieur)", "Erreur détectée !\nPar déduction mathématique globale de la zone, cette case devrait valoir " + reponseExacte + ".", surbrillance, solutions, true);
                } else {
                    surbrillance.addAll(casesDeLaZone);
                    for(Case c : blocCible.getListeCases()) if(!surbrillance.contains(c)) surbrillance.add(c);
                    String msg = "Techniques de reste de grille : Tous les blocs sont parfaitement contenus dans cette zone, sauf un seul qui n'y possède qu'une case.\nLa somme (ou le produit) d'une ligne est toujours le même. Utilisez la différence pour trouver la case manquante !";
                    return new Indice("Reste de Grille (Intérieur)", msg, surbrillance, solutions, false);
                }
            }
        }
        return null;
    }

    private int getSommeBlocConstante(GroupementCases b) {
        if (b.getListeCases().size() == 1) return b.getResultatCible();
        if (b.getOperation() != null && b.getOperation().getSymbole().equals("+")) return b.getResultatCible();
        List<List<Integer>> combos = b.getCombinaisonsMaths();
        if (combos == null || combos.isEmpty()) return -1;
        int sum = -1;
        for (List<Integer> combo : combos) {
            int s = 0; for (int val : combo) s += val;
            if (sum == -1) sum = s; else if (sum != s) return -1;
        }
        return sum;
    }

    private int getProduitBlocConstant(GroupementCases b) {
        if (b.getListeCases().size() == 1) return b.getResultatCible();
        if (b.getOperation() != null && b.getOperation().getSymbole().equals("x")) return b.getResultatCible();
        List<List<Integer>> combos = b.getCombinaisonsMaths();
        if (combos == null || combos.isEmpty()) return -1;
        int prod = -1;
        for (List<Integer> combo : combos) {
            int p = 1; for (int val : combo) p *= val;
            if (prod == -1) prod = p; else if (prod != p) return -1;
        }
        return prod;
    }
}