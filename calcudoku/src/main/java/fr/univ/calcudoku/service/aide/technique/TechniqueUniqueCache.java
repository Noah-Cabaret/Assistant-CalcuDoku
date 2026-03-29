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

/**
 * Technique : Unique Caché (Hidden Single).
 * Identifie une ligne, colonne ou un bloc où un chiffre spécifique 
 * ne peut être placé que dans une seule case.
 */
public class TechniqueUniqueCache implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        List<Indice> indicesErreurs = new ArrayList<>();
        List<Indice> indicesNormaux = new ArrayList<>();

        // On parcourt tout et on collecte tous les uniques cachés
        analyserLignesEtColonnes(grille, indicesErreurs, indicesNormaux);
        analyserBlocs(grille, indicesErreurs, indicesNormaux);

        // Sélection aléatoire d'un indice (priorité aux erreurs signalées)
        Random rand = new Random();
        if (!indicesErreurs.isEmpty()) return indicesErreurs.get(rand.nextInt(indicesErreurs.size()));
        if (!indicesNormaux.isEmpty()) return indicesNormaux.get(rand.nextInt(indicesNormaux.size()));
        
        return null;
    }

    private void analyserLignesEtColonnes(Grille grille, List<Indice> erreurs, List<Indice> normaux) {
        int taille = grille.getTaille();
        for (int indexZone = 0; indexZone < taille; indexZone++) {
            for (int chiffre = 1; chiffre <= taille; chiffre++) {
                chercherUniqueCacheDansZone(grille, indexZone, chiffre, true, erreurs, normaux);
                chercherUniqueCacheDansZone(grille, indexZone, chiffre, false, erreurs, normaux);
            }
        }
    }

    private void chercherUniqueCacheDansZone(Grille grille, int indexZone, int chiffre, boolean estLigne, List<Indice> erreurs, List<Indice> normaux) {
        int taille = grille.getTaille();
        List<Case> placesPossibles = new ArrayList<>();
        List<Case> casesDeLaZone = new ArrayList<>();
        boolean chiffreDejaPresent = false;
        int nbCasesVides = 0;

        for (int i = 0; i < taille; i++) {
            int x = estLigne ? i : indexZone;
            int y = estLigne ? indexZone : i;
            Case c = grille.getCase(x, y);
            casesDeLaZone.add(c);
            
            if (c.getValeur() == 0) nbCasesVides++;
            if (c.getValeur() == chiffre) { chiffreDejaPresent = true; break; }
            
            if (c.getValeur() == 0 && grille.estCoupValide(c.getX(), c.getY(), chiffre)) {
                placesPossibles.add(c);
            }
        }

        if (chiffreDejaPresent) return;

        if (placesPossibles.size() == 1) {
            Case caseCible = placesPossibles.get(0);
            
            // Sécurité anti-fausse piste : on vérifie que la déduction n'est pas faussée par une erreur du joueur ailleurs
            if (caseCible.getSolution() != chiffre) return;

            int valeurJoueur = caseCible.getValeur();
            boolean contientErreur = (valeurJoueur != 0 && valeurJoueur != caseCible.getSolution());
            
            // Si la zone n'a plus qu'une case vide, on laisse "Dernière Case Ligne/Colonne" gérer (technique plus simple)
            if (!contientErreur && nbCasesVides <= 1) return;

            List<Case> casesASurbriller = new ArrayList<>();
            Map<Case, Integer> solutions = new HashMap<>();
            String nomZone = estLigne ? "la ligne " + (indexZone + 1) : "la colonne " + (indexZone + 1);

            if (contientErreur) {
                casesASurbriller.add(caseCible);
                String msg = "Erreur ! Le chiffre " + chiffre + " doit obligatoirement figurer dans " + nomZone + ", et à cause des intersections, cette case est sa seule place valide.";
                erreurs.add(new Indice("Unique Caché", msg, casesASurbriller, solutions, true));
            } else {
                casesASurbriller.addAll(casesDeLaZone);
                String msg = "Technique de l'unique caché : Regardez " + nomZone + ".\nEn croisant les données avec les blocs et autres lignes/colonnes, le chiffre " + chiffre + " n'a plus qu'une seule case où il peut être placé !";
                normaux.add(new Indice("Unique Caché", msg, casesASurbriller, solutions, false));
            }
        }
    }

    private void analyserBlocs(Grille grille, List<Indice> erreurs, List<Indice> normaux) {
        int taille = grille.getTaille();
        
        for (GroupementCases bloc : grille.getListeGroupements()) {
            if (bloc.getListeCases().size() <= 1) continue;

            int nbCasesVides = 0;
            for(Case c : bloc.getListeCases()) {
                if (c.getValeur() == 0) nbCasesVides++;
            }

            for (int chiffre = 1; chiffre <= taille; chiffre++) {
                if (blocContientChiffreValide(bloc, chiffre)) continue; 
                
                // Le bloc DOIT contenir ce chiffre (toutes ses combinaisons mathématiques valides l'incluent)
                if (!blocRequiertChiffre(bloc, chiffre)) continue; 

                List<Case> placesPossibles = new ArrayList<>();
                for (Case c : bloc.getListeCases()) {
                    if (c.getValeur() == 0 && grille.estCoupValide(c.getX(), c.getY(), chiffre)) {
                        placesPossibles.add(c);
                    }
                }

                if (placesPossibles.size() == 1) {
                    Case caseCible = placesPossibles.get(0);
                    
                    // Sécurité anti-fausse piste
                    if (caseCible.getSolution() != chiffre) continue;

                    int valeurJoueur = caseCible.getValeur();
                    boolean contientErreur = (valeurJoueur != 0 && valeurJoueur != caseCible.getSolution());
                    
                    // On laisse "Dernière Case Bloc" s'en charger s'il reste une seule case vide
                    if (!contientErreur && nbCasesVides <= 1) continue;

                    List<Case> casesASurbriller = new ArrayList<>();
                    Map<Case, Integer> solutions = new HashMap<>();

                    if (contientErreur) {
                        casesASurbriller.add(caseCible);
                        erreurs.add(new Indice("Unique Caché (Bloc)", "Erreur détectée dans ce bloc.\nLe chiffre " + chiffre + " doit obligatoirement y figurer (mathématiquement) et cette case est sa seule place valide.", casesASurbriller, solutions, true));
                    } else {
                        casesASurbriller.addAll(bloc.getListeCases());
                        String symbole = bloc.getOperation() != null ? bloc.getOperation().getSymbole() : "";
                        int cible = bloc.getResultatCible();
                        
                        String msg = "Technique unique caché (Bloc) : Pour faire " + cible + " (" + symbole + "), toutes les combinaisons possibles nécessitent le chiffre " + chiffre + " !\nPar processus d'élimination avec les lignes/colonnes, il ne peut être placé que dans une seule case du bloc.";
                        normaux.add(new Indice("Unique Caché (Bloc)", msg, casesASurbriller, solutions, false));
                    }
                }
            }
        }
    }

    private boolean blocContientChiffreValide(GroupementCases bloc, int chiffre) {
        for (Case c : bloc.getListeCases()) {
            if (c.getValeur() == chiffre) return true;
        }
        return false;
    }

    private boolean blocRequiertChiffre(GroupementCases bloc, int chiffre) {
        if (bloc.getCombinaisonsMaths() == null || bloc.getCombinaisonsMaths().isEmpty()) return false;
        // Vérifie si le chiffre est présent dans ABSOLUMENT TOUTES les combinaisons
        for (List<Integer> combinaison : bloc.getCombinaisonsMaths()) {
            if (!combinaison.contains(chiffre)) return false; 
        }
        return true; 
    }
}