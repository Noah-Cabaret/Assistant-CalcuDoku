package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TechniqueIntraBloc_3_5 extends TechniqueIntraBloc {

    @Override
    public Indice analyser(Grille grille) {
        List<Indice> indicesErreurs = new ArrayList<>();
        List<Indice> indicesNormaux = new ArrayList<>();

        for (GroupementCases bloc : grille.getListeGroupements()) {
            
            if (bloc.getListeCases().size() >= 4 && verifierTopologieGrandL(bloc)) {
                
                List<List<Integer>> combosPossibles = bloc.getCombinaisonsMaths();
                List<List<Integer>> combosValides = getCombinaisonsValides(grille, bloc);
                
                // Si la grille a réduit les possibilités à UNE SEULE et qu'elle a un doublon
                if (combosValides.size() == 1) {
                    List<Integer> combinaisonUnique = combosValides.get(0);
                    
                    if (aDesChiffresIdentiques(combinaisonUnique)) {
                        
                        int nbCasesVides = 0;
                        boolean contientErreur = false;
                        List<Case> casesFausses = new ArrayList<>();

                        for (Case c : bloc.getListeCases()) {
                            if (c.getValeur() == 0) nbCasesVides++;
                            else if (c.getValeur() != c.getSolution()) { contientErreur = true; casesFausses.add(c); }
                        }

                        if (!contientErreur && nbCasesVides <= 1) continue;

                        Map<Case, Integer> solutions = new HashMap<>();

                        if (contientErreur) {
                            String msg = "Erreur détectée ! Ce bloc allongé force l'utilisation d'un doublon en fonction de la grille actuelle.\nCertains chiffres sont mal placés pour éviter les conflits.";
                            indicesErreurs.add(new Indice("Technique Intra-bloc (Grand L)", msg, casesFausses, solutions, true));
                        } else {
                            String comboStr = combinaisonUnique.toString().replace("[", "").replace("]", "");
                            String symbole = bloc.getOperation() != null ? bloc.getOperation().getSymbole() : "";
                            int cible = bloc.getResultatCible();
                            String msg;

                            if (combosPossibles.size() == 1) {
                                msg = "Technique intra-bloc : Observez ce grand bloc. Pour faire " + cible + " avec (" + symbole + "), sa seule combinaison est (" + comboStr + ") qui contient des doublons !\nVous devez ruser pour placer ces doublons sans violer les règles sur sa ligne principale.";
                            } else {
                                msg = "Déduction intra-bloc : Grâce aux autres chiffres de la grille, il ne reste plus qu'une seule combinaison valable pour ce grand bloc : (" + comboStr + ") !\nElle nécessite des doublons que vous devez placer astucieusement pour éviter les conflits.";
                            }
                            
                            indicesNormaux.add(new Indice("Technique Intra-bloc (Grand L)", msg, bloc.getListeCases(), solutions, false));
                        }
                    }
                }
            }
        }

        Random rand = new Random();
        if (!indicesErreurs.isEmpty()) return indicesErreurs.get(rand.nextInt(indicesErreurs.size()));
        if (!indicesNormaux.isEmpty()) return indicesNormaux.get(rand.nextInt(indicesNormaux.size()));

        return null; 
    }

    private boolean verifierTopologieGrandL(GroupementCases bloc) {
        List<Case> cases = bloc.getListeCases();
        int taille = cases.size();
        
        for (int i = 0; i < taille; i++) {
            Case candidateSortante = cases.get(i);
            Integer commonX = null;
            Integer commonY = null;
            
            for (int j = 0; j < taille; j++) {
                if (i == j) continue; 
                if (commonX == null) commonX = cases.get(j).getX();
                else if (commonX != cases.get(j).getX()) commonX = -1; 
                
                if (commonY == null) commonY = cases.get(j).getY();
                else if (commonY != cases.get(j).getY()) commonY = -1; 
            }
            
            if (commonX != null && commonX != -1 && candidateSortante.getX() != commonX) return true; 
            if (commonY != null && commonY != -1 && candidateSortante.getY() != commonY) return true; 
        }
        return false; 
    }
}