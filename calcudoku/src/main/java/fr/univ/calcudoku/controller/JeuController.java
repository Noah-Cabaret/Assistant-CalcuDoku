package fr.univ.calcudoku.controller;

import java.io.IOException;
import java.util.List;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.view.VueCase;
import fr.univ.calcudoku.view.VueGrille;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.control.Label;

public class JeuController {

    @FXML private StackPane conteneurGrille;
    @FXML private HBox conteneurBoutonsNombres;
    
    @FXML private Button btnUndo;
    @FXML private Button btnRedo;
    @FXML private Button btnAnnoter;
    @FXML private Button btnEffacer;
    @FXML private Button btnCalculatrice;
    @FXML private Label labelChrono;
    @FXML private GridPane gridPaneVisual;

    private Grille grilleModele;
    private VueGrille vueGrille;
    private boolean modeAnnotationActif = false;
    private javafx.stage.Popup calcPopup;
    private javafx.stage.Popup aidePopup;
    private VBox conteneurAide;

    private VueCase vueCaseSelectionnee = null;
    private Case caseModeleSelectionnee = null;
    private double xOffset = 0;
    private double yOffset = 0;
        
    private Timeline timeline;
    private int secondesEcoulees = 0;

    public void initialiserPartie(Grille grille) {
        this.grilleModele = grille;
        
        this.vueGrille = new VueGrille(grille);
        vueGrille.rafraichirToutesLesBordures();
        conteneurGrille.getChildren().add(vueGrille);

        NumberBinding tailleCarree = Bindings.min(conteneurGrille.widthProperty(), conteneurGrille.heightProperty());
        vueGrille.prefWidthProperty().bind(tailleCarree);
        vueGrille.prefHeightProperty().bind(tailleCarree);
        vueGrille.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        for (int y = 0; y < grille.getTaille(); y++) {
            for (int x = 0; x < grille.getTaille(); x++) {
                VueCase vc = vueGrille.getGrilleVueCases(x, y);
                final Case modeleCase = grille.getCase(x, y);
                
                vc.setOnMouseClicked(event -> selectionnerCase(vc, modeleCase));

                vc.setOnMouseEntered(event->{
                    GroupementCases g = modeleCase.getGroupement();
                    if (g != null) {
                        g.calculerPossibilites(grille.getTaille());
                        mettreAJourAide(g, event.getScreenX(), event.getScreenY());
                    }
                }); 
                vc.setOnMouseExited(event -> {
                    if (aidePopup != null) {
                        aidePopup.hide();
                    }
                });
            }
        }

        genererBoutonsNombres(grille.getTaille());
        demarrerChrono();
    }

    private void genererBoutonsNombres(int taille) {
        conteneurBoutonsNombres.getChildren().clear(); 
        
        for (int i = 1; i <= taille; i++) {
            Button btnChiffre = new Button(String.valueOf(i));
            
            btnChiffre.setMinSize(55, 55); 
            btnChiffre.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

            final int valeur = i; 
            btnChiffre.setOnAction(e -> actionChiffreClique(valeur));
            
            conteneurBoutonsNombres.getChildren().add(btnChiffre);
        }
    }

    private void demarrerChrono() {
        secondesEcoulees = 0; 
        
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            secondesEcoulees++;
            
            int minutes = secondesEcoulees / 60;
            int secondes = secondesEcoulees % 60;
            
            labelChrono.setText(String.format("%02d:%02d", minutes, secondes));
        }));
        
        timeline.setCycleCount(Timeline.INDEFINITE);
        
        timeline.play();
    }

    private void selectionnerCase(VueCase vueCase, Case modeleCase) {
        if (vueCaseSelectionnee != null) {
            vueCaseSelectionnee.getStyleClass().remove("case-selectionnee");
        }

        this.vueCaseSelectionnee = vueCase;
        this.caseModeleSelectionnee = modeleCase;

        vueCaseSelectionnee.getStyleClass().add("case-selectionnee");
    }

    
    private void actionChiffreClique(int valeur) {
        if (caseModeleSelectionnee != null) {
            if (modeAnnotationActif) {
                caseModeleSelectionnee.basculerNote(valeur); 
            } else {
                caseModeleSelectionnee.setValeur(valeur);   
                
                if (grilleModele.estGagnee()) {
                    System.out.println("VICTOIRE ! La grille est complétée correctement !");
                }
            }
        }
    }

    @FXML
    void actionBasculeAnnotation(ActionEvent event) {
        modeAnnotationActif = !modeAnnotationActif;
        if(modeAnnotationActif) {
            btnAnnoter.setStyle("-fx-background-color: #bbdefb;"); 
        } else {
            btnAnnoter.setStyle(""); 
        }
    }

    @FXML
    void actionEffacer(ActionEvent event) {
        if (caseModeleSelectionnee != null) {
            caseModeleSelectionnee.setValeur(0);
            caseModeleSelectionnee.effacerNotes();
        }
    }

    @FXML
    void actionUndo(ActionEvent event) {
        System.out.println("Undo cliqué");
    }

    @FXML
    void actionRedo(ActionEvent event) {
        System.out.println("Redo cliqué");
    }

    @FXML
    void actionCalculatrice(ActionEvent event) {
        System.out.println("Calculatrice cliquée");
    try {
        if (this.calcPopup != null && this.calcPopup.isShowing()) {
            this.calcPopup.hide();
            return; 
        }

        if (this.calcPopup == null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/VueCalculatrice.fxml"));
            Parent root = loader.load();
            
            this.calcPopup = new javafx.stage.Popup();
            this.calcPopup.getContent().add(root);

            this.calcPopup.setAutoHide(false); // Reste affichée quand on clique sur la grille
            root.setMouseTransparent(false); // Permet de cliquer sur les boutons de la calculette

            root.setOnMousePressed(e -> {
                xOffset = e.getSceneX();
                yOffset = e.getSceneY();
            });
            root.setOnMouseDragged(e -> {
                this.calcPopup.setX(e.getScreenX() - xOffset);
                this.calcPopup.setY(e.getScreenY() - yOffset);
            });

            this.calcPopup.setX(50); 
            this.calcPopup.setY(200);
        }
        Stage mainStage = (Stage) ((Button)event.getSource()).getScene().getWindow();
        this.calcPopup.show(mainStage);

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    
private void mettreAJourAide(GroupementCases groupement, double x, double y) {
    if (groupement == null) return;

    if (this.aidePopup == null) {
        this.aidePopup = new javafx.stage.Popup();
        this.conteneurAide = new VBox(12);
        this.conteneurAide.getStyleClass().add("calc-main-window");
        this.conteneurAide.setPadding(new Insets(15));
        this.conteneurAide.setAlignment(Pos.TOP_CENTER);
        this.conteneurAide.setPrefWidth(220);
        
        this.conteneurAide.setMouseTransparent(true); 
        
        this.aidePopup.getContent().add(conteneurAide);
    }

    conteneurAide.getChildren().clear();
    Label header = new Label(groupement.getResultatCible() + " " + groupement.getOperation());
    header.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: #333;");
    
    VBox liste = new VBox(8);
    liste.setAlignment(Pos.CENTER);

    for (List<Integer> comb : groupement.getCombinaisonsMaths()) {
        String texte = comb.toString().replace("[", "").replace("]", "");
        Label lbl = new Label(texte);
        lbl.getStyleClass().add("btn-light");
        lbl.setPrefWidth(180);
        lbl.setAlignment(Pos.CENTER);
        liste.getChildren().add(lbl);
    }

    conteneurAide.getChildren().addAll(header, liste);

    if (!aidePopup.isShowing()) {
        aidePopup.show(conteneurGrille.getScene().getWindow());
    }
    
    this.aidePopup.setX(x + 25);
    this.aidePopup.setY(y + 25);
}
    
}