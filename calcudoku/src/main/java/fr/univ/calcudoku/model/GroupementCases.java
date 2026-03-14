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

    public void calculerPossibilites(int tailleGrille) {
        this.combinaisonsMaths.clear();
        // On récupère la liste des cases pour connaître leurs coordonnées (x, y)
        List<Case> casesDuGroupe = getListeCases();
        trouverCombinaisons(new ArrayList<>(), casesDuGroupe, tailleGrille);
    }

    private void trouverCombinaisons(List<Integer> valeursActuelles, List<Case> casesRestantes, int max) {
        if (valeursActuelles.size() == listeCases.size()) {
            if (operation.calculer(valeursActuelles) == resultatCible) {
                List<Integer> copie = new ArrayList<>(valeursActuelles);
                copie.sort(Integer::compareTo); 
                if (!combinaisonsMaths.contains(copie)) {
                    combinaisonsMaths.add(copie);
                }
            }
            return;
        }

        int indexCaseActuelle = valeursActuelles.size();
        Case caseAremplir = casesRestantes.get(indexCaseActuelle);

        for (int v = 1; v <= max; v++) {
            if (estPossible(v, caseAremplir, valeursActuelles, casesRestantes)) {
                valeursActuelles.add(v);
                trouverCombinaisons(valeursActuelles, casesRestantes, max);
                valeursActuelles.remove(valeursActuelles.size() - 1); // Backtracking
            }
        }
    }

    private boolean estPossible(int valeur, Case caseCible, List<Integer> valeursPlacees, List<Case> toutesLesCases) {
        for (int i = 0; i < valeursPlacees.size(); i++) {
            Case casePrecedente = toutesLesCases.get(i);
            int valeurPrecedente = valeursPlacees.get(i);

            // Si c'est la même valeur, elle ne doit pas être sur la même ligne ou colonne
            if (valeur == valeurPrecedente) {
                if (caseCible.getX() == casePrecedente.getX() || caseCible.getY() == casePrecedente.getY()) {
                    return false;
                }
            }
        }
        return true;
    }
}