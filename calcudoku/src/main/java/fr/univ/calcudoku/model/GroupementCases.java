package fr.univ.calcudoku.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GroupementCases {
    private Operation operation;
    private int resultatCible;
    private Case caseOp;
    private List<Case> listeCases;
    private List<List<Integer>> combinaisonsMaths;

    public GroupementCases(Operation operation, int resultatCible){
        this.operation = operation;
        this.resultatCible = resultatCible;
        this.listeCases = new ArrayList<>();
        this.combinaisonsMaths = new ArrayList<>();
    }
    public void ajouterCase(Case c){
        c.setGroupement(this);
        this.listeCases.add(c);
        mettreAJourCaseOp();     
    }
    private void mettreAJourCaseOp(){
        Comparator<Case> comparateurCoords = Comparator
            .comparingInt(Case::getY)
            .thenComparingInt(Case::getX);

        this.caseOp = listeCases.stream()
            .min(comparateurCoords)
            .orElse(null);    
    }
    public boolean groupementValide() {
        List<Integer> valeurs = new ArrayList<>();
        for (Case c : listeCases) {
            if (c.getValeur() == 0) return false; 
            valeurs.add(c.getValeur());
        }
        return operation.calculer(valeurs) == resultatCible;
    }

    public List<List<Integer>> getCombinaisonsMaths() {
        return combinaisonsMaths;
    }

    public Operation getOperation() { return operation; }
    public int getResultatCible() { return resultatCible; }
    public void setResultatCible(int res) { resultatCible = res ; }
    public List<Case> getListeCases() { return new ArrayList<>(listeCases); }
    public Case getCaseOp() { return caseOp; }

    public void calculerPossibilites(Grille grille) {
        this.combinaisonsMaths.clear();
        List<Case> casesDuGroupe = getListeCases();
        // On passe la grille et sa taille (max)
        trouverCombinaisons(new ArrayList<>(), casesDuGroupe, grille.getTaille(), grille);
    }

    private void trouverCombinaisons(List<Integer> valeursActuelles, List<Case> casesRestantes, int max, Grille grille) {
        if (valeursActuelles.size() == casesRestantes.size()) {
            if (operation.calculer(valeursActuelles) == resultatCible) {
                List<Integer> copie = new ArrayList<>(valeursActuelles);
                copie.sort(Integer::compareTo); 
                if (!combinaisonsMaths.contains(copie)) {
                    combinaisonsMaths.add(copie);
                }
            }
            return;
        }

        int index = valeursActuelles.size();
        Case caseAremplir = casesRestantes.get(index);
        int valeurDejaPosee = caseAremplir.getValeur();

        if (valeurDejaPosee != 0) {
            if (estPossible(valeurDejaPosee, caseAremplir, valeursActuelles, casesRestantes, grille)) {
                valeursActuelles.add(valeurDejaPosee);
                trouverCombinaisons(valeursActuelles, casesRestantes, max, grille);
                valeursActuelles.remove(valeursActuelles.size() - 1);
            }
        } else {
            for (int v = 1; v <= max; v++) {
                if (estPossible(v, caseAremplir, valeursActuelles, casesRestantes, grille)) {
                    valeursActuelles.add(v);
                    trouverCombinaisons(valeursActuelles, casesRestantes, max, grille);
                    valeursActuelles.remove(valeursActuelles.size() - 1);
                }
            }
        }
    }

    private boolean estPossible(int valeur, Case caseCible, List<Integer> valeursPlacees, List<Case> toutesLesCases, Grille grille) {
        int x = caseCible.getX();
        int y = caseCible.getY();

        for (int i = 0; i < grille.getTaille(); i++) {
            // Vérifie la ligne
            Case cLigne = grille.getCase(i, y);
            if (cLigne != caseCible && cLigne.getValeur() == valeur) return false;

            // Vérifie la colonne
            Case cCol = grille.getCase(x, i);
            if (cCol != caseCible && cCol.getValeur() == valeur) return false;
        }

        for (int i = 0; i < valeursPlacees.size(); i++) {
            Case casePrecedente = toutesLesCases.get(i);
            if (valeur == valeursPlacees.get(i)) {
                if (x == casePrecedente.getX() || y == casePrecedente.getY()) {
                    return false;
                }
            }
        }
        return true;
    }
}