package fr.univ.calcudoku.service.aide;

import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.Indice;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.List;

/**
 * Service JavaFX pour analyser la grille et trouver des indices de manière asynchrone.
 * Évite de bloquer l'interface utilisateur pendant l'analyse.
 */
public class AideService extends Service<List<Indice>>{
    /** Moteur d'analyse pour les indices */
    private final MoteurAide moteur;
    /** Copie de la grille à analyser */
    private Grille grilleCopie;

    /**
     * Constructeur d'un service d'aide.
     * Initialise le moteur d'analyse.
     */
    public AideService() {
        this.moteur = new MoteurAide();
    }

    /**
     * Lance l'analyse asynchrone de la grille pour trouver des indices.
     * @param grille la grille à analyser
     */
    public void lancerAnalyse(Grille grille){
        if(grille == null) return;

        grilleCopie = new Grille(grille);
        this.restart();
    }

    @Override
    /**
     * Crée la tâche d'analyse exécutée en arrière-plan.
     * @return la tâche retournant la liste des indices trouvés
     */
    protected Task<List<Indice>> createTask(){
        return new Task<>() {
            @Override
            protected List<Indice> call() throws Exception{
                if(grilleCopie == null){return null;}
                return moteur.trouverLesAides(grilleCopie);
            }
        };
    }
}
