package fr.univ.calcudoku.service.aide.technique;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Technique : Unique Caché (Lignes, Colonnes et Blocs).
 * Identifie un chiffre qui n'a qu'une seule case physiquement disponible
 * dans une zone donnée (ligne, colonne ou bloc).
 */
public class TechniqueUniqueCache implements TechniqueAide {

    @Override
    public Indice analyser(Grille grille) {
        // 1. D'abord on vérifie les uniques cachés sur les Lignes et Colonnes
        Indice indiceLigneCol = analyserLignesEtColonnes(grille);
        if (indiceLigneCol != null && indiceLigneCol.aUneErreur()) return indiceLigneCol;

        // 2. Ensuite on vérifie les uniques cachés mathématiques dans les blocs
        Indice indiceBloc = analyserBlocs(grille);
        if (indiceBloc != null && indiceBloc.aUneErreur()) return indiceBloc;

        // Retourne le premier indice trouvé (privilégiant Lignes/Col, puis Blocs)
        return indiceLigneCol != null ? indiceLigneCol : indiceBloc;
    }

    private Indice analyserLignesEtColonnes(Grille grille) {
        int taille = grille.getTaille();
        Indice indiceNormal = null;

        for (int indexZone = 0; indexZone < taille; indexZone++) {
            for (int chiffre = 1; chiffre <= taille; chiffre++) {
                
                // Test sur la ligne
                Indice indLigne = chercherUniqueCacheDansZone(grille, indexZone, chiffre, true);
                if (indLigne != null) {
                    if (indLigne.aUneErreur()) return indLigne;
                    if (indiceNormal == null) indiceNormal = indLigne;
                }

                // Test sur la colonne
                Indice indCol = chercherUniqueCacheDansZone(grille, indexZone, chiffre, false);
                if (indCol != null) {
                    if (indCol.aUneErreur()) return indCol;
                    if (indiceNormal == null) indiceNormal = indCol;
                }
            }
        }
        return indiceNormal;
    }

    private Indice chercherUniqueCacheDansZone(Grille grille, int indexZone, int chiffre, boolean estLigne) {
        int taille = grille.getTaille();
        List<Case> placesPossibles = new ArrayList<>();
        List<Case> casesDeLaZone = new ArrayList<>();
        boolean chiffreDejaPresent = false;

        for (int i = 0; i < taille; i++) {
            Case c = estLigne ? grille.getCase(i, indexZone) : grille.getCase(indexZone, i);
            casesDeLaZone.add(c);
            
            if (c.getValeur() == chiffre) {
                chiffreDejaPresent = true;
                break;
            }
            if (c.getValeur() == 0 && grille.estCoupValide(c.getX(), c.getY(), chiffre)) {
                placesPossibles.add(c);
            }
        }

        if (chiffreDejaPresent) return null;

        if (placesPossibles.size() == 1) {
            Case caseCible = placesPossibles.get(0);
            int valeurJoueur = caseCible.getValeur();

            boolean contientErreur = (valeurJoueur != 0 && valeurJoueur != caseCible.getSolution());
            
            List<Case> casesASurbriller = new ArrayList<>();
            Map<Case, Integer> solutions = new HashMap<>();

            if (contientErreur) {
                casesASurbriller.add(caseCible);
                return new Indice("Unique Caché", "Erreur détectée ! Le chiffre " + chiffre + " doit obligatoirement figurer dans cette " + (estLigne ? "ligne" : "colonne") + " et cette case est sa seule place valide.", casesASurbriller, solutions, true);
            } else {
                casesASurbriller.addAll(casesDeLaZone);
                String msg = "Techniques uniques cachées : Regardez cette " + (estLigne ? "ligne" : "colonne") + ". En croisant les données, le chiffre " + chiffre + " n'a plus qu'une seule case où il peut être placé !";
                return new Indice("Unique Caché", msg, casesASurbriller, solutions, false);
            }
        }
        return null;
    }

    private Indice analyserBlocs(Grille grille) {
        Indice indiceNormal = null;

        for (GroupementCases bloc : grille.getListeGroupements()) {
            if (bloc.getListeCases().size() <= 1) continue;

            int nbCasesVides = 0;
            for(Case c : bloc.getListeCases()) if (c.getValeur() == 0) nbCasesVides++;

            int taille = grille.getTaille();

            for (int chiffre = 1; chiffre <= taille; chiffre++) {
                if (blocContientChiffreValide(bloc, chiffre)) continue; 
                
                // Le chiffre doit être OBLIGATOIRE dans les mathématiques du bloc
                if (!blocRequiertChiffre(bloc, chiffre)) continue; 

                // Cherche toutes les places non menacées par un même chiffre sur la ligne/colonne
                List<Case> placesPossibles = new ArrayList<>();
                for (Case c : bloc.getListeCases()) {
                    if (c.getValeur() != chiffre && grille.estCoupValide(c.getX(), c.getY(), chiffre)) {
                        placesPossibles.add(c);
                    }
                }

                // S'il n'y a qu'une place, c'est un Unique Caché
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
                        return new Indice("Unique Caché (Bloc)", "Erreur détectée dans ce bloc.\nLe chiffre " + chiffre + " doit obligatoirement y figurer et cette case est sa seule place valide.", casesASurbriller, solutions, true);
                    } else if (indiceNormal == null) {
                        casesASurbriller.addAll(bloc.getListeCases());
                        String msg = "Techniques uniques cachées : Un certain chiffre indispensable à ce bloc ne peut être placé que dans une seule case. Par processus d'élimination avec les lignes/colonnes, trouvez où il va !";
                        indiceNormal = new Indice("Unique Caché (Bloc)", msg, casesASurbriller, solutions, false);
                    }
                }
            }
        }
        return indiceNormal;
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