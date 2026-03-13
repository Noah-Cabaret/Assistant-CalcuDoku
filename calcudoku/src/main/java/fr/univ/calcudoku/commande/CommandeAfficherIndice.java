package fr.univ.calcudoku.commande;

import java.util.Map;
import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.view.VueCase;
import fr.univ.calcudoku.view.VueGrille;
import javafx.scene.control.Label;

/**
 * Implémentation de la commande d'affichage d'indice.
 * Gère les 3 niveaux d'aide progressifs : texte, surbrillance, et solutions.
 */
public class CommandeAfficherIndice implements CommandeAide {
    /** L'indice à afficher */
    private final Indice indice;
    /** Label pour afficher le texte de l'indice */
    private final Label labelMessageAide;
    /** Grille visuelle pour surbriller les cases */
    private final VueGrille vueGrille;
    
    /** Étape actuelle : 1=texte, 2=surbrillance, 3=solutions */
    private int etapeActuelle = 1; 

    /**
     * Constructeur d'une commande d'indice.
     * @param indice l'indice à afficher
     * @param labelMessageAide le label pour le message
     * @param vueGrille la grille visuelle
     */
    public CommandeAfficherIndice(Indice indice, Label labelMessageAide, VueGrille vueGrille){
        this.indice = indice;
        this.labelMessageAide = labelMessageAide;
        this.vueGrille = vueGrille;
    }

    /**
     * Vérifie si ce groupement possède un niveau 2 d'aide (surbrillance).
     * @return true si des cases peuvent être surbrillees
     */
    private boolean possedeNiveau2() {
        return indice.getCasesASurbriller() != null && !indice.getCasesASurbriller().isEmpty();
    }

    /**
     * Vérifie si ce groupement possède un niveau 3 d'aide (solutions).
     * @return true si des solutions sont disponibles
     */
    private boolean possedeNiveau3() {
        return indice.getSolutions() != null && !indice.getSolutions().isEmpty();
    }

    @Override
    /**
     * Affiche l'indice au niveau d'aide actuellement déverrouillé.
     */
    public void afficher() {
        // 1. On affiche TOUJOURS le texte du Niveau 1
        String texteComplet = "[" + indice.getNomTechnique() + "] \n" + indice.getMessageExplicatif();
        
        // 2. Si on a débloqué le Niveau 2, on met en surbrillance
        if (etapeActuelle >= 2 && possedeNiveau2()) {
            for (Case c : indice.getCasesASurbriller()) {
                VueCase vueCase = vueGrille.getGrilleVueCases(c.getX(), c.getY());
                if (!vueCase.getStyleClass().contains("case-indice-surbrillance")) {
                    vueCase.getStyleClass().add("case-indice-surbrillance");
                }
            }
        }

        // 3. Si on a débloqué le Niveau 3, on ajoute la solution au texte
        if (etapeActuelle >= 3 && possedeNiveau3()) {
            texteComplet += "\n\nSolution : ";
            for (Map.Entry<Case, Integer> reponse : indice.getSolutions().entrySet()) {
                Case c = reponse.getKey();
                texteComplet += "Saisissez " + reponse.getValue() + " dans la case (" + (c.getX() + 1) + "," + (c.getY() + 1) + "). ";
            }
        }

        labelMessageAide.setText(texteComplet);
    }

    @Override
    /**
     * Masque l'indice actuellement affiché.
     */
    public void masquer() {
        labelMessageAide.setText("");
        if (possedeNiveau2()) {
            for (Case c : indice.getCasesASurbriller()) {
                VueCase vueCase = vueGrille.getGrilleVueCases(c.getX(), c.getY());
                vueCase.getStyleClass().remove("case-indice-surbrillance");
            }
        }
    }

    @Override
    /**
     * Améliore le niveau d'aide au niveau suivant s'il est disponible.
     */
    public void ameliorerNiveau() {
        if (peutEtreAmeliore()) {
            etapeActuelle++;
            afficher(); // Met à jour l'écran avec le nouveau niveau
        }
    }

    @Override
    /**
     * Vérifie si l'aide peut être améliorée vers le niveau suivant.
     * @return true s'il reste un niveau d'aide disponible
     */
    public boolean peutEtreAmeliore() {
        // Bloqué au niv 1 si pas de niv 2
        if (etapeActuelle == 1 && !possedeNiveau2()) return false; 
        // Bloqué au niv 2 si pas de niv 3
        if (etapeActuelle == 2 && !possedeNiveau3()) return false; 
        
        return etapeActuelle < 3;
    }
}