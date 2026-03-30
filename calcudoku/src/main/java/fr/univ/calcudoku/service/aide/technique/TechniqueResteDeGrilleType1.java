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
import java.util.Random;
import java.util.Set;

/**
 * Technique d'aide : Reste de Grille Type 1 (Innie / Intérieur).
 * Compare la somme (ou le produit) théorique d'une ligne/colonne complète
 * avec la somme des blocs qui y sont parfaitement inclus pour déduire la case manquante.
 */
public class TechniqueResteDeGrilleType1 implements TechniqueAide {

    /**
     * Analyse chaque ligne et colonne pour appliquer la méthode globale du Reste de Grille.
     * @param grille La grille à analyser.
     * @return Un Indice avec des messages progressifs.
     */
    @Override
    public Indice analyser(Grille grille) {
        int taille = grille.getTaille();
        
        List<Indice> indicesErreurs = new ArrayList<>();
        List<Indice> indicesNormaux = new ArrayList<>();

        for (int i = 0; i < taille; i++) {
            Indice indLigne = analyserLigneOuColonne(grille, i, true);
            if (indLigne != null) { 
                if (indLigne.aUneErreur()) indicesErreurs.add(indLigne); 
                else indicesNormaux.add(indLigne); 
            }
            
            Indice indCol = analyserLigneOuColonne(grille, i, false);
            if (indCol != null) { 
                if (indCol.aUneErreur()) indicesErreurs.add(indCol); 
                else indicesNormaux.add(indCol); 
            }
        }
        
        Random random = new Random();
        if (!indicesErreurs.isEmpty()) return indicesErreurs.get(random.nextInt(indicesErreurs.size()));
        else if (!indicesNormaux.isEmpty()) return indicesNormaux.get(random.nextInt(indicesNormaux.size()));

        return null;
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

        boolean contientPlus = false;
        boolean contientFois = false;
        boolean operateurInvalide = false;

        for (GroupementCases b : blocsTouches) {
            if (b.getListeCases().size() > 1 && b.getOperation() != null) {
                String sym = b.getOperation().getSymbole();
                if (sym.equals("+")) contientPlus = true;
                else if (sym.equals("x") || sym.equals("*")) contientFois = true;
                else { operateurInvalide = true; break; }
            }
        }

        if (operateurInvalide || (contientPlus && contientFois)) return null;

        List<GroupementCases> blocsPartiels = new ArrayList<>();
        for (GroupementCases bloc : blocsTouches) {
            if (!casesDeLaZone.containsAll(bloc.getListeCases())) blocsPartiels.add(bloc);
        }

        if (blocsPartiels.size() == 1) {
            GroupementCases blocCible = blocsPartiels.get(0);
            int compteurInterne = 0;
            Case caseInterne = null;
            
            for (Case c : blocCible.getListeCases()) {
                if (casesDeLaZone.contains(c)) { compteurInterne++; caseInterne = c; }
            }

            if (compteurInterne == 1 && blocCible.getListeCases().size() > 1) {
                
                int reponseExacte = -1;
                
                if (!contientFois) {
                    boolean calculSommeValide = true;
                    int sommeBlocsInternes = 0;
                    for (GroupementCases b : blocsTouches) {
                        if (b != blocCible) {
                            int s = getSommeBlocConstante(b);
                            if (s == -1) { calculSommeValide = false; break; }
                            sommeBlocsInternes += s;
                        }
                    }
                    if (calculSommeValide) {
                        int sommeTheoriqueZone = taille * (taille + 1) / 2;
                        reponseExacte = sommeTheoriqueZone - sommeBlocsInternes;
                    }
                } 
                else {
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
                List<String> messages = new ArrayList<>();

                if (contientErreur) {
                    surbrillance.add(caseInterne); 
                    messages.add("Une erreur a été trouvée en analysant une ligne ou une colonne dans sa globalité.");
                    messages.add("En comparant le total mathématique théorique de la zone avec le total des blocs qu'elle contient, le compte n'y est pas.");
                    messages.add("Erreur détectée ! Par déduction mathématique (Reste de Grille), la case en surbrillance devrait valoir " + reponseExacte + ".");
                    return new Indice("Reste de Grille (Intérieur)", messages, surbrillance, solutions, true);
                } else {
                    surbrillance.addAll(casesDeLaZone);
                    for(Case c : blocCible.getListeCases()) if(!surbrillance.contains(c)) surbrillance.add(c);
                    
                    String typeCalcul = contientFois ? "le produit global" : "la somme globale";
                    
                    messages.add("Avez-vous pensé à faire " + typeCalcul + " d'une ligne ou d'une colonne entière ?");
                    messages.add("Le résultat d'une ligne complète est toujours connu d'avance. Comparez ce total avec les blocs qui sont parfaitement à l'intérieur.");
                    messages.add("Reste de grille (Intérieur) : Tous les blocs sont inclus dans la zone en surbrillance, sauf un. Calculez la différence pour trouver la case ciblée !");
                    
                    return new Indice("Reste de Grille (Intérieur)", messages, surbrillance, solutions, false);
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
        if (b.getOperation() != null && (b.getOperation().getSymbole().equals("x") || b.getOperation().getSymbole().equals("*"))) return b.getResultatCible();
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