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
        
        // Utilisation propre de notre nouveau Handler Clavier
        filtreClavier = ClavierHandler.creerFiltre(
            grilleModele, 
            () -> caseModeleSelectionnee, 
            (x, y) -> selectionnerCase(vueGrille.getGrilleVueCases(x, y), grilleModele.getCase(x, y)), 
            this::actionChiffreClique, 
            () -> actionEffacer(null)
        );
        scene.addEventFilter(KeyEvent.KEY_PRESSED, filtreClavier);
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
    @FXML void actionReglesTechniques(ActionEvent event) { System.out.println("Règles ouvertes"); }
    
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
            labelCombinaisons.setStyle("-fx-text-fill: black; -fx-font-weight: normal;");
            labelCombinaisons.setWrapText(true);
        }
    }

    private void genererBoutonsNombres(int taille) {
        conteneurBoutonsNombres.getChildren().clear(); 
        for (int i = 1; i <= taille; i++) {
            Button btnChiffre = new Button(String.valueOf(i));
            btnChiffre.setMinSize(55, 55); 
            btnChiffre.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
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