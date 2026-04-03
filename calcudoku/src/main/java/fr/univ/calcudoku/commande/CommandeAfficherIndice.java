package fr.univ.calcudoku.commande;

import java.util.List;
import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.view.VueCase;
import fr.univ.calcudoku.view.VueGrille;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class CommandeAfficherIndice implements CommandeAide {
    private final Indice indice;
    private final Label labelMessageAide;
    private final VueGrille vueGrille;
    
    private int etapeActuelle = 1; 

    public CommandeAfficherIndice(Indice indice, Label labelMessageAide, VueGrille vueGrille){
        this.indice = indice;
        this.labelMessageAide = labelMessageAide;
        this.vueGrille = vueGrille;
    }

    private boolean possedeNiveau2() {
        return indice.getCasesASurbriller() != null && !indice.getCasesASurbriller().isEmpty();
    }

    @Override
    public void afficher() {
        labelMessageAide.setText("");

        String niveauTexte = (indice.getNiveauAide() != null) ? "Niveau " + indice.getNiveauAide() + " | " : "";
        
        List<String> messages = indice.getMessagesExplicatifs();
        String texteExplicatif = "";
        
        if (messages != null && !messages.isEmpty()) {
            int index = Math.min(etapeActuelle - 1, messages.size() - 1);
            texteExplicatif = messages.get(index);
        } else {
            texteExplicatif = indice.getMessageExplicatif(); 
        }

        Text texteBase = new Text("[" + niveauTexte + indice.getNomTechnique() + "] \n" + texteExplicatif);
        texteBase.setFill(Color.web("#2c3e50"));
        texteBase.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        TextFlow textFlow = new TextFlow(texteBase);
        
        if (etapeActuelle >= 2 && possedeNiveau2()) {
            for (Case c : indice.getCasesASurbriller()) {
                VueCase vueCase = vueGrille.getGrilleVueCases(c.getX(), c.getY());
                vueCase.getStyleClass().remove("case-indice-surbrillance");
                if (!vueCase.getStyleClass().contains("case-aide-surbrillance")) {
                    vueCase.getStyleClass().add("case-aide-surbrillance");
                }
            }
            
            if (indice.aUneErreur()) {
                Text texteErreur = new Text("\n\nAttention, vous avez une erreur dans une case applicable à cette technique !");
                texteErreur.setFill(Color.RED); 
                texteErreur.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
                textFlow.getChildren().add(texteErreur);
            }
        }

        labelMessageAide.setGraphic(textFlow);
    }

    @Override
    public void masquer() {
        labelMessageAide.setText("");
        labelMessageAide.setGraphic(null);
        if (possedeNiveau2()) {
            for (Case c : indice.getCasesASurbriller()) {
                VueCase vueCase = vueGrille.getGrilleVueCases(c.getX(), c.getY());
                vueCase.getStyleClass().remove("case-aide-surbrillance");
                vueCase.getStyleClass().remove("case-indice-surbrillance"); 
            }
        }
    }

    @Override
    public void ameliorerNiveau() {
        if (peutEtreAmeliore()) {
            etapeActuelle++;
            afficher(); 
        }
    }

    @Override
    public boolean peutEtreAmeliore() {
        if (etapeActuelle == 1 && !possedeNiveau2()) return false; 
        // L'aide s'arrête obligatoirement à l'étape 2 (plus de solutions dévoilées)
        return etapeActuelle < 2;
    }
}