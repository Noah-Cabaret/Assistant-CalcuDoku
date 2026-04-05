package fr.univ.calcudoku.utils;

import fr.univ.calcudoku.commande.CommandeAide;
import fr.univ.calcudoku.commande.CommandeAfficherIndice;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.view.VueGrille;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;

/**
 * Gère la navigation entre les indices d'aide (précédent / suivant / améliorer).
 * Coordonne l'affichage des commandes d'aide dans l'interface.
 */
public class AideNavigateur {

    private final List<CommandeAide> listeAides = new ArrayList<>();
    private List<Indice> indicesEnAttente = new ArrayList<>();
    private int indexAideActuelle = 0;

    /**
     * Met à jour la liste des indices disponibles.
     * @param indices les indices trouvés par le moteur d'aide
     */
    public void mettreAJourIndices(List<Indice> indices) {
        this.indicesEnAttente = (indices != null) ? indices : new ArrayList<>();
    }

    public List<Indice> getIndicesEnAttente() {
        return indicesEnAttente;
    }

    public int getIndexAideActuelle() {
        return indexAideActuelle;
    }

    /**
     * Rafraîchit l'affichage des aides avec les indices en attente.
     * @param labelMessage le label où afficher le message de l'aide
     * @param vueGrille la vue de la grille pour le surlignage
     * @param btnAmeliorer bouton "Améliorer"
     * @param btnPrecedente bouton "Précédent"
     * @param btnSuivante bouton "Suivant"
     */
    public void rafraichirContenu(Label labelMessage, VueGrille vueGrille,
            Button btnAmeliorer, Button btnPrecedente, Button btnSuivante) {
        if (!listeAides.isEmpty() && indexAideActuelle < listeAides.size()) {
            listeAides.get(indexAideActuelle).masquer();
        }
        listeAides.clear();
        indexAideActuelle = 0;

        for (Indice ind : indicesEnAttente) {
            listeAides.add(new CommandeAfficherIndice(ind, labelMessage, vueGrille));
        }

        if (!listeAides.isEmpty()) {
            listeAides.get(indexAideActuelle).afficher();
            mettreAJourBoutons(btnAmeliorer, btnPrecedente, btnSuivante);
        } else {
            if (labelMessage != null) labelMessage.setText("Aucune technique trouvée pour le moment.");
            if (btnAmeliorer != null) btnAmeliorer.setDisable(true);
            if (btnPrecedente != null) btnPrecedente.setDisable(true);
            if (btnSuivante != null) btnSuivante.setDisable(true);
        }
    }

    /** Masque l'aide actuellement affichée. */
    public void fermer() {
        if (!listeAides.isEmpty() && indexAideActuelle < listeAides.size()) {
            listeAides.get(indexAideActuelle).masquer();
        }
    }

    /** Améliore le niveau de détail de l'aide courante. */
    public void ameliorer(Button btnAmeliorer, Button btnPrecedente, Button btnSuivante) {
        if (!listeAides.isEmpty()) {
            listeAides.get(indexAideActuelle).ameliorerNiveau();
            mettreAJourBoutons(btnAmeliorer, btnPrecedente, btnSuivante);
        }
    }

    /** Passe à l'aide suivante. */
    public void suivant(Button btnAmeliorer, Button btnPrecedente, Button btnSuivante) {
        if (indexAideActuelle < listeAides.size() - 1) {
            listeAides.get(indexAideActuelle).masquer();
            indexAideActuelle++;
            listeAides.get(indexAideActuelle).afficher();
            mettreAJourBoutons(btnAmeliorer, btnPrecedente, btnSuivante);
        }
    }

    /** Revient à l'aide précédente. */
    public void precedent(Button btnAmeliorer, Button btnPrecedente, Button btnSuivante) {
        if (indexAideActuelle > 0) {
            listeAides.get(indexAideActuelle).masquer();
            indexAideActuelle--;
            listeAides.get(indexAideActuelle).afficher();
            mettreAJourBoutons(btnAmeliorer, btnPrecedente, btnSuivante);
        }
    }

    private void mettreAJourBoutons(Button btnAmeliorer, Button btnPrecedente, Button btnSuivante) {
        if (listeAides.isEmpty()) return;
        if (btnPrecedente != null) btnPrecedente.setDisable(indexAideActuelle == 0);
        if (btnSuivante != null) btnSuivante.setDisable(indexAideActuelle == listeAides.size() - 1);
        if (btnAmeliorer != null) btnAmeliorer.setDisable(!listeAides.get(indexAideActuelle).peutEtreAmeliore());
    }
}
