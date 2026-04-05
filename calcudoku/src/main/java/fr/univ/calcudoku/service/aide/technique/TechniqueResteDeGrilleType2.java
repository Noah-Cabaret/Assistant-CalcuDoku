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
 * Technique d'aide : Reste de Grille Type 2 (Outie / Extérieur).
 * Identifie un bloc qui déborde d'une ligne ou colonne complète par une seule case.
 * Soustrait la valeur théorique de la zone au total des blocs touchés.
 */
public class TechniqueResteDeGrilleType2 implements TechniqueAide {

    /**
     * Analyse chaque ligne et colonne pour y chercher un débordement logique d'une case.
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
            int compteurExterne = 0;
            Case caseExterne = null;
            
            for (Case c : blocCible.getListeCases()) {
                if (!casesDeLaZone.contains(c)) { compteurExterne++; caseExterne = c; }
            }

            if (compteurExterne == 1 && blocCible.getListeCases().size() > 1) {
                
                int reponseExacte = -1;
                
                if (!contientFois) {
                    boolean calculSommeValide = true;
                    int sommeTousBlocs = 0;
                    for (GroupementCases b : blocsTouches) {
                        int s = getSommeBlocConstante(b);
                        if (s == -1) { calculSommeValide = false; break; }
                        sommeTousBlocs += s;
                    }
                    if (calculSommeValide) {
                        int sommeTheoriqueZone = taille * (taille + 1) / 2;
                        reponseExacte = sommeTousBlocs - sommeTheoriqueZone;
                    }
                } 
                else {
                    boolean calculProdValide = true;
                    int prodTousBlocs = 1;
                    for (GroupementCases b : blocsTouches) {
                        int p = getProduitBlocConstant(b);
                        if (p == -1) { calculProdValide = false; break; }
                        prodTousBlocs *= p;
                    }
                    if (calculProdValide) {
                        int prodTheorique = 1;
                        for(int i=1; i<=taille; i++) prodTheorique *= i;
                        if (prodTheorique != 0 && prodTousBlocs % prodTheorique == 0) {
                            reponseExacte = prodTousBlocs / prodTheorique;
                        }
                    }
                }

                if (reponseExacte < 1 || reponseExacte > taille) return null;

                int valeurJoueur = caseExterne.getValeur();
                if (valeurJoueur == reponseExacte) return null;
                
                boolean contientErreur = (valeurJoueur != 0 && valeurJoueur != caseExterne.getSolution());
                if (!contientErreur && nbCasesVides <= 1) return null;

                List<Case> surbrillance = new ArrayList<>();
                Map<Case, Integer> solutions = new HashMap<>();
                List<String> messages = new ArrayList<>();

                if (contientErreur) {
                    surbrillance.add(caseExterne); 
                    messages.add("Une anomalie globale a été détectée en utilisant la technique du 'Reste de Grille'.");
                    messages.add("La valeur d'une case orpheline, déduite par soustraction globale des blocs avec la ligne entière, ne correspond pas à votre saisie.");
                    messages.add("Erreur détectée ! En soustrayant la zone aux blocs, la case en surbrillance doit valoir " + reponseExacte + ".");
                    return new Indice("Reste de Grille (Extérieur)", messages, surbrillance, solutions, true);
                } else {
                    surbrillance.addAll(casesDeLaZone);
                    for(Case c : blocCible.getListeCases()) if(!surbrillance.contains(c)) surbrillance.add(c);
                    
                    messages.add("Regardez les lignes et les colonnes globalement. Parfois, un bloc 'déborde' d'une seule case d'une zone complète.");
                    messages.add("Additionnez les blocs qui touchent cette zone et soustrayez le total théorique connu de la zone pour isoler la case qui déborde.");
                    messages.add("Reste de grille (Extérieur) : Regardez la zone en surbrillance. Soustrayez la valeur théorique de la zone au total des blocs impliqués pour trouver la case orpheline !");
                    
                    return new Indice("Reste de Grille (Extérieur)", messages, surbrillance, solutions, false);
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