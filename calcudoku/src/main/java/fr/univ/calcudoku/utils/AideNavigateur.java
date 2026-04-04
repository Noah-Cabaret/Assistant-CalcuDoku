package fr.univ.calcudoku.utils;

import fr.univ.calcudoku.commande.CommandeAide;
import fr.univ.calcudoku.commande.CommandeAfficherIndice;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.view.VueGrille;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;

public class AideNavigateur {

    private final List<CommandeAide> listeAides = new ArrayList<>();
    private List<Indice> indicesEnAttente = new ArrayList<>();
    private int indexAideActuelle = 0;

    public void mettreAJourIndices(List<Indice> indices) {
        this.indicesEnAttente = (indices != null) ? indices : new ArrayList<>();
    }

    public List<Indice> getIndicesEnAttente() {
        return indicesEnAttente;
    }

    public int getIndexAideActuelle() {
        return indexAideActuelle;
    }

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

    public void fermer() {
        if (!listeAides.isEmpty() && indexAideActuelle < listeAides.size()) {
            listeAides.get(indexAideActuelle).masquer();
        }
    }

    public void ameliorer(Button btnAmeliorer, Button btnPrecedente, Button btnSuivante) {
        if (!listeAides.isEmpty()) {
            listeAides.get(indexAideActuelle).ameliorerNiveau();
            mettreAJourBoutons(btnAmeliorer, btnPrecedente, btnSuivante);
        }
    }

    public void suivant(Button btnAmeliorer, Button btnPrecedente, Button btnSuivante) {
        if (indexAideActuelle < listeAides.size() - 1) {
            listeAides.get(indexAideActuelle).masquer();
            indexAideActuelle++;
            listeAides.get(indexAideActuelle).afficher();
            mettreAJourBoutons(btnAmeliorer, btnPrecedente, btnSuivante);
        }
    }

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
