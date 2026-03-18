package fr.univ.calcudoku.service.aide;

import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.service.aide.technique.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Moteur d'analyse pour trouver des indices et aide au joueur.
 * Applique différentes techniques d'analyse pour résoudre des étapes du puzzle.
 */
public class MoteurAide {
    /** Liste des techniques d'aide disponibles */
    private List<TechniqueAide> techniques;

    /**
     * Constructeur du moteur d'aide.
     * Initialise la liste des techniques d'analyse.
     */
    public MoteurAide() {
        this.techniques = new ArrayList<>();
        this.techniques.add(new TechniqueBlocUnique());
        this.techniques.add(new TechniqueBlocDe1());
        this.techniques.add(new TechniqueDerniereCaseLigneCol());
        this.techniques.add(new TechniqueDernierChiffreGrille());
        this.techniques.add(new TechniqueDerniereCaseBloc());
        this.techniques.add(new TechniquePlaceUniqueLigneColonne());

        this.techniques.add(new TechniqueCandidatUnique());
        this.techniques.add(new TechniqueUniqueCache());  
        this.techniques.add(new TechniqueIntraBloc_1_3());
        
        this.techniques.add(new TechniqueResteDeGrilleType1());
        this.techniques.add(new TechniqueResteDeGrilleType2());
      
        // ajout autres techniques
    }

    /**
     * Trouve tous les indices possibles pour une grille donnée.
     * Applique chaque technique d'analyse disponible.
     * 
     * @param grille la grille à analyser
     * @return la liste des indices trouvés
     */
    public List<Indice> trouverLesAides(Grille grille) {
        List<Indice> aidesTrouvees = new ArrayList<>();
        for (TechniqueAide technique : techniques) {
            /**
             * Analyse la grille avec la technique courante
             */
            Indice indice = technique.analyser(grille);

            if (indice != null) {
                aidesTrouvees.add(indice);
            }
        }
        return aidesTrouvees;
    }
}
