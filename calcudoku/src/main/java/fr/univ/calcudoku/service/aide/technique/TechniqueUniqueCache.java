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

/**
 * Technique de l'Unique Caché (Hidden Single / Place Unique).
 * Cherche si un chiffre spécifique (de 1 à N) n'a qu'une seule place mathématiquement 
 * possible dans une zone donnée (Ligne, Colonne ou Bloc).
 */
public class TechniqueUniqueCache implements TechniqueAide, VisiteurGrille {

    private Grille grilleActuelle;
    private Indice indiceTrouve;

    // ==========================================
    // IMPLÉMENTATION STRATÉGIE
    // ==========================================

    @Override
    public Indice analyser(Grille grille) {
        this.grilleActuelle = grille;
        this.indiceTrouve = null;

        // Le Visiteur va d'abord appeler visiter(Grille), puis visiter(GroupementCases)
        grille.accepter(this);

        return indiceTrouve;
    }

    // ==========================================
    // IMPLÉMENTATION VISITEUR
    // ==========================================

    @Override
    public void visiter(Grille g) {
        if (indiceTrouve != null) return;

        int taille = g.getTaille();

        // ÉTAPE 1 : Analyse de chaque Ligne et Colonne
        for (int i = 0; i < taille; i++) {
            chercherPlaceUniqueLigneOuColonne(i, true);  // Recherche sur la Ligne 'i'
            if (indiceTrouve != null) return; // On s'arrête dès qu'on a une aide
            
            chercherPlaceUniqueLigneOuColonne(i, false); // Recherche sur la Colonne 'i'
            if (indiceTrouve != null) return;
        }
    }

    @Override
    public void visiter(GroupementCases groupement) {
        if (indiceTrouve != null) return;
        
        // Filtre de difficulté : on ignore les cages de 1 seule case
        if (groupement.getListeCases().size() <= 1) return;

        int taille = grilleActuelle.getTaille();

        // ÉTAPE 2 : Analyse du Bloc (Cage)
        // Pour chaque chiffre possible (1 à N)
        for (int chiffre = 1; chiffre <= taille; chiffre++) {
            
            if (blocContientChiffre(groupement, chiffre)) continue; // Chiffre déjà posé
            if (!blocAccepteChiffre(groupement, chiffre)) continue; // La cage interdit ce chiffre (mathématiquement)

            // On cherche dans quelles cases vides du bloc ce chiffre a le droit d'aller
            List<Case> placesPossibles = new ArrayList<>();
            for (Case c : groupement.getListeCases()) {
                if (c.getValeur() == 0 && grilleActuelle.estCoupValide(c.getX(), c.getY(), chiffre)) {
                    placesPossibles.add(c);
                }
            }

            // SUCCESS : S'il n'y a qu'une seule place pour le chiffre dans ce bloc
            if (placesPossibles.size() == 1) {
                Case caseCible = placesPossibles.get(0);
                genererIndice("Bloc", groupement.getListeCases(), caseCible, chiffre, "cette cage mathématique");
                return;
            }
        }
    }

    @Override
    public void visiter(Case c) {
        // Inutile ici : cette technique s'applique uniquement à des "Zones" (Ligne, Col, Bloc).
    }

    // ==========================================
    // MÉTHODES UTILITAIRES PRIVÉES
    // ==========================================

    private void chercherPlaceUniqueLigneOuColonne(int index, boolean estLigne) {
        int taille = grilleActuelle.getTaille();

        for (int chiffre = 1; chiffre <= taille; chiffre++) {
            if (chiffreDejaPlaceLigneCol(index, estLigne, chiffre)) continue;

            List<Case> placesPossibles = new ArrayList<>();
            List<Case> zoneComplete = new ArrayList<>();

            for (int i = 0; i < taille; i++) {
                int x = estLigne ? i : index;
                int y = estLigne ? index : i;
                Case c = grilleActuelle.getCase(x, y);
                zoneComplete.add(c);

                if (c.getValeur() == 0) {
                    // Le chiffre a le droit d'aller dans cette case SI :
                    // 1. Il n'est pas déjà sur la croix (estCoupValide)
                    // 2. Le bloc auquel appartient cette case autorise ce chiffre
                    if (grilleActuelle.estCoupValide(x, y, chiffre) && blocAccepteChiffre(c.getGroupement(), chiffre)) {
                        placesPossibles.add(c);
                    }
                }
            }

            // SUCCESS : S'il n'y a qu'une seule place dans la ligne/colonne
            if (placesPossibles.size() == 1) {
                Case caseCible = placesPossibles.get(0);
                String nomZone = estLigne ? "la ligne " + (index + 1) : "la colonne " + (index + 1);
                genererIndice(estLigne ? "Ligne" : "Colonne", zoneComplete, caseCible, chiffre, nomZone);
                return;
            }
        }
    }

    private void genererIndice(String typeZone, List<Case> casesASurbriller, Case caseCible, int chiffre, String nomZone) {
        String message = "Regardez " + nomZone + ".\n" +
                         "Le chiffre " + chiffre + " doit obligatoirement y figurer pour terminer le puzzle.\n" +
                         "Toutes les autres cases de cette zone sont bloquées (soit par un chiffre identique à l'intersection, soit parce que leur propre bloc mathématique l'interdit).\n" +
                         "Il n'y a donc qu'une seule place possible pour le placer !";

        Map<Case, Integer> solutions = new HashMap<>();
        solutions.put(caseCible, chiffre);

        // On crée l'indice (niveau 1 : on met la zone en surbrillance, niveau 2 : on donne le chiffre)
        this.indiceTrouve = new Indice("Unique Caché (" + typeZone + ")", message, casesASurbriller, solutions, false);
    }

    private boolean chiffreDejaPlaceLigneCol(int index, boolean estLigne, int chiffre) {
        for (int i = 0; i < grilleActuelle.getTaille(); i++) {
            int x = estLigne ? i : index;
            int y = estLigne ? index : i;
            if (grilleActuelle.getCase(x, y).getValeur() == chiffre) return true;
        }
        return false;
    }

    private boolean blocContientChiffre(GroupementCases bloc, int chiffre) {
        for (Case c : bloc.getListeCases()) {
            if (c.getValeur() == chiffre) return true;
        }
        return false;
    }

    /**
     * Vérifie si la cage peut mathématiquement contenir ce chiffre.
     * Basé sur les combinaisons générées par calculerPossibilites().
     */
    private boolean blocAccepteChiffre(GroupementCases bloc, int chiffre) {
        // Note: Assure-toi que la méthode getCombinaisonsMaths() existe bien 
        // dans ta classe GroupementCases comme le laissait penser ton ancien code !
        if (bloc.getCombinaisonsMaths() == null || bloc.getCombinaisonsMaths().isEmpty()) {
            return true; // Fallback si les combinaisons ne sont pas encore calculées
        }
        
        for (List<Integer> combinaison : bloc.getCombinaisonsMaths()) {
            if (combinaison.contains(chiffre)) {
                return true;
            }
        }
        return false;
    }
}