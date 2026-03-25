package fr.univ.calcudoku.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import fr.univ.calcudoku.service.aide.visitor.VisiteurGrille;

/**
 * Représente un groupement de cases (cage) dans la grille Calcudoku.
 * Chaque cage a une opération et un résultat cible.
 */
public class GroupementCases  implements ElementVisitable {
    /** L'opération mathématique de ce groupement */
    private Operation operation;
    /** Le résultat cible que doit atteindre l'opération */
    private int resultatCible;
    /** La case contenant l'indice (la première en haut à gauche) */
    private Case caseOp;
    /** Liste des cases appartenant à ce groupement */
    private List<Case> listeCases;
    /** Combinaisons mathématiques possibles pour ce groupement */
    private List<List<Integer>> combinaisonsMaths;

    /**
     * Constructeur d'un groupement avec opération et résultat cible.
     * @param operation l'opération mathématique
     * @param resultatCible le résultat attendu
     */
    public GroupementCases(Operation operation, int resultatCible){
        this.operation = operation;
        this.resultatCible = resultatCible;
        this.listeCases = new ArrayList<>();
        this.combinaisonsMaths = new ArrayList<>();
    }

    /**
     * Constructeur de copie. Crée une copie indépendante du groupement.
     * @param source le groupement à copier
     */
    public GroupementCases(GroupementCases source) {
        this.operation = source.getOperation();           
        this.resultatCible = source.getResultatCible();   
        this.listeCases = new ArrayList<>(); 
        this.combinaisonsMaths = new ArrayList<>(source.getCombinaisonsMaths());
    }

    /**
     * Ajoute une case à ce groupement.
     * @param c la case à ajouter
     */
    public void ajouterCase(Case c){
        c.setGroupement(this);
        this.listeCases.add(c);
        mettreAJourCaseOp();     
    }
    /**
     * Met à jour la case contenant l'indice (la plus haut à gauche).
     */
    private void mettreAJourCaseOp(){
        Comparator<Case> comparateurCoords = Comparator
            .comparingInt(Case::getY)
            .thenComparingInt(Case::getX);

        this.caseOp = listeCases.stream()
            .min(comparateurCoords)
            .orElse(null);    
    }
    /**
     * Vérifie si le groupement est valide (toutes les cases remplies et l'opération correcte).
     * @return true si le groupement est correct
     */
    public boolean groupementValide() {
        List<Integer> valeurs = new ArrayList<>();
        for (Case c : listeCases) {
            if (c.getValeur() == 0) return false; 
            valeurs.add(c.getValeur());
        }
        return operation.calculer(valeurs) == resultatCible;
    }

    /**
     * Retourne les combinaisons mathématiques possibles pour ce groupement.
     * @return la liste des combinaisons
     */
    public List<List<Integer>> getCombinaisonsMaths() {
        return combinaisonsMaths;
    }

    /**
     * Retourne l'opération de ce groupement.
     * @return l'opération mathématique
     */
    public Operation getOperation() { return operation; }
    /**
     * Retourne le résultat cible du groupement.
     * @return le résultat attendu
     */
    public int getResultatCible() { return resultatCible; }
    /**
     * Définit le résultat cible du groupement.
     * @param res le nouveau résultat
     */
    public void setResultatCible(int res) { resultatCible = res ; }
    /**
     * Retourne la liste des cases du groupement.
     * @return une copie de la liste des cases
     */
    public List<Case> getListeCases() { return new ArrayList<>(listeCases); }
    /**
     * Retourne la case contenant l'indice (résultat et opération).
     * @return la case de l'indice
     */
    public Case getCaseOp() { return caseOp; }

    @Override
    public void accepter(VisiteurGrille visiteur) {
        visiteur.visiter(this);
    }
    /**
     * Calcule les combinaisons possibles du groupement selon l'état actuel de la grille.
     * @param grille la grille de jeu pour vérifier les contraintes de ligne/colonne
     */
    public void calculerPossibilites(Grille grille) {
        this.combinaisonsMaths.clear();
        Set<List<Integer>> setUnique = new HashSet<>();
        List<Case> casesDuGroupe = getListeCases();
        trouverCombinaisons(new ArrayList<>(), casesDuGroupe, setUnique,grille);
        this.combinaisonsMaths.addAll(setUnique);
    }
    /**
     * Recherche récursivement les combinaisons valides par backtracking.
     * @param valeursActuelles nombres en cours de test pour le groupement
     * @param toutesLesCases cases appartenant au groupement
     * @param setUnique stockage des combinaisons uniques trouvées
     * @param grille référence à la grille pour les vérifications globales
     */
    private void trouverCombinaisons(List<Integer> valeursActuelles, List<Case> toutesLesCases, Set<List<Integer>> setUnique, Grille grille) {
        if (valeursActuelles.size() == listeCases.size()) {
            if (operation.calculer(valeursActuelles) == resultatCible) {
                List<Integer> copie = new ArrayList<>(valeursActuelles);
                copie.sort(Integer::compareTo);
                setUnique.add(copie);
            }
            return;
        }
        int indexCaseActuelle = valeursActuelles.size();
        Case caseAremplir = toutesLesCases.get(indexCaseActuelle);
        int valeurDeLaGrille = caseAremplir.getValeur();
        if (valeurDeLaGrille != 0) {
            if (estPossible(valeurDeLaGrille, caseAremplir, valeursActuelles, toutesLesCases, grille)) {
                valeursActuelles.add(valeurDeLaGrille);
                trouverCombinaisons(valeursActuelles, toutesLesCases, setUnique, grille);
                valeursActuelles.remove(valeursActuelles.size() - 1); 
            }
            return;
        }
        else {
            for (int v = 1; v <= grille.getTaille(); v++) {
                if (estPossible(v, caseAremplir, valeursActuelles, toutesLesCases, grille)) {
                    valeursActuelles.add(v);
                    trouverCombinaisons(valeursActuelles, toutesLesCases, setUnique, grille);
                    valeursActuelles.remove(valeursActuelles.size() - 1); // Backtracking
                }
             }
         }
    }
    /**
     * Vérifie si une valeur respecte les règles du Calcudoku (groupement, ligne et colonne).
     * @param valeur le chiffre à tester
     * @param caseCible la case de destination
     * @param valeursPlacees chiffres déjà simulés dans le groupement
     * @param toutesLesCases liste des cases du groupe
     * @param grille la grille complète pour détecter les doublons distants
     * @return true si le coup est légal, false sinon
     */
    private boolean estPossible(int valeur, Case caseCible, List<Integer> valeursPlacees, List<Case> toutesLesCases, Grille grille) {
        for (int i = 0; i < grille.getTaille(); i++) {
            Case cLigne = grille.getCase(i, caseCible.getY());
            if (cLigne != caseCible && cLigne.getValeur() == valeur) 
                return false;

            Case cCol = grille.getCase(caseCible.getX(), i);
            if (cCol != caseCible && cCol.getValeur() == valeur)
                return false;
        }
        for (int i = 0; i < valeursPlacees.size(); i++) {
            Case casePrecedente = toutesLesCases.get(i);
            if (valeur == valeursPlacees.get(i)) {
                if (caseCible.getX() == casePrecedente.getX() || caseCible.getY() == casePrecedente.getY()) {
                    return false;
                }
            }
        }
        return true;
    }
}