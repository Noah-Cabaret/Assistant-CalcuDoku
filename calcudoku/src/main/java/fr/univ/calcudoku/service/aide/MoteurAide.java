package fr.univ.calcudoku.service.aide;

import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.service.aide.technique.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Moteur d'analyse pour trouver des indices et aide au joueur.
 * Applique différentes techniques d'analyse classées par difficulté.
 */
public class MoteurAide {

    /** Énumération définissant les niveaux de difficulté des techniques */
    public enum NiveauAide {
        FACILE, MOYEN, DIFFICILE
    }

    /** Map associant chaque niveau à sa liste de techniques d'aide */
    private Map<NiveauAide, List<TechniqueAide>> techniquesParNiveau;

    /**
     * Constructeur du moteur d'aide.
     * Initialise et classe les techniques d'analyse par niveau.
     */
    public MoteurAide() {
        // Utilisation d'une LinkedHashMap pour préserver l'ordre : Facile -> Moyen -> Difficile
        this.techniquesParNiveau = new LinkedHashMap<>();

        // --- NIVEAU FACILE ---
        List<TechniqueAide> faciles = new ArrayList<>();
        faciles.add(new TechniqueBlocDe1());
        faciles.add(new TechniqueDerniereCaseLigneCol());
        faciles.add(new TechniqueDernierChiffreGrille());
        faciles.add(new TechniqueDerniereCaseBloc());
        this.techniquesParNiveau.put(NiveauAide.FACILE, faciles);

        // --- NIVEAU MOYEN ---
        List<TechniqueAide> moyennes = new ArrayList<>();
        moyennes.add(new TechniquePlaceUniqueLigneColonne());
        moyennes.add(new TechniqueBlocUnique());
        moyennes.add(new TechniqueCandidatUnique());
        moyennes.add(new TechniqueChiffreIncontournable());
        moyennes.add(new TechniqueUniqueCache());
        this.techniquesParNiveau.put(NiveauAide.MOYEN, moyennes);

        // --- NIVEAU DIFFICILE ---
        List<TechniqueAide> difficiles = new ArrayList<>();
        difficiles.add(new TechniqueIntraBloc_1_3());
        difficiles.add(new TechniqueVerrouillageBloc());
        difficiles.add(new TechniqueResteDeGrilleType1());
        difficiles.add(new TechniqueResteDeGrilleType2());
        difficiles.add(new TechniquePairesIsolees());
        difficiles.add(new TechniquePairesCachees());
        this.techniquesParNiveau.put(NiveauAide.DIFFICILE, difficiles);
    }

    /**
     * Trouve tous les indices possibles pour une grille donnée, 
     * en récoltant tous les résultats possibles et en piochant aléatoirement 
     * un seul résultat par niveau d'aide.
     * @param grille la grille à analyser
     * @return la liste d'un indice maximum par niveau trouvé
     */
    public List<Indice> trouverLesAides(Grille grille) {
        List<Indice> aidesTrouvees = new ArrayList<>();
        
        for (Map.Entry<NiveauAide, List<TechniqueAide>> entry : techniquesParNiveau.entrySet()) {
            List<Indice> resultatsPourCeNiveau = new ArrayList<>();
            
            // On lance toutes les techniques du niveau et on récolte celles qui trouvent un résultat
            for (TechniqueAide technique : entry.getValue()) {
                Indice indice = technique.analyser(grille);
                if (indice != null) {
                    indice.setNiveauAide(entry.getKey().name());
                    resultatsPourCeNiveau.add(indice);
                }
            }
            
            // S'il y a des résultats, on les mélange et on garde uniquement le premier
            if (!resultatsPourCeNiveau.isEmpty()) {
                Collections.shuffle(resultatsPourCeNiveau);
                aidesTrouvees.add(resultatsPourCeNiveau.get(0));
            }
        }
        return aidesTrouvees;
    }

    /**
     * Trouve les indices possibles pour une grille donnée en utilisant 
     * uniquement les techniques d'un niveau de difficulté précis et
     * en en piochant un seul aléatoirement parmi les résultats.
     * @param grille la grille à analyser
     * @param niveau le niveau de difficulté souhaité
     * @return la liste des indices trouvés pour ce niveau (un seul au hasard)
     */
    public List<Indice> trouverLesAidesParNiveau(Grille grille, NiveauAide niveau) {
        List<Indice> aidesTrouvees = new ArrayList<>();
        List<TechniqueAide> techniquesCiblees = techniquesParNiveau.getOrDefault(niveau, new ArrayList<>());
        List<Indice> resultatsPourCeNiveau = new ArrayList<>();
        
        // On teste toutes les techniques du niveau ciblé
        for (TechniqueAide technique : techniquesCiblees) {
            Indice indice = technique.analyser(grille);
            if (indice != null) {
                indice.setNiveauAide(niveau.name());
                resultatsPourCeNiveau.add(indice);
            }
        }
        
        // Si on a obtenu des aides pour ce niveau, on les mélange pour en prendre une au hasard
        if (!resultatsPourCeNiveau.isEmpty()) {
            Collections.shuffle(resultatsPourCeNiveau);
            aidesTrouvees.add(resultatsPourCeNiveau.get(0));
        }
        
        return aidesTrouvees;
    }
}