package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.commande.CommandeAide;
import fr.univ.calcudoku.commande.CommandeAfficherIndice;
import fr.univ.calcudoku.service.aide.AideService;
import fr.univ.calcudoku.view.VueCase;
import fr.univ.calcudoku.view.VueGrille;
import fr.univ.calcudoku.utils.JeuUtilitaires;
import fr.univ.calcudoku.save.Sauvegarde;
import fr.univ.calcudoku.MainApp;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JeuController {

    @FXML private StackPane conteneurGrille, popupAbandon;
    @FXML private HBox conteneurBoutonsNombres, conteneurBoutonsHypothese;
    @FXML private Button btnUndo, btnRedo, btnAnnoter, btnEffacer, btnCalculatrice, btnHypothese, btnRetour, btnMenu;
    @FXML private VBox boiteCombinaisons, bulleAide, menuDeroulant;
    @FXML private Label labelChrono, labelCombinaisons, labelMessageAide;
    @FXML private RadioButton radioCombinaisons, radioCalculatrice;
    @FXML private ToggleGroup groupeConfigAide;
    @FXML private Button btnAmeliorerAide, btnAidePrecedente, btnAideSuivante, btnActualiserAide;
    @FXML private Button btnValider, btnAide, btnValiderHypothese, btnAnnulerHypothese; // Ajout des boutons ronds manquants

    private boolean modeHypotheseActif = false, modeAnnotationActif = false;
    private Timeline timeline;
    private int secondesEcoulees = 0;

    private Grille grilleModele;
    private VueGrille vueGrille;
    private VueCase vueCaseSelectionnee = null;
    private Case caseModeleSelectionnee = null;
    private Sauvegarde save;

    private final AideService aideService = new AideService();
    private List<CommandeAide> listeAides = new ArrayList<>();
    private List<Indice> indicesEnAttente = new ArrayList<>();
    private int indexAideActuelle = 0;

    private EventHandler<KeyEvent> filtreClavier;

    public static String pagePrecedente = "/fxml/menu.fxml";

    public void initialiserPartie(Grille grille, Sauvegarde save) {
        this.grilleModele = grille;
        this.save = save;

        if (this.save != null && (this.save.getIdGrille() == null || this.save.getIdGrille().isEmpty())) {
            this.save.setIdGrille("libre_" + grille.getTaille() + "_1_1"); 
        }

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
        appliquerModeSombre(); // <-- On applique la couleur tout de suite !
        
        if (popupAbandon != null) popupAbandon.setVisible(false);
        if (menuDeroulant != null) menuDeroulant.setVisible(false);
        bulleAide.setVisible(false);
        
        initialiserLogiqueMenu();
        aideService.setOnSucceeded(event -> indicesEnAttente = aideService.getValue());

        for (GroupementCases bloc : grilleModele.getListeGroupements()) {
            bloc.calculerPossibilites(grilleModele);
        }

        // Ajout du clavier et des sécurités via nos Utilitaires !
        Platform.runLater(() -> {
            Scene scene = conteneurGrille.getScene();
            if (scene != null) {
                configurerClavier(scene);
                JeuUtilitaires.installerSecuritesFermeture(scene, () -> forcerSauvegarde(true), () -> forcerSauvegarde(false));
            }
        });

        demarrerChrono();
        aideService.lancerAnalyse(grilleModele);
    }

    private void initialiserLogiqueMenu() {
        if (radioCombinaisons != null && groupeConfigAide != null) {
            boolean modeCombiInitial = radioCombinaisons.isSelected();
            boiteCombinaisons.setVisible(modeCombiInitial);
            boiteCombinaisons.setManaged(modeCombiInitial);
            btnCalculatrice.setDisable(modeCombiInitial);

            groupeConfigAide.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                boolean modeCombi = (newVal == radioCombinaisons);
                boiteCombinaisons.setVisible(modeCombi);
                boiteCombinaisons.setManaged(modeCombi);
                btnCalculatrice.setDisable(modeCombi);
                if (modeCombi) JeuUtilitaires.cacherCalculatrice();
            });
        }
    }

    private void forcerSauvegarde(boolean avecImage) {
        if (save != null && grilleModele != null) {
            if (timeline != null) timeline.stop();
            String profilActif = MainApp.getProfileManager().getProfilActif();
            if (profilActif == null) profilActif = "Invité";

            if (save.tmp != null) save.tmp.setTempsPrecedent(save.tmp.tempsTotal());
            save.enreg(profilActif, grilleModele);

            if (avecImage && Platform.isFxApplicationThread() && save.getIdGrille() != null) {
                JeuUtilitaires.sauvegarderImageGrille(grilleModele, vueGrille, vueCaseSelectionnee, save.getIdGrille() + ".png", this::actionFermerBulleAide);
            }
        }
    }

    // ========================================================
    // GESTION DU MODE SOMBRE
    // ========================================================

    private void appliquerModeSombre() {
        boolean sombre = MainApp.modeSombreActif;
        
        // 1. Appliquer le fichier CSS global à la page
        Platform.runLater(() -> {
            javafx.scene.Scene scene = conteneurGrille.getScene();
            if (scene != null) {
                String cssPath = getClass().getResource("/styles/sombre.css").toExternalForm();
                if (sombre && !scene.getStylesheets().contains(cssPath)) {
                    scene.getStylesheets().add(cssPath);
                } else if (!sombre) {
                    scene.getStylesheets().remove(cssPath);
                }
            }
            
            // On force tous les Labels à l'intérieur de la grille à rester noirs !
            if (vueGrille != null) {
                vueGrille.lookupAll(".label").forEach(noeud -> noeud.setStyle("-fx-text-fill: black;"));
            }
        });

        // 2. Définir les palettes de couleurs
        String couleurTexte = sombre ? "white" : "black";
        String couleurFond = sombre ? "#2b2b2b" : "white";
        String couleurBordure = sombre ? "#888888" : "black";
        
        // 3. Repeindre les conteneurs classiques
        labelChrono.setStyle("-fx-text-fill: " + couleurTexte + ";");
        labelCombinaisons.setStyle("-fx-text-fill: " + couleurTexte + ";");
        boiteCombinaisons.setStyle("-fx-background-color: " + couleurFond + "; -fx-border-color: " + couleurBordure + "; -fx-border-width: 1px; -fx-padding: 10px;");
        
        String couleurMenu = sombre ? "-fx-background-color: #333333; -fx-border-color: #555555; -fx-border-width: 1px; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-padding: 15px;" 
                                    : "-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-width: 1px; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-padding: 15px;";
        menuDeroulant.setStyle(couleurMenu);

        // On parcourt les éléments du menu pour adapter leurs couleurs
        for (javafx.scene.Node node : menuDeroulant.getChildren()) {
            if (node instanceof Button) {
                node.setStyle("-fx-background-color: " + couleurFond + "; -fx-text-fill: " + couleurTexte + "; -fx-border-color: " + couleurBordure + "; -fx-cursor: hand; -fx-padding: 8px; -fx-font-weight: bold;");
            } else if (node instanceof VBox) {
                node.setStyle("-fx-border-color: " + couleurBordure + "; -fx-border-width: 1 0 0 0; -fx-padding: 10 0 0 0;");
                for (javafx.scene.Node subNode : ((VBox) node).getChildren()) {
                    if (subNode instanceof Label) {
                        subNode.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + couleurTexte + ";");
                    } else if (subNode instanceof RadioButton) {
                        subNode.setStyle("-fx-text-fill: " + couleurTexte + "; -fx-cursor: hand;");
                    }
                }
            }
        }

        // 4. Repeindre les boutons ronds (Valider, Aide, Hypothèse)
        String styleRondBase = " -fx-border-width: 3px; -fx-border-radius: 50%; -fx-background-radius: 50%; -fx-cursor: hand; ";
        String couleurBoutonRond = sombre ? "-fx-background-color: #444444; -fx-border-color: #888888;" : "-fx-background-color: white; -fx-border-color: black;";
        
        if (btnValider != null) btnValider.setStyle(couleurBoutonRond + styleRondBase + "-fx-min-width: 60px; -fx-min-height: 60px;");
        if (btnAide != null) btnAide.setStyle(couleurBoutonRond + styleRondBase + "-fx-min-width: 60px; -fx-min-height: 60px;");
        if (btnHypothese != null) btnHypothese.setStyle(couleurBoutonRond + styleRondBase + "-fx-min-width: 60px; -fx-min-height: 60px; -fx-font-size: 38px; -fx-font-family: 'Times New Roman', serif; -fx-font-style: italic; -fx-text-fill: " + couleurTexte + "; -fx-padding: 0;");
        if (btnValiderHypothese != null) btnValiderHypothese.setStyle(couleurBoutonRond + styleRondBase + "-fx-min-width: 45px; -fx-min-height: 45px;");
        if (btnAnnulerHypothese != null) btnAnnulerHypothese.setStyle(couleurBoutonRond + styleRondBase + "-fx-min-width: 45px; -fx-min-height: 45px;");

        // 5. Repeindre TOUTES les icônes (FontIcons)
        Color iconColor = sombre ? Color.WHITE : Color.BLACK;
        Button[] boutonsAvecIcones = {btnRetour, btnMenu, btnValider, btnAide, btnValiderHypothese, btnAnnulerHypothese, btnUndo, btnRedo, btnAnnoter, btnEffacer, btnCalculatrice, btnActualiserAide};
        
        for (Button btn : boutonsAvecIcones) {
            if (btn != null && btn.getGraphic() instanceof FontIcon) {
                ((FontIcon) btn.getGraphic()).setIconColor(iconColor);
            }
        }

        // --->LA POPUP D'ABANDON <---
        if (popupAbandon != null && !popupAbandon.getChildren().isEmpty()) {
            // On récupère la boîte blanche de la popup
            VBox boitePopup = (VBox) popupAbandon.getChildren().get(0);
            boitePopup.setStyle("-fx-background-color: " + couleurFond + "; -fx-background-radius: 12px; -fx-padding: 20px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 15, 0, 0, 0);");
            
            // On parcourt les éléments dedans (Les textes et les boutons)
            for (javafx.scene.Node n : boitePopup.getChildren()) {
                if (n instanceof Label) {
                    Label lbl = (Label) n;
                    if (lbl.getText().contains("Abandonner")) {
                        lbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + couleurTexte + ";");
                    } else if (lbl.getText().contains("progression")) {
                        // On force le rouge pour qu'il reste visible
                        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #e57373;");
                    }
                } else if (n instanceof javafx.scene.layout.HBox) {
                    // C'est la boîte qui contient les boutons "Annuler" et "Quitter"
                    javafx.scene.layout.HBox boxBoutons = (javafx.scene.layout.HBox) n;
                    Button btnAnnuler = (Button) boxBoutons.getChildren().get(0);
                    // On adapte le bouton annuler au thème
                    String couleurBtnAnnuler = sombre ? "#444444" : "#f5f5f5";
                    btnAnnuler.setStyle("-fx-background-color: " + couleurBtnAnnuler + "; -fx-border-color: " + couleurBordure + "; -fx-text-fill: " + couleurTexte + "; -fx-cursor: hand; -fx-min-width: 90px;");
                }
            }
        }
    }

    // ========================================================
    // CHANGEMENTS DE SCENES ET SAUVEGARDES
    // ========================================================

    @FXML void actionRetourMenu(ActionEvent event) { 
        forcerSauvegarde(true); 
        changerScene("/fxml/menu.fxml");
    }

    @FXML void actionAbandonner(ActionEvent event) {
        if (menuDeroulant != null) menuDeroulant.setVisible(false);
        if (popupAbandon != null) popupAbandon.setVisible(true);
    }

    @FXML void actionAnnulerAbandon(ActionEvent event) {
        if (popupAbandon != null) popupAbandon.setVisible(false);
    }

    @FXML void actionConfirmerAbandon(ActionEvent event) {
        if (timeline != null) timeline.stop();
        if (save != null && save.hist != null) save.hist.viderQueue();
        if (save != null) save.effacer(MainApp.getProfileManager().getProfilActif());
        changerScene("/fxml/menu.fxml");
    }

    private void changerScene(String fxml) {
        if (conteneurGrille.getScene() != null) {
            JeuUtilitaires.desinstallerSecuritesFermeture(conteneurGrille.getScene());
        }
        JeuUtilitaires.cacherCalculatrice();
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML void actionRecommencer(ActionEvent event) {
        secondesEcoulees = 0; labelChrono.setText("00:00");
        if (save != null && save.tmp != null) { save.tmp.setTempsPrecedent(0.0); save.tmp.lancer(); }
        for (int y = 0; y < grilleModele.getTaille(); y++) {
            for (int x = 0; x < grilleModele.getTaille(); x++) {
                Case c = grilleModele.getCase(x, y);
                c.setValeur(0); c.effacerNotes();
                vueGrille.getGrilleVueCases(x, y).setEstHypothese(false);
            }
        }
        save.hist.viderQueue();
        quitterModeHypotheseVisuel();
        if (menuDeroulant != null) menuDeroulant.setVisible(false);
        aideService.lancerAnalyse(grilleModele);
        if (caseModeleSelectionnee != null) rafraichirZoneCombinaisons(caseModeleSelectionnee);
    }

    // ========================================================
    // GESTION DU CLAVIER ET INTERACTIONS GRILLE
    // ========================================================

    private void configurerClavier(Scene scene) {
        if (filtreClavier != null) scene.removeEventFilter(KeyEvent.KEY_PRESSED, filtreClavier);
        
        /* * Décommentez et ajustez si vous utilisez la classe ClavierHandler :
         * * filtreClavier = ClavierHandler.creerFiltre(
         * grilleModele, 
         * () -> caseModeleSelectionnee, 
         * (x, y) -> selectionnerCase(vueGrille.getGrilleVueCases(x, y), grilleModele.getCase(x, y)), 
         * this::actionChiffreClique, 
         * () -> actionEffacer(null)
         * );
         * scene.addEventFilter(KeyEvent.KEY_PRESSED, filtreClavier);
         */
    }

    private void selectionnerCase(VueCase vueCase, Case modeleCase) {
        if (vueCaseSelectionnee != null) vueCaseSelectionnee.getStyleClass().remove("case-selectionnee");
        this.vueCaseSelectionnee = vueCase;
        this.caseModeleSelectionnee = modeleCase;
        vueCaseSelectionnee.getStyleClass().add("case-selectionnee");
        rafraichirZoneCombinaisons(modeleCase);
    }

    private void actionChiffreClique(int valeur) {
        if (caseModeleSelectionnee != null) {
            if (modeAnnotationActif) {
                caseModeleSelectionnee.basculerNote(valeur); 
                if(caseModeleSelectionnee.getValeur() < 10)
                    save.hist.addEtape(caseModeleSelectionnee.getX(), caseModeleSelectionnee.getY(), valeur + 10 + (modeHypotheseActif ? 20 : 0));
            } else {
                save.hist.addEtape(caseModeleSelectionnee.getX(), caseModeleSelectionnee.getY(), valeur + (modeHypotheseActif ? 20 : 0));
                caseModeleSelectionnee.setValeur(valeur);   
                vueCaseSelectionnee.setEstHypothese(modeHypotheseActif);
                rafraichirZoneCombinaisons(caseModeleSelectionnee);
                if (bulleAide.isVisible()) { btnActualiserAide.setVisible(true); btnActualiserAide.setManaged(true); }
                aideService.lancerAnalyse(grilleModele);
            }
        }
    }

    @FXML void actionBasculeAnnotation(ActionEvent event) {
        modeAnnotationActif = !modeAnnotationActif;
        btnAnnoter.setStyle(modeAnnotationActif ? "-fx-background-color: #bbdefb;" : "");
    }

    @FXML void actionEffacer(ActionEvent event) {
        if (caseModeleSelectionnee != null) {
            caseModeleSelectionnee.setValeur(0);
            caseModeleSelectionnee.effacerNotes();
            if (bulleAide.isVisible()) { btnActualiserAide.setVisible(true); btnActualiserAide.setManaged(true); }
            aideService.lancerAnalyse(grilleModele);
            save.hist.addEtape(caseModeleSelectionnee.getX(), caseModeleSelectionnee.getY(), (modeHypotheseActif ? 20 : 0));
        }
    }

    // ========================================================
    // UNDO / REDO / HYPOTHESES (Délégués à Historique)
    // ========================================================

    @FXML void actionUndo(ActionEvent event) {
        save.hist.appliquerUndo(grilleModele, modeHypotheseActif);
        if (caseModeleSelectionnee != null) rafraichirZoneCombinaisons(caseModeleSelectionnee);
    }

    @FXML void actionRedo(ActionEvent event) {
        save.hist.appliquerRedo(grilleModele);
        if (caseModeleSelectionnee != null) rafraichirZoneCombinaisons(caseModeleSelectionnee);
    }

    @FXML void actionHypothese(ActionEvent event) {
        modeHypotheseActif = true;
        btnHypothese.setDisable(true); conteneurBoutonsHypothese.setVisible(true);
    }

    @FXML void actionValiderHypothese(ActionEvent event) {
        quitterModeHypotheseVisuel();
        save.hist.validerHypotheses();
    }

    @FXML void actionAnnulerHypothese(ActionEvent event) {
        quitterModeHypotheseVisuel();
        save.hist.rollbackHypotheses(grilleModele, modeHypotheseActif);
    }

    private void quitterModeHypotheseVisuel() {
        modeHypotheseActif = false;
        btnHypothese.setDisable(false); conteneurBoutonsHypothese.setVisible(false);
        for (int y = 0; y < grilleModele.getTaille(); y++) {
            for (int x = 0; x < grilleModele.getTaille(); x++) vueGrille.getGrilleVueCases(x, y).setEstHypothese(false);
        }
    }

    // ========================================================
    // AIDE & UTILITAIRES
    // ========================================================

    @FXML void actionCalculatrice(ActionEvent event) { JeuUtilitaires.afficherCalculatrice(event); }
    @FXML void actionBasculerMenu(ActionEvent event) { if (menuDeroulant != null) menuDeroulant.setVisible(!menuDeroulant.isVisible()); }
    
    @FXML void actionReglesTechniques(ActionEvent event) { 
        ReglesTechniquesController.pagePrecedente = "/fxml/partie.fxml";
        changerScene("/fxml/reglesTechniques.fxml"); 
    }
    
    @FXML public void actionBoutonAidePointInterrogation() {
        if (!listeAides.isEmpty() && indexAideActuelle < listeAides.size()) listeAides.get(indexAideActuelle).masquer();
        listeAides.clear(); indexAideActuelle = 0;
        if (indicesEnAttente != null) {
            for (Indice ind : indicesEnAttente) listeAides.add(new CommandeAfficherIndice(ind, labelMessageAide, vueGrille));
        }
        if (listeAides.isEmpty()) return;
        btnActualiserAide.setVisible(false); btnActualiserAide.setManaged(false);
        bulleAide.setVisible(true); listeAides.get(indexAideActuelle).afficher();
        mettreAJourBoutonsNavigation();
    }

    @FXML public void actionFermerBulleAide() {
        if (!listeAides.isEmpty()) listeAides.get(indexAideActuelle).masquer();
        bulleAide.setVisible(false);
    }

    @FXML public void actionAmeliorerAide() {
        if (!listeAides.isEmpty()) { listeAides.get(indexAideActuelle).ameliorerNiveau(); mettreAJourBoutonsNavigation(); }
    }

    @FXML public void actionAideSuivante() {
        if (indexAideActuelle < listeAides.size() - 1) {
            listeAides.get(indexAideActuelle).masquer(); indexAideActuelle++;
            listeAides.get(indexAideActuelle).afficher(); mettreAJourBoutonsNavigation();
        }
    }

    @FXML public void actionAidePrecedente() {
        if (indexAideActuelle > 0) {
            listeAides.get(indexAideActuelle).masquer(); indexAideActuelle--;
            listeAides.get(indexAideActuelle).afficher(); mettreAJourBoutonsNavigation();
        }
    }

    @FXML void actionVerifier(ActionEvent event) {
        List<VueCase> casesEnErreur = new ArrayList<>();
        for (int y = 0; y < grilleModele.getTaille(); y++) {
            for (int x = 0; x < grilleModele.getTaille(); x++) {
                Case c = grilleModele.getCase(x, y);
                if (c.getValeur() != 0 && c.getValeur() != c.getSolution()) casesEnErreur.add(vueGrille.getGrilleVueCases(x, y));
            }
        }
        for (VueCase vc : casesEnErreur) vc.getStyleClass().add("case-erreur");
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> { for (VueCase vc : casesEnErreur) vc.getStyleClass().remove("case-erreur"); });
        pause.play();
    }

    private void mettreAJourBoutonsNavigation() {
        if (listeAides.isEmpty()) return;
        btnAidePrecedente.setDisable(indexAideActuelle == 0);
        btnAideSuivante.setDisable(indexAideActuelle == listeAides.size() - 1);
        btnAmeliorerAide.setDisable(!listeAides.get(indexAideActuelle).peutEtreAmeliore());
    }

    private void rafraichirZoneCombinaisons(Case modeleCase) {
        if (modeleCase == null || modeleCase.getGroupement() == null) { labelCombinaisons.setText("Selectionnez une case"); return; }
        GroupementCases group = modeleCase.getGroupement();    
        group.calculerPossibilites(this.grilleModele); 
        List<List<Integer>> combis = group.getCombinaisonsMaths();

        if (combis.isEmpty()) {
            labelCombinaisons.setText("Aucune combinaison possible !");
            labelCombinaisons.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(group.getResultatCible()).append(" ").append(group.getOperation().getSymbole()).append(" :\n");
            for (int i = 0; i < combis.size(); i++){
                sb.append(combis.get(i).toString());
                if(i < combis.size() - 1) sb.append(" | ");
            }
            labelCombinaisons.setText(sb.toString());
            String couleurTexte = MainApp.modeSombreActif ? "white" : "black";
            labelCombinaisons.setStyle("-fx-text-fill: " + couleurTexte + "; -fx-font-weight: normal;");
            labelCombinaisons.setWrapText(true);
        }
    }

    private void genererBoutonsNombres(int taille) {
        conteneurBoutonsNombres.getChildren().clear(); 
        String couleurTexte = MainApp.modeSombreActif ? "white" : "black";
        String couleurFond = MainApp.modeSombreActif ? "#444444" : "#f4f4f4";
        for (int i = 1; i <= taille; i++) {
            Button btnChiffre = new Button(String.valueOf(i));
            btnChiffre.setMinSize(55, 55); 
            btnChiffre.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + couleurTexte + "; -fx-background-color: " + couleurFond + "; -fx-cursor: hand; -fx-background-radius: 10;");
            final int valeur = i; btnChiffre.setOnAction(e -> actionChiffreClique(valeur));
            conteneurBoutonsNombres.getChildren().add(btnChiffre);
        }
    }

    private void demarrerChrono() {
        if (save != null && save.tmp != null && save.tmp.getTempsPrecedent() != null) {
            secondesEcoulees = save.tmp.getTempsPrecedent().intValue();
        } else { secondesEcoulees = 0; }

        labelChrono.setText(String.format("%02d:%02d", secondesEcoulees / 60, secondesEcoulees % 60));
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            secondesEcoulees++;
            labelChrono.setText(String.format("%02d:%02d", secondesEcoulees / 60, secondesEcoulees % 60));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE); 
        timeline.play();
        if (save != null && save.tmp != null) save.tmp.lancer();
    }
}