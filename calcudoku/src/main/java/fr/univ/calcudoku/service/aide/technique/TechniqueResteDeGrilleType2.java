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
 * Technique : Reste de Grille 2 (Exclusion).
 * Identifie un bloc qui déborde de la zone ciblée par une seule case.
 * Utilise l'algèbre pour trouver cette case "orpheline".
 */
public class TechniqueResteDeGrilleType2 implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        int taille = grille.getTaille();
        
        // Listes pour stocker TOUS les indices trouvés
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
        
        // Sélection aléatoire d'un seul indice
        Random random = new Random();
        if (!indicesErreurs.isEmpty()) {
            return indicesErreurs.get(random.nextInt(indicesErreurs.size()));
        } else if (!indicesNormaux.isEmpty()) {
            return indicesNormaux.get(random.nextInt(indicesNormaux.size()));
        }

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

        // Vérification exclusive des opérateurs (+ uniquement OU x uniquement)
        boolean contientPlus = false;
        boolean contientFois = false;
        boolean operateurInvalide = false;

        for (GroupementCases b : blocsTouches) {
            if (b.getListeCases().size() > 1 && b.getOperation() != null) {
                String sym = b.getOperation().getSymbole();
                if (sym.equals("+")) {
                    contientPlus = true;
                } else if (sym.equals("x") || sym.equals("*")) {
                    contientFois = true;
                } else {
                    operateurInvalide = true;
                    break;
                }
            }
        }

        if (operateurInvalide || (contientPlus && contientFois)) {
            return null; // On abandonne cette zone car elle mélange ou a des signes interdits
        }

        List<GroupementCases> blocsPartiels = new ArrayList<>();
        for (GroupementCases bloc : blocsTouches) {
            if (!casesDeLaZone.containsAll(bloc.getListeCases())) blocsPartiels.add(bloc);
        }

        // Forme "Outie" : Un seul bloc partiel possède la case orpheline
        if (blocsPartiels.size() == 1) {
            GroupementCases blocCible = blocsPartiels.get(0);
            int compteurExterne = 0;
            Case caseExterne = null;
            
            for (Case c : blocCible.getListeCases()) {
                if (!casesDeLaZone.contains(c)) { compteurExterne++; caseExterne = c; }
            }

            if (compteurExterne == 1 && blocCible.getListeCases().size() > 1) {
                
                int reponseExacte = -1;
                
                // Si tout est en Addition
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
                // Si tout est en Multiplication
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

                if (contientErreur) {
                    surbrillance.add(caseExterne); 
                    return new Indice("Reste de Grille (Extérieur)", "Erreur détectée !\nEn soustrayant la zone aux blocs, la case externe orpheline doit valoir " + reponseExacte + ".", surbrillance, solutions, true);
                } else {
                    surbrillance.addAll(casesDeLaZone);
                    for(Case c : blocCible.getListeCases()) if(!surbrillance.contains(c)) surbrillance.add(c);
                    
                    // NOUVEAU MESSAGE ADAPTÉ
                    String typeCalcul = contientFois ? "le produit global" : "la somme globale";
                    String operation = contientFois ? "multiplications" : "additions";
                    String msg = "Reste de grille (Extérieur) : Ce bloc est entièrement dans la zone surlignée, à l'exception d'une seule case \"orpheline\".\n"
                               + "Puisque les blocs ne contiennent que des " + operation + ", calculez " + typeCalcul + " des blocs et soustrayez-y la valeur théorique de la zone pour trouver la valeur de cette case externe !";
                    
                    return new Indice("Reste de Grille (Extérieur)", msg, surbrillance, solutions, false);
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