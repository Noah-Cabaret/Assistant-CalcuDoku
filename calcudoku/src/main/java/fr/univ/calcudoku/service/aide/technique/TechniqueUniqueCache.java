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

public class TechniqueUniqueCache implements TechniqueAide {

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

        for (int i = 0; i < taille; i++) {
            Case c = estLigne ? grille.getCase(i, indexZone) : grille.getCase(indexZone, i);
            casesDeLaZone.add(c);
            
            if (c.getValeur() == chiffre) { chiffreDejaPresent = true; break; }
            if (c.getValeur() == 0 && grille.estCoupValide(c.getX(), c.getY(), chiffre)) placesPossibles.add(c);
        }

        if (chiffreDejaPresent) return;

        if (placesPossibles.size() == 1) {
            Case caseCible = placesPossibles.get(0);
            int valeurJoueur = caseCible.getValeur();
            boolean contientErreur = (valeurJoueur != 0 && valeurJoueur != caseCible.getSolution());
            
            List<Case> casesASurbriller = new ArrayList<>();
            Map<Case, Integer> solutions = new HashMap<>();

            if (contientErreur) {
                casesASurbriller.add(caseCible);
                erreurs.add(new Indice("Unique Caché", "Erreur détectée ! Le chiffre " + chiffre + " doit obligatoirement figurer dans cette " + (estLigne ? "ligne" : "colonne") + " et cette case est sa seule place valide.", casesASurbriller, solutions, true));
            } else {
                casesASurbriller.addAll(casesDeLaZone);
                String msg = "Techniques uniques cachées : Regardez cette " + (estLigne ? "ligne" : "colonne") + ". En croisant les données, le chiffre " + chiffre + " n'a plus qu'une seule case où il peut être placé !";
                normaux.add(new Indice("Unique Caché", msg, casesASurbriller, solutions, false));
            }
        }
    }

    private void analyserBlocs(Grille grille, List<Indice> erreurs, List<Indice> normaux) {
        for (GroupementCases bloc : grille.getListeGroupements()) {
            if (bloc.getListeCases().size() <= 1) continue;

            int nbCasesVides = 0;
            for(Case c : bloc.getListeCases()) if (c.getValeur() == 0) nbCasesVides++;

            int taille = grille.getTaille();

            for (int chiffre = 1; chiffre <= taille; chiffre++) {
                if (blocContientChiffreValide(bloc, chiffre)) continue; 
                if (!blocRequiertChiffre(bloc, chiffre)) continue; 

                List<Case> placesPossibles = new ArrayList<>();
                for (Case c : bloc.getListeCases()) {
                    if (c.getValeur() != chiffre && grille.estCoupValide(c.getX(), c.getY(), chiffre)) placesPossibles.add(c);
                }

                if (placesPossibles.size() == 1) {
                    Case caseCible = placesPossibles.get(0);
                    int valeurJoueur = caseCible.getValeur();
                    if (valeurJoueur == chiffre) continue;

                    boolean contientErreur = (valeurJoueur != 0 && valeurJoueur != caseCible.getSolution());
                    if (!contientErreur && nbCasesVides <= 1) continue;

                    List<Case> casesASurbriller = new ArrayList<>();
                    Map<Case, Integer> solutions = new HashMap<>();

                    if (contientErreur) {
                        casesASurbriller.add(caseCible);
                        erreurs.add(new Indice("Unique Caché (Bloc)", "Erreur détectée dans ce bloc.\nLe chiffre " + chiffre + " doit obligatoirement y figurer et cette case est sa seule place valide.", casesASurbriller, solutions, true));
                    } else {
                        casesASurbriller.addAll(bloc.getListeCases());
                        String msg = "Techniques uniques cachées : Un certain chiffre indispensable à ce bloc ne peut être placé que dans une seule case. Par processus d'élimination avec les lignes/colonnes, trouvez où il va !";
                        normaux.add(new Indice("Unique Caché (Bloc)", msg, casesASurbriller, solutions, false));
                    }
                }
            }
        }
    }

    private boolean blocContientChiffreValide(GroupementCases bloc, int chiffre) {
        for (Case c : bloc.getListeCases()) if (c.getValeur() == chiffre) return true;
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