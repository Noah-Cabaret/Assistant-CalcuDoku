package fr.univ.calcudoku.service.aide;

import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.service.aide.MoteurAide.NiveauAide;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.List;

/**
 * Service JavaFX pour analyser la grille et trouver des indices de manière asynchrone.
 * Évite de bloquer l'interface utilisateur pendant l'analyse.
 */
public class AideService extends Service<List<Indice>> {
    
    private final MoteurAide moteur;
    private Grille grilleCopie;
    private NiveauAide niveauCible;

    /**
     * Constructeur d'un service d'aide.
     * Initialise le moteur d'analyse.
     */
    public AideService() {
        this.moteur = new MoteurAide();
    }

    /**
     * Lance l'analyse asynchrone globale de la grille pour trouver tous les indices.
     * @param grille la grille à analyser
     */
    public void lancerAnalyse(Grille grille) {
        lancerAnalyseSpecifique(grille, null);
    }

    /**
     * Lance l'analyse asynchrone de la grille pour un niveau de difficulté précis.
     * @param grille la grille à analyser
     * @param niveau le niveau de difficulté des indices souhaités
     */
    public void lancerAnalyseSpecifique(Grille grille, NiveauAide niveau) {
        if (grille == null) return;

        this.grilleCopie = new Grille(grille);
        this.niveauCible = niveau;
        this.restart();
    }

    /**
     * Crée la tâche d'analyse exécutée en arrière-plan.
     * @return la tâche retournant la liste des indices trouvés
     */
    @Override
    protected Task<List<Indice>> createTask() {
        return new Task<>() {
            @Override
            protected List<Indice> call() throws Exception {
                if (grilleCopie == null) {
                    return null;
                }
                
                if (niveauCible != null) {
                    return moteur.trouverLesAidesParNiveau(grilleCopie, niveauCible);
                } else {
                    return moteur.trouverLesAides(grilleCopie);
                }
            }
        };
    }
}