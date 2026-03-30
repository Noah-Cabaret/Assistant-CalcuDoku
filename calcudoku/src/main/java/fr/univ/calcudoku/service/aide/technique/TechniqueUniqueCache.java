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
 * Technique d'aide : Unique Caché (Hidden Single).
 * Identifie une ligne, une colonne ou un bloc où un chiffre spécifique 
 * ne peut être placé que dans une seule case après déduction.
 */
public class TechniqueUniqueCache implements TechniqueAide {

    /**
     * Analyse l'ensemble de la grille (lignes, colonnes et blocs) pour trouver un unique caché.
     * @param grille La grille à analyser.
     * @return Un Indice avec des messages progressifs.
     */
    @Override
    public Indice analyser(Grille grille) {
        List<Indice> indicesErreurs = new ArrayList<>();
        List<Indice> indicesNormaux = new ArrayList<>();

        analyserLignesEtColonnes(grille, indicesErreurs, indicesNormaux);
        analyserBlocs(grille, indicesErreurs, indicesNormaux);

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
            
            if (caseCible.getSolution() != chiffre) return;

            int valeurJoueur = caseCible.getValeur();
            boolean contientErreur = (valeurJoueur != 0 && valeurJoueur != caseCible.getSolution());
            
            if (!contientErreur && nbCasesVides <= 1) return;

            List<Case> casesASurbriller = new ArrayList<>();
            Map<Case, Integer> solutions = new HashMap<>();
            List<String> messages = new ArrayList<>();
            String nomZone = estLigne ? "la ligne " + (indexZone + 1) : "la colonne " + (indexZone + 1);

            if (contientErreur) {
                casesASurbriller.add(caseCible);
                messages.add("Il y a une incohérence. Le placement d'un chiffre bloque la résolution d'une ligne ou colonne.");
                messages.add("Le chiffre " + chiffre + " devait obligatoirement figurer dans cette zone, mais sa seule place possible est occupée.");
                messages.add("Erreur ! Le chiffre " + chiffre + " doit figurer dans " + nomZone + ", et à cause des intersections, la case en surbrillance est sa seule place valide.");
                erreurs.add(new Indice("Unique Caché", messages, casesASurbriller, solutions, true));
            } else {
                casesASurbriller.addAll(casesDeLaZone);
                messages.add("Analysez attentivement les lignes et les colonnes. Un chiffre se cache et n'a plus qu'une seule option.");
                messages.add("En croisant les données avec les blocs et les autres axes, vous pouvez déduire l'unique place du chiffre " + chiffre + ".");
                messages.add("Technique de l'unique caché : Regardez " + nomZone + " en surbrillance. Le chiffre " + chiffre + " n'a plus qu'une seule case où il peut être placé !");
                normaux.add(new Indice("Unique Caché", messages, casesASurbriller, solutions, false));
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
                if (!blocRequiertChiffre(bloc, chiffre)) continue; 

                List<Case> placesPossibles = new ArrayList<>();
                for (Case c : bloc.getListeCases()) {
                    if (c.getValeur() == 0 && grille.estCoupValide(c.getX(), c.getY(), chiffre)) {
                        placesPossibles.add(c);
                    }
                }

                if (placesPossibles.size() == 1) {
                    Case caseCible = placesPossibles.get(0);
                    if (caseCible.getSolution() != chiffre) continue;

                    int valeurJoueur = caseCible.getValeur();
                    boolean contientErreur = (valeurJoueur != 0 && valeurJoueur != caseCible.getSolution());
                    
                    if (!contientErreur && nbCasesVides <= 1) continue;

                    List<Case> casesASurbriller = new ArrayList<>();
                    Map<Case, Integer> solutions = new HashMap<>();
                    List<String> messages = new ArrayList<>();

                    if (contientErreur) {
                        casesASurbriller.add(caseCible);
                        messages.add("Une erreur a été détectée dans l'un des blocs. Une déduction mathématique a été ignorée.");
                        messages.add("Le chiffre " + chiffre + " doit obligatoirement figurer dans ce bloc pour atteindre son résultat.");
                        messages.add("Erreur ! Le chiffre " + chiffre + " doit mathématiquement figurer dans ce bloc, et la case en surbrillance est sa seule place valide.");
                        erreurs.add(new Indice("Unique Caché (Bloc)", messages, casesASurbriller, solutions, true));
                    } else {
                        casesASurbriller.addAll(bloc.getListeCases());
                        String symbole = bloc.getOperation() != null ? bloc.getOperation().getSymbole() : "";
                        int cible = bloc.getResultatCible();
                        
                        messages.add("Concentrez-vous sur les blocs. L'un d'eux a un chiffre obligatoire dans toutes ses combinaisons possibles.");
                        messages.add("Pour atteindre la cible mathématique " + cible + " (" + symbole + "), vous devez absolument utiliser le chiffre " + chiffre + ".");
                        messages.add("Unique caché (Bloc) : Par élimination, le chiffre " + chiffre + " ne peut être placé que dans une seule case du bloc en surbrillance.");
                        normaux.add(new Indice("Unique Caché (Bloc)", messages, casesASurbriller, solutions, false));
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
        for (List<Integer> combinaison : bloc.getCombinaisonsMaths()) {
            if (!combinaison.contains(chiffre)) return false; 
        }
        return true; 
    }
}