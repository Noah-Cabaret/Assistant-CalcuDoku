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

/**
 * Technique du Candidat Unique (Naked Single).
 * Analyse si une case vide n'a plus qu'une seule possibilité mathématique 
 * en fonction des chiffres déjà validés sur sa ligne et sa colonne.
 */
public class TechniqueCandidatUnique implements TechniqueAide, VisiteurGrille {

    // On stocke la grille et l'indice trouvé en attributs de classe 
    // pour que le Visiteur puisse y accéder pendant son parcours.
    private Grille grilleActuelle;
    private Indice indiceTrouve;

    // ==========================================
    // IMPLÉMENTATION DU DESIGN PATTERN STRATÉGIE
    // ==========================================

    @Override
    public Indice analyser(Grille grille) {
        this.grilleActuelle = grille;
        this.indiceTrouve = null; // On réinitialise à chaque nouvelle analyse

        // On lance le parcours de la grille grâce au Visiteur
        // La grille va appeler la méthode "visiter(Case c)" sur toutes ses cases
        grille.accepter(this);

        // Une fois le parcours terminé, on renvoie l'indice (ou null s'il n'y a rien)
        return indiceTrouve;
    }

    // ==========================================
    // IMPLÉMENTATION DU DESIGN PATTERN VISITEUR
    // ==========================================

    @Override
    public void visiter(Grille g) {
        // Pas besoin de logique globale sur la grille pour cette technique
    }

    @Override
    public void visiter(GroupementCases groupement) {
        // Pas besoin d'analyser les blocs mathématiques pour cette technique
    }

    @Override
    public void visiter(Case c) {
        // CONDITIONS D'ARRÊT :
        // 1. Si on a DÉJÀ trouvé un indice, on ne fait plus rien (on veut renvoyer une seule aide à la fois)
        // 2. Si la case est déjà remplie (valeur != 0), on passe à la suivante
        if (indiceTrouve != null || c.getValeur() != 0) {
            return;
        }

        int taille = grilleActuelle.getTaille();
        Set<Integer> chiffresVus = new HashSet<>();

        // ÉTAPE 1 : Scanner la LIGNE de cette case
        for (int i = 0; i < taille; i++) {
            int valeurLigne = grilleActuelle.getCase(i, c.getY()).getValeur();
            if (valeurLigne != 0) {
                chiffresVus.add(valeurLigne);
            }
        }

        // ÉTAPE 2 : Scanner la COLONNE de cette case
        for (int j = 0; j < taille; j++) {
            int valeurColonne = grilleActuelle.getCase(c.getX(), j).getValeur();
            if (valeurColonne != 0) {
                chiffresVus.add(valeurColonne);
            }
        }

        // ÉTAPE 3 : Vérification Mathématique
        // S'il y a exactement (Taille - 1) chiffres différents sur la croix (ligne + colonne)
        // Alors il ne manque qu'un seul chiffre possible pour cette case.
        if (chiffresVus.size() == taille - 1) {
            
            int chiffreManquant = trouverChiffreManquant(chiffresVus, taille);

            // ÉTAPE 4 : Construction de la réponse (l'Indice)
            String message = "Regardez la case située à la ligne " + (c.getY() + 1) 
                           + " et à la colonne " + (c.getX() + 1) + ".\n"
                           + "En regardant les chiffres déjà présents sur sa ligne et sa colonne, "
                           + "il ne reste plus qu'une seule possibilité : le chiffre " + chiffreManquant + ".";

            // Les cases à mettre en surbrillance (Niveau 1 de l'aide)
            List<Case> casesASurbriller = new ArrayList<>();
            casesASurbriller.add(c);

            // La solution à remplir automatiquement ou à annoter (Niveau 2 de l'aide)
            Map<Case, Integer> solutions = new HashMap<>();
            solutions.put(c, chiffreManquant);

            // On génère l'indice !
            this.indiceTrouve = new Indice(
                "Candidat Unique",
                message,
                casesASurbriller,
                solutions,
                false // ne contient pas d'erreur
            );
        }
    }

    // ==========================================
    // MÉTHODE UTILITAIRE PRIVÉE
    // ==========================================

    /**
     * Permet de déduire quel chiffre (entre 1 et la taille de la grille) 
     * est absent de la liste des chiffres déjà vus.
     */
    private int trouverChiffreManquant(Set<Integer> chiffresVus, int taille) {
        for (int n = 1; n <= taille; n++) {
            if (!chiffresVus.contains(n)) {
                return n;
            }
        }
        return -1; // Sécurité (ne devrait jamais arriver si la logique au-dessus est respectée)
    }
}