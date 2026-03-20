package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.service.aide.visitor.VisiteurGrille;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TechniqueUniqueCache implements TechniqueAide, VisiteurGrille {

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
        // MODIFICATION (Filtre anti-doublon) : On ne scanne plus les Lignes et Colonnes ici !
        // C'est le travail exclusif de "TechniquePlaceUniqueLigneColonne".
    }

    @Override
    public void visiter(GroupementCases groupement) {
        if (indiceTrouve != null) return;
        
        // Ignorer les cages de 1 case (TechniqueBlocDe1 s'en charge)
        if (groupement.getListeCases().size() <= 1) return;

        // MODIFICATION (Filtre anti-doublon) : Si la cage est presque terminée, 
        // on laisse "TechniqueDerniereCaseBloc" s'en charger.
        int nbCasesVides = 0;
        for(Case c : groupement.getListeCases()) {
            if (c.getValeur() == 0) nbCasesVides++;
        }
        if (nbCasesVides <= 1) return;

        int taille = grilleActuelle.getTaille();

        for (int chiffre = 1; chiffre <= taille; chiffre++) {
            if (blocContientChiffreValide(groupement, chiffre)) continue; 
            if (!blocAccepteChiffre(groupement, chiffre)) continue; 

            List<Case> placesPossibles = new ArrayList<>();
            for (Case c : groupement.getListeCases()) {
                // On autorise l'analyse des cases même si le joueur a mis une mauvaise valeur
                if (c.getValeur() != chiffre && grilleActuelle.estCoupValide(c.getX(), c.getY(), chiffre)) {
                    placesPossibles.add(c);
                }
            }

            if (placesPossibles.size() == 1) {
                Case caseCible = placesPossibles.get(0);
                genererIndice("Bloc", groupement.getListeCases(), caseCible, chiffre, "cette cage mathématique");
                return;
            }
        }
    }

    @Override
    public void visiter(Case c) {}

    private void genererIndice(String typeZone, List<Case> casesASurbriller, Case caseCible, int chiffre, String nomZone) {
        int valeurJoueur = caseCible.getValeur();
        
        if (valeurJoueur == chiffre) return;

        // MODIFICATION : Détection d'erreur
        boolean contientErreur = (valeurJoueur != 0);

        String message;
        if (contientErreur) {
            message = "Erreur détectée dans " + nomZone + ".\n" +
                      "La case ciblée a été remplie avec un " + valeurJoueur + ", mais elle est mathématiquement la seule place valide pour le chiffre " + chiffre + ".";
        } else {
            message = "Regardez " + nomZone + ".\n" +
                      "Le chiffre " + chiffre + " doit obligatoirement y figurer pour terminer le puzzle.\n" +
                      "Toutes les autres cases de cette zone sont bloquées. Il n'y a donc qu'une seule place possible pour le placer !";
        }

        Map<Case, Integer> solutions = new HashMap<>();
        solutions.put(caseCible, chiffre);

        this.indiceTrouve = new Indice("Unique Caché (" + typeZone + ")", message, casesASurbriller, solutions, contientErreur);
    }

    private boolean blocContientChiffreValide(GroupementCases bloc, int chiffre) {
        for (Case c : bloc.getListeCases()) {
            if (c.getValeur() == chiffre) return true;
        }
        return false;
    }

    private boolean blocAccepteChiffre(GroupementCases bloc, int chiffre) {
        if (bloc.getCombinaisonsMaths() == null || bloc.getCombinaisonsMaths().isEmpty()) {
            return true; 
        }
        for (List<Integer> combinaison : bloc.getCombinaisonsMaths()) {
            if (combinaison.contains(chiffre)) {
                return true;
            }
        }
        return false;
    }
}