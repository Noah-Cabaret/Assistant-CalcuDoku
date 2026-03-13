package fr.univ.calcudoku.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import fr.univ.calcudoku.service.aide.visitor.VisiteurGrille;


public class GroupementCases  implements ElementVisitable {
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

    @Override
    public void accepter(VisiteurGrille visiteur) {
        visiteur.visiter(this);
    }
}