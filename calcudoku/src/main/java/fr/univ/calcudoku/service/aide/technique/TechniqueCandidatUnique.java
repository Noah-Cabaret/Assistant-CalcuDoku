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

public class TechniqueCandidatUnique implements TechniqueAide, VisiteurGrille {

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
    public void visiter(Grille g) {}

    @Override
    public void visiter(GroupementCases groupement) {}

    @Override
    public void visiter(Case c) {
        if (indiceTrouve != null) {
            return;
        }

        int taille = grilleActuelle.getTaille();
        Set<Integer> chiffresVus = new HashSet<>();
        
        int nbSurLigne = 0;
        int nbSurColonne = 0;

        // ÉTAPE 1 : Scanner la LIGNE de cette case
        for (int i = 0; i < taille; i++) {
            if (i != c.getX()) {
                int valeurLigne = grilleActuelle.getCase(i, c.getY()).getValeur();
                if (valeurLigne != 0) {
                    chiffresVus.add(valeurLigne);
                    nbSurLigne++;
                }
            }
        }

        // ÉTAPE 2 : Scanner la COLONNE de cette case
        for (int j = 0; j < taille; j++) {
            if (j != c.getY()) {
                int valeurColonne = grilleActuelle.getCase(c.getX(), j).getValeur();
                if (valeurColonne != 0) {
                    chiffresVus.add(valeurColonne);
                    nbSurColonne++;
                }
            }
        }

        // MODIFICATION (Filtre anti-doublon) : 
        // Si la ligne (ou la colonne) contient DÉJÀ tous les autres chiffres (Taille - 1),
        // on annule ! C'est la technique "Dernière Case Ligne/Colonne" qui doit s'en charger.
        if (nbSurLigne == taille - 1 || nbSurColonne == taille - 1) {
            return;
        }

        // ÉTAPE 3 : Vérification Mathématique (Le croisement)
        if (chiffresVus.size() == taille - 1) {
            int chiffreManquant = trouverChiffreManquant(chiffresVus, taille);
            int valeurJoueur = c.getValeur();

            // Si le joueur a déjà mis la bonne réponse, on passe
            if (valeurJoueur == chiffreManquant) {
                return;
            }

            // MODIFICATION : Détection de l'erreur
            boolean contientErreur = (valeurJoueur != 0);

            String nom = "Candidat Unique";
            String message;
            if (contientErreur) {
                message = "Erreur détectée ! La case située à la ligne " + (c.getY() + 1) + " et colonne " + (c.getX() + 1) + " "
                        + "ne peut contenir que le chiffre " + chiffreManquant + " à cause des autres chiffres croisés sur sa ligne et sa colonne.";
            } else {
                message = "Regardez la case située à la ligne " + (c.getY() + 1) + " et à la colonne " + (c.getX() + 1) + ".\n"
                        + "En croisant les chiffres déjà présents sur sa ligne et sa colonne, il ne reste plus qu'une seule possibilité : le chiffre " + chiffreManquant + ".";
            }

            List<Case> casesASurbriller = new ArrayList<>();
            casesASurbriller.add(c);

            Map<Case, Integer> solutions = new HashMap<>();
            solutions.put(c, chiffreManquant);

            this.indiceTrouve = new Indice(nom, message, casesASurbriller, solutions, contientErreur);
        }
    }

    private int trouverChiffreManquant(Set<Integer> chiffresVus, int taille) {
        for (int n = 1; n <= taille; n++) {
            if (!chiffresVus.contains(n)) {
                return n;
            }
        }
        return -1;
    }
}