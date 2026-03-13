package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.commande.CommandeAide;
import fr.univ.calcudoku.commande.CommandeAfficherIndice;
import fr.univ.calcudoku.service.aide.AideService;
import fr.univ.calcudoku.view.VueCase;
import fr.univ.calcudoku.view.VueGrille;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import fr.univ.calcudoku.MainApp;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JeuController {

    @FXML private StackPane conteneurGrille;
    @FXML private HBox conteneurBoutonsNombres;
    
    @FXML private Button btnUndo;
    @FXML private Button btnRedo;
    @FXML private Button btnAnnoter;
    @FXML private Button btnEffacer;
    @FXML private Button btnCalculatrice;

    @FXML private VBox bulleAide;
    @FXML private Label labelMessageAide;
    @FXML private Button btnAmeliorerAide;
    @FXML private Button btnAidePrecedente;
    @FXML private Button btnAideSuivante;

    private Grille grilleModele;
    private VueGrille vueGrille;
    private boolean modeAnnotationActif = false;

    private VueCase vueCaseSelectionnee = null;
    private Case caseModeleSelectionnee = null;

    private final AideService aideService = new AideService();
    private List<CommandeAide> listeAides = new ArrayList<>();
    private int indexAideActuelle = 0;

    public void initialiserPartie(Grille grille) {
        this.grilleModele = grille;
        this.vueGrille = new VueGrille(grille);
        
        conteneurGrille.getChildren().clear(); 
        conteneurGrille.getChildren().add(vueGrille);
        NumberBinding tailleCarree = Bindings.min(conteneurGrille.widthProperty(), conteneurGrille.heightProperty());
        
        vueGrille.prefWidthProperty().bind(tailleCarree);
        vueGrille.prefHeightProperty().bind(tailleCarree);
        
        vueGrille.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        vueGrille.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        for (int y = 0; y < grille.getTaille(); y++) {
            for (int x = 0; x < grille.getTaille(); x++) {
                VueCase vc = vueGrille.getGrilleVueCases(x, y);
                final Case modeleCase = grille.getCase(x, y);
                vc.setOnMouseClicked(event -> selectionnerCase(vc, modeleCase));
            }
        }
        genererBoutonsNombres(grille.getTaille());

        bulleAide.setVisible(false);

        aideService.setOnSucceeded(event -> {
            List<Indice> indicesTrouves = aideService.getValue();
            
            if (!listeAides.isEmpty()) {
                listeAides.get(indexAideActuelle).masquer();
            }

            listeAides.clear();
            indexAideActuelle = 0;

            if (indicesTrouves != null) {
                for (Indice ind : indicesTrouves) {
                    listeAides.add(new CommandeAfficherIndice(ind, labelMessageAide, vueGrille));
                }
            }
            
            mettreAJourBoutonsNavigation();
            
            if (bulleAide.isVisible() && !listeAides.isEmpty()) {
                listeAides.get(indexAideActuelle).afficher();
            }
        });

        aideService.lancerAnalyse(grilleModele);
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

    public void sauvegarderImageGrille(String nomFichier) {
        try {
            String nomJoueur = MainApp.getProfileManager().getProfilActif();
            if (nomJoueur == null) nomJoueur = "Invité";

            File dossierImages = new File("profils/" + nomJoueur + "/jeu/images");
            if (!dossierImages.exists()) {
                dossierImages.mkdirs();
            }

            String nomImage = nomFichier;
            if (nomImage.endsWith(".json")) {
                nomImage = nomImage.replace(".json", ".png");
            } else if (!nomImage.endsWith(".png")) {
                nomImage += ".png";
            }
            
            File fichierFinal = new File(dossierImages, nomImage);

            if (vueCaseSelectionnee != null) {
                vueCaseSelectionnee.getStyleClass().remove("case-selectionnee");
            }
            
            vueGrille.setStyle("-fx-background-color: white;");

            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.WHITE); 
            WritableImage image = vueGrille.snapshot(params, null);

            if (vueCaseSelectionnee != null) {
                vueCaseSelectionnee.getStyleClass().add("case-selectionnee");
            }

            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", fichierFinal);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                aideService.lancerAnalyse(grilleModele);
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
            aideService.lancerAnalyse(grilleModele);
        }
    }

    @FXML
    void actionUndo(ActionEvent event) {
    }

    @FXML
    void actionRedo(ActionEvent event) {
    }

    @FXML
    void actionCalculatrice(ActionEvent event) {
        sauvegarderImageGrille("1.png");
    }

    @FXML
    public void actionBoutonAidePointInterrogation() {
        // 👇 --- DÉBUT DU BLOC DE TEST FICTIF --- 👇
        if (listeAides.isEmpty() && grilleModele != null) {
            
            // --- AIDE TEST N°1 ---
            Case case1 = grilleModele.getCase(0, 0);
            Case case2 = grilleModele.getCase(1, 0);

            java.util.List<Case> listeCasesTest1 = new java.util.ArrayList<>();
            listeCasesTest1.add(case1);
            listeCasesTest1.add(case2);

            java.util.Map<Case, Integer> mapSolutionsTest1 = new java.util.HashMap<>();
            mapSolutionsTest1.put(case1, 4);

            Indice indiceTest1 = new Indice(
                "Technique Visuelle (Test 1)", 
                "Ceci est la PREMIÈRE fausse aide. Observe les deux cases en haut à gauche.", 
                listeCasesTest1, 
                mapSolutionsTest1
            );
            listeAides.add(new CommandeAfficherIndice(indiceTest1, labelMessageAide, vueGrille));


            // --- AIDE TEST N°2 ---
            // On vérifie que la grille est assez grande pour ne pas crasher
            if (grilleModele.getTaille() > 2) {
                Case case3 = grilleModele.getCase(2, 2); // Une case au milieu
                
                java.util.List<Case> listeCasesTest2 = new java.util.ArrayList<>();
                listeCasesTest2.add(case3); // Niveau 2 : Surbrillance
                
                java.util.Map<Case, Integer> mapSolutionsTest2 = new java.util.HashMap<>();
                mapSolutionsTest2.put(case3, 2); // Niveau 3 : Solution

                Indice indiceTest2 = new Indice(
                    "Déduction Logique (Test 2)", 
                    "Ceci est la DEUXIÈME fausse aide ! Tu as réussi à naviguer jusqu'ici.", 
                    listeCasesTest2, 
                    mapSolutionsTest2
                );
                listeAides.add(new CommandeAfficherIndice(indiceTest2, labelMessageAide, vueGrille));
            }
        }
        // 👆 --- FIN DU BLOC DE TEST FICTIF --- 👆


        if (listeAides.isEmpty()) {
            System.out.println("Aucune aide disponible.");
            return;
        }
        
        bulleAide.setVisible(true);
        listeAides.get(indexAideActuelle).afficher();
        mettreAJourBoutonsNavigation();
    }

    @FXML
    public void actionFermerBulleAide() {
        if (!listeAides.isEmpty()) {
            listeAides.get(indexAideActuelle).masquer();
        }
        bulleAide.setVisible(false);
    }

    @FXML
    public void actionAmeliorerAide() {
        if (!listeAides.isEmpty()) {
            listeAides.get(indexAideActuelle).ameliorerNiveau();
            mettreAJourBoutonsNavigation();
        }
    }

    @FXML
    public void actionAideSuivante() {
        if (indexAideActuelle < listeAides.size() - 1) {
            listeAides.get(indexAideActuelle).masquer();
            indexAideActuelle++;
            listeAides.get(indexAideActuelle).afficher();
            mettreAJourBoutonsNavigation();
        }
    }

    @FXML
    public void actionAidePrecedente() {
        if (indexAideActuelle > 0) {
            listeAides.get(indexAideActuelle).masquer();
            indexAideActuelle--;
            listeAides.get(indexAideActuelle).afficher();
            mettreAJourBoutonsNavigation();
        }
    }

    private void mettreAJourBoutonsNavigation() {
        if (listeAides.isEmpty()) return;

        btnAidePrecedente.setDisable(indexAideActuelle == 0);
        btnAideSuivante.setDisable(indexAideActuelle == listeAides.size() - 1);
        btnAmeliorerAide.setDisable(!listeAides.get(indexAideActuelle).peutEtreAmeliore());
    }
}