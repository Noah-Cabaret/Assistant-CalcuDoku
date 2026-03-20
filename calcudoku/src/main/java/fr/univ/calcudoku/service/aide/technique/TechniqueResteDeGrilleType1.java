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

public class TechniqueResteDeGrilleType1 implements TechniqueAide, VisiteurGrille {

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

        for (int i = 0; i < taille; i++) {
            int x = estLigne ? i : index;
            int y = estLigne ? index : i;
            Case c = grilleActuelle.getCase(x, y);
            casesDeLaZone.add(c);
            if (c.getGroupement() != null) {
                blocsTouches.add(c.getGroupement());
            }
        }

        List<GroupementCases> blocsPartiels = new ArrayList<>();
        for (GroupementCases bloc : blocsTouches) {
            boolean estEntierementDedans = casesDeLaZone.containsAll(bloc.getListeCases());
            if (!estEntierementDedans) {
                blocsPartiels.add(bloc);
            }
        }

        if (blocsPartiels.size() == 1) {
            GroupementCases blocCible = blocsPartiels.get(0);

            int compteurInterne = 0;
            Case caseInterne = null;
            for (Case c : blocCible.getListeCases()) {
                if (casesDeLaZone.contains(c)) {
                    compteurInterne++;
                    caseInterne = c;
                }
            }

            if (compteurInterne == 1 && blocCible.getListeCases().size() > 1) {
                
                // MODIFICATION : Calcul explicite de la solution via la somme théorique
                int sommeTheoriqueZone = taille * (taille + 1) / 2;
                int sommeBlocsInternes = 0;
                
                for (GroupementCases b : blocsTouches) {
                    if (b != blocCible) {
                        int sommeBloc = 0;
                        if (b.getCombinaisonsMaths() != null && !b.getCombinaisonsMaths().isEmpty()) {
                            // On déduit la somme du bloc avec sa combinaison
                            for (int val : b.getCombinaisonsMaths().get(0)) {
                                sommeBloc += val;
                            }
                        }
                        sommeBlocsInternes += sommeBloc;
                    }
                }
                
                int reponseExacte = sommeTheoriqueZone - sommeBlocsInternes;
                int valeurJoueur = caseInterne.getValeur();
                
                if (valeurJoueur == reponseExacte) return;
                
                // MODIFICATION : Détection d'erreur et message dynamique
                boolean contientErreur = (valeurJoueur != 0);
                String nomZone = estLigne ? "la ligne " + (index + 1) : "la colonne " + (index + 1);
                String message;
                
                if (contientErreur) {
                    message = "Erreur détectée sur " + nomZone + " !\n"
                            + "Par déduction mathématique (la somme théorique de la zone par rapport aux blocs complets), "
                            + "cette case devrait valoir " + reponseExacte + ".";
                } else {
                    message = "Technique Reste de Grille (Intersection) sur " + nomZone + ".\n"
                            + "Tous les blocs sont parfaitement contenus dans cette zone, sauf un seul qui n'y possède qu'une case.\n"
                            + "Par déduction mathématique, cette case vaut exactement " + reponseExacte + " !";
                }

                List<Case> surbrillance = new ArrayList<>(casesDeLaZone);
                for(Case c : blocCible.getListeCases()) {
                    if(!surbrillance.contains(c)) surbrillance.add(c);
                }

                // MODIFICATION : Injection de la vraie solution au lieu d'une Map vide
                Map<Case, Integer> solutions = new HashMap<>();
                solutions.put(caseInterne, reponseExacte);

                this.indiceTrouve = new Indice("Intersection Simple", message, surbrillance, solutions, contientErreur);
            }
        }
    }
}