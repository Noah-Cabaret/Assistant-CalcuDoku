package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.challenge.Defi;
import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.service.ProfileManager;
import fr.univ.calcudoku.service.ValidateurJeu;
import fr.univ.calcudoku.service.aide.AideService;
import fr.univ.calcudoku.view.VueCase;
import fr.univ.calcudoku.view.VueGrille;
import fr.univ.calcudoku.utils.ChronoManager;
import fr.univ.calcudoku.utils.Constantes;
import fr.univ.calcudoku.utils.PopupFactory;
import fr.univ.calcudoku.utils.JeuUtilitaires;
import fr.univ.calcudoku.utils.ScoreManager;
import fr.univ.calcudoku.utils.ThemeManager;
import fr.univ.calcudoku.utils.AideNavigateur;
import fr.univ.calcudoku.save.Etape;
import fr.univ.calcudoku.save.Sauvegarde;
import fr.univ.calcudoku.MainApp;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

public class JeuController {

    @FXML private StackPane conteneurGrille;
    @FXML private HBox conteneurBoutonsNombres;
    @FXML private Button btnUndo, btnRedo, btnAnnoter, btnEffacer, btnCalculatrice;
    @FXML private VBox bulleAide;
    @FXML private Label labelMessageAide;
    @FXML private Button btnAmeliorerAide, btnAidePrecedente, btnAideSuivante, btnActualiserAide, btnValider, btnAide;    
    @FXML private Button btnHypothese, btnValiderHypothese, btnAnnulerHypothese;
    @FXML private Button btnRetour, btnMenu;
    @FXML private HBox conteneurBoutonsHypothese;
    @FXML private VBox boiteCombinaisons;
    @FXML private Label labelCombinaisons;
    @FXML private RadioButton radioCombinaisons, radioCalculatrice;
    @FXML private Label labelChrono;
    @FXML private Label labelDefi;
    @FXML private VBox menuDeroulant;
    @FXML private StackPane popupAbandon;

    private Grille grilleModele;
    private VueGrille vueGrille;
    private Sauvegarde save;
    private ChronoManager chronoManager; 

    private boolean modeHypotheseActif = false;
    private boolean modeAnnotationActif = false;
    private boolean partiePerdue = false;
    
    private VueCase vueCaseSelectionnee = null;
    private Case caseModeleSelectionnee = null;
    
    private final AideService aideService = new AideService();
    private final AideNavigateur aideNavigateur = new AideNavigateur();
    
    private EventHandler<KeyEvent> filtreClavier;
    private EventHandler<KeyEvent> raccourcisClavier;

    public void initialiserPartie(Grille grille, Sauvegarde save) {
        this.grilleModele = grille;
        this.save = save;
        this.partiePerdue = false;

        conteneurGrille.setDisable(false);
        conteneurBoutonsNombres.setDisable(false);
        if (btnAide != null) {
            btnAide.setDisable(false);
            btnAide.setOpacity(1.0);
        }

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
                vc.setEstHypothese(modeleCase.isEstHypothese());
            }
        }
        
        appliquerModeSombre(); 
        genererBoutonsNombres(grille.getTaille());
        conteneurBoutonsNombres.applyCss();
        conteneurBoutonsNombres.layout();
        
        if (popupAbandon != null) popupAbandon.setVisible(false);
        if (menuDeroulant != null) menuDeroulant.setVisible(false);
        if (bulleAide != null) bulleAide.setVisible(false);
        
        if (btnActualiserAide != null) {
            btnActualiserAide.setOnAction(this::actionActualiserAide);
        }

        aideService.setOnSucceeded(event -> { 
            aideNavigateur.mettreAJourIndices(aideService.getValue()); 
            if (bulleAide != null && bulleAide.isVisible()) {
                aideNavigateur.rafraichirContenu(labelMessageAide, vueGrille, btnAmeliorerAide, btnAidePrecedente, btnAideSuivante);
            }
        });
        
        for (GroupementCases bloc : grilleModele.getListeGroupements()) { 
            bloc.calculerPossibilites(grilleModele); 
        }
        aideService.lancerAnalyse(grilleModele);

        if (save.getDefi() == Defi.TypeDefi.NOAID && btnAide != null) {
            btnAide.setDisable(true);
            btnAide.setOpacity(0.5);
        }

        ProfileManager manager = MainApp.getProfileManager();
        String nomProfil = manager.getProfilActif();
        java.util.Map<String, String> statsConfig = manager.lireStatistiques(nomProfil);
        String aideCalcul = statsConfig.getOrDefault(Constantes.OPTION_AIDE_CALCUL, Constantes.VALEUR_AIDE_COMBINAISONS);

        if (aideCalcul.equals(Constantes.VALEUR_AIDE_CALCULATRICE) && radioCalculatrice != null) {
            radioCalculatrice.setSelected(true);
            appliquerModeAide(Constantes.VALEUR_AIDE_CALCULATRICE);
        } else if (radioCombinaisons != null) {
            radioCombinaisons.setSelected(true);
            appliquerModeAide(Constantes.VALEUR_AIDE_COMBINAISONS);
        }

        if (radioCombinaisons != null) {
            radioCombinaisons.setOnAction(e -> {
                appliquerModeAide(Constantes.VALEUR_AIDE_COMBINAISONS);
                manager.mettreAJourStatistique(nomProfil, Constantes.OPTION_AIDE_CALCUL, Constantes.VALEUR_AIDE_COMBINAISONS);
            });
        }
        if (radioCalculatrice != null) {
            radioCalculatrice.setOnAction(e -> {
                appliquerModeAide(Constantes.VALEUR_AIDE_CALCULATRICE);
                manager.mettreAJourStatistique(nomProfil, Constantes.OPTION_AIDE_CALCUL, Constantes.VALEUR_AIDE_CALCULATRICE);
            });
        }

        filtreClavier = ClavierHandler.creerFiltre(
            grilleModele,
            () -> caseModeleSelectionnee,
            (x, y) -> selectionnerCase(vueGrille.getGrilleVueCases(x, y), grilleModele.getCase(x, y)),
            this::actionChiffreClique,
            () -> actionEffacer(null)
        );

        raccourcisClavier = event -> {
            if (event.getCode() == KeyCode.A) {
                actionBasculeAnnotation(null);
                event.consume();
            }
        };

        Platform.runLater(() -> {
            Scene scene = conteneurGrille.getScene();
            if (scene != null) {
                scene.removeEventFilter(KeyEvent.KEY_PRESSED, filtreClavier);
                scene.addEventFilter(KeyEvent.KEY_PRESSED, filtreClavier);
                scene.removeEventFilter(KeyEvent.KEY_PRESSED, raccourcisClavier);
                scene.addEventFilter(KeyEvent.KEY_PRESSED, raccourcisClavier);
                JeuUtilitaires.installerSecuritesFermeture(scene, this::sauvegarderPartie, this::sauvegarderPartie);
            }
        });

        mettreAJourLabelDefi();
        
        this.chronoManager = new ChronoManager(labelChrono, save, () -> declencherDefaite("Le temps est écoulé !"));
        this.chronoManager.demarrer();
    }

    private void appliquerModeSombre() {
        boolean sombre = MainApp.isModeSombre();
        Platform.runLater(() -> {
            ThemeManager.appliquerModeSombre(
                conteneurGrille.getScene(), sombre,
                btnRetour, btnMenu, btnValider, btnAide,
                btnValiderHypothese, btnAnnulerHypothese,
                btnUndo, btnRedo, btnAnnoter, btnEffacer, btnCalculatrice
            );
        });
    }

    private void mettreAJourLabelDefi() {
        if (labelDefi == null || save == null) return;
        labelDefi.setManaged(true);
        labelDefi.setVisible(true);
        switch (save.getDefi()) {
            case CHRON: labelDefi.setText("Défi : Contre la montre"); break;
            case NOAID: labelDefi.setText("Défi : Aucune aide disponible"); break;
            case SURVI: labelDefi.setText("Défi : Mode Survie, il vous reste " + save.getVies() + " vie(s)"); break;
            default: labelDefi.setVisible(false); labelDefi.setManaged(false); break;
        }
    }

    private void appliquerModeAide(String mode) {
        if (mode.equals(Constantes.VALEUR_AIDE_CALCULATRICE)) {
            if (boiteCombinaisons != null) {
                boiteCombinaisons.setVisible(false);
                boiteCombinaisons.setManaged(false); 
            }
            if (btnCalculatrice != null) {
                btnCalculatrice.setVisible(true);
                btnCalculatrice.setManaged(true);
            }
        } else {
            if (boiteCombinaisons != null) {
                boiteCombinaisons.setVisible(true);
                boiteCombinaisons.setManaged(true);
            }
            if (btnCalculatrice != null) {
                btnCalculatrice.setVisible(false);
                btnCalculatrice.setManaged(false);
            }
            JeuUtilitaires.cacherCalculatrice();
        }
    }

    private void genererBoutonsNombres(int taille) {
        if (conteneurBoutonsNombres != null) {
            conteneurBoutonsNombres.getChildren().clear(); 
            for (int i = 1; i <= taille; i++) {
                Button btnChiffre = new Button(String.valueOf(i));
                btnChiffre.setMinSize(55, 55); 
                btnChiffre.getStyleClass().add("bouton-chiffre"); 
                final int valeur = i; 
                btnChiffre.setOnAction(e -> actionChiffreClique(valeur));
                conteneurBoutonsNombres.getChildren().add(btnChiffre);
            }
        }
    }

    private void rafraichirZoneCombinaisons(Case modeleCase) {
        if (labelCombinaisons == null) return;
        if (modeleCase == null || modeleCase.getGroupement() == null) { 
            labelCombinaisons.setText("Selectionnez une case"); 
            return; 
        }
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
            labelCombinaisons.setStyle("");
            labelCombinaisons.setWrapText(true);
        }
    }

    private void sauvegarderPartie() {
        if (save != null && !partiePerdue && save.getIdGrille() != null && !save.getIdGrille().isEmpty()) {
            String nomProfil = MainApp.getProfileManager().getProfilActif();
            if (chronoManager != null) chronoManager.arreter();
            mettreAJourGuidesVisuels(null);
            if (vueCaseSelectionnee != null) {
                vueCaseSelectionnee.getStyleClass().remove(Constantes.CSS_CASE_SELECTIONNEE);
            }
            save.enreg(nomProfil, grilleModele);
            JeuUtilitaires.sauvegarderImageGrille(grilleModele, vueGrille, vueCaseSelectionnee, save.getIdGrille() + ".png", this::actionFermerBulleAide);
        }
    }

    private void deconnecterClavier() {
        if (conteneurGrille != null && conteneurGrille.getScene() != null) {
            if (filtreClavier != null) {
                conteneurGrille.getScene().removeEventFilter(KeyEvent.KEY_PRESSED, filtreClavier);
            }
            if (raccourcisClavier != null) {
                conteneurGrille.getScene().removeEventFilter(KeyEvent.KEY_PRESSED, raccourcisClavier);
            }
            JeuUtilitaires.desinstallerSecuritesFermeture(conteneurGrille.getScene());
        }
    }

    private void declencherDefaite(String message) {
        if (partiePerdue) return;
        partiePerdue = true;
        if (chronoManager != null) chronoManager.arreter();
        conteneurGrille.setDisable(true);
        conteneurBoutonsNombres.setDisable(true);
        deconnecterClavier(); 
        String nomProfil = MainApp.getProfileManager().getProfilActif();
        int secEcoulees = (chronoManager != null) ? chronoManager.getSecondesEcoulees() : 0;
        MainApp.getProfileManager().enregistrerFinDePartie(nomProfil, false, secEcoulees, 0, save.getDiff(), save.getIdGrille());        
        if (save != null) {
            save.effacer(nomProfil);
        }
        PopupFactory.afficherPopupFinPartie("Défi échoué !", message, Constantes.ICONE_DEFAITE, Constantes.COULEUR_DEFAITE, false, () -> actionRecommencer(null), () -> actionRetourMenu(null));
    }
    
    private void verifierVictoire() {
        if (ValidateurJeu.estVictoire(grilleModele) && !partiePerdue) {
            partiePerdue = true; 
            if (chronoManager != null) chronoManager.arreter();
            conteneurGrille.setDisable(true);
            conteneurBoutonsNombres.setDisable(true);
            deconnecterClavier();
            String nomProfil = MainApp.getProfileManager().getProfilActif();
            int secEcoulees = chronoManager.getSecondesEcoulees();

            long scoreCalcul = ScoreManager.calculerScore(
                grilleModele.getTaille(), secEcoulees,
                save.getMalus(), save.getAidesUtilisees(), save.getDiff()
            );

            MainApp.getProfileManager().enregistrerFinDePartie(nomProfil, true, secEcoulees, scoreCalcul, save.getDiff(), save.getIdGrille());            
            if (save != null) save.effacer(nomProfil);
            String msg = "Temps : " + ScoreManager.formaterTemps(secEcoulees) + ".\nScore obtenu : " + scoreCalcul;
            PopupFactory.afficherPopupFinPartie("Victoire !", msg, Constantes.ICONE_VICTOIRE, Constantes.COULEUR_VICTOIRE, true, () -> actionRecommencer(null), () -> actionRetourMenu(null));
        }
    }

    private void selectionnerCase(VueCase vueCase, Case modeleCase) {
        if (vueCaseSelectionnee != null) vueCaseSelectionnee.getStyleClass().remove(Constantes.CSS_CASE_SELECTIONNEE);
        this.vueCaseSelectionnee = vueCase;
        this.caseModeleSelectionnee = modeleCase;
        vueCaseSelectionnee.getStyleClass().add(Constantes.CSS_CASE_SELECTIONNEE);
        rafraichirZoneCombinaisons(modeleCase);
        mettreAJourGuidesVisuels(modeleCase);
    }

    private void actionChiffreClique(int valeur) {
        if (caseModeleSelectionnee != null) {
            if (modeAnnotationActif) {
                caseModeleSelectionnee.basculerNote(valeur); 
                if(caseModeleSelectionnee.getValeur() < 10)
                    save.getHistorique().addEtape(caseModeleSelectionnee.getX(), caseModeleSelectionnee.getY(), valeur + Etape.OFFSET_ANNOTATION + (modeHypotheseActif ? Etape.OFFSET_HYPOTHESE : 0));
            } else {
                save.getHistorique().addEtape(caseModeleSelectionnee.getX(), caseModeleSelectionnee.getY(), valeur + (modeHypotheseActif ? Etape.OFFSET_HYPOTHESE : 0));
                caseModeleSelectionnee.setEstHypothese(modeHypotheseActif);
                caseModeleSelectionnee.setValeur(valeur);   
                vueCaseSelectionnee.setEstHypothese(modeHypotheseActif);
                rafraichirAnnotations(caseModeleSelectionnee.getX(), caseModeleSelectionnee.getY(), valeur);
                rafraichirZoneCombinaisons(caseModeleSelectionnee);
                aideService.lancerAnalyse(grilleModele);
                mettreAJourGuidesVisuels(caseModeleSelectionnee);
                verifierVictoire();
            }
        }
    }

    @FXML void actionBasculeAnnotation(ActionEvent event) {
        modeAnnotationActif = !modeAnnotationActif;
        if (btnAnnoter != null) btnAnnoter.setStyle(modeAnnotationActif ? "-fx-background-color: #bbdefb;" : "");
    }

    @FXML void actionEffacer(ActionEvent event) {
        if (caseModeleSelectionnee != null) {
            caseModeleSelectionnee.setEstHypothese(false);
            caseModeleSelectionnee.setValeur(0);
            caseModeleSelectionnee.effacerNotes();
            aideService.lancerAnalyse(grilleModele);
            save.getHistorique().addEtape(caseModeleSelectionnee.getX(), caseModeleSelectionnee.getY(), (modeHypotheseActif ? Etape.OFFSET_HYPOTHESE : 0));
            mettreAJourGuidesVisuels(caseModeleSelectionnee);
        }
    }

    @FXML void actionVerifier(ActionEvent event) {
        List<Case> casesEnErreur = ValidateurJeu.trouverErreurs(grilleModele);
        List<VueCase> vuesEnErreur = new ArrayList<>();
        boolean caseIncorrecte = false;

        for (Case c : casesEnErreur) {
            vuesEnErreur.add(vueGrille.getGrilleVueCases(c.getX(), c.getY()));
            save.setMalus(save.getMalus() + 1);
            if (save.getDefi() == Defi.TypeDefi.SURVI) caseIncorrecte = true;
        }
        if (caseIncorrecte && save.getDefi() == Defi.TypeDefi.SURVI) {
            save.setVies(save.getVies() - 1);
            mettreAJourLabelDefi();
            if (save.getVies() <= 0) {
                declencherDefaite("Vous n'avez plus de vies !");
                return;
            }
        }
        for (VueCase vc : vuesEnErreur) vc.getStyleClass().add(Constantes.CSS_CASE_ERREUR);
        if (!vuesEnErreur.isEmpty()) {
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(e -> { 
                for (VueCase vc : vuesEnErreur) vc.getStyleClass().remove(Constantes.CSS_CASE_ERREUR); 
            });
            pause.play();
        }
    }

    @FXML void actionUndo(ActionEvent event) {
        save.getHistorique().appliquerUndo(grilleModele, modeHypotheseActif);
        synchroniserVuesApresHistorique();
    }

    @FXML void actionRedo(ActionEvent event) {
        save.getHistorique().appliquerRedo(grilleModele);
        synchroniserVuesApresHistorique();
    }

    private void synchroniserVuesApresHistorique() {
        if (caseModeleSelectionnee != null) rafraichirZoneCombinaisons(caseModeleSelectionnee);
        for (int y = 0; y < grilleModele.getTaille(); y++) {
            for (int x = 0; x < grilleModele.getTaille(); x++) {
                Case c = grilleModele.getCase(x, y);
                vueGrille.getGrilleVueCases(x, y).setEstHypothese(c.isEstHypothese());
            }
        }
        mettreAJourGuidesVisuels(caseModeleSelectionnee);
    }

    @FXML void actionHypothese(ActionEvent event) {
        modeHypotheseActif = true;
        if (btnHypothese != null) btnHypothese.setDisable(true); 
        if (conteneurBoutonsHypothese != null) conteneurBoutonsHypothese.setVisible(true);
    }

    @FXML void actionValiderHypothese(ActionEvent event) {
        quitterModeHypotheseVisuel();
        save.getHistorique().validerHypotheses();
    }

    @FXML void actionAnnulerHypothese(ActionEvent event) {
        quitterModeHypotheseVisuel();
        save.getHistorique().rollbackHypotheses(grilleModele, modeHypotheseActif);
    }

    private void quitterModeHypotheseVisuel() {
        modeHypotheseActif = false;
        if (btnHypothese != null) btnHypothese.setDisable(false); 
        if (conteneurBoutonsHypothese != null) conteneurBoutonsHypothese.setVisible(false);
        for (int y = 0; y < grilleModele.getTaille(); y++) {
            for (int x = 0; x < grilleModele.getTaille(); x++) {
                grilleModele.getCase(x, y).setEstHypothese(false); 
                vueGrille.getGrilleVueCases(x, y).setEstHypothese(false);
            }
        }
    }

    @FXML void actionBasculerMenu(ActionEvent event) {
        if (menuDeroulant != null) menuDeroulant.setVisible(!menuDeroulant.isVisible());
    }

    @FXML void actionRetourMenu(ActionEvent event) {
        sauvegarderPartie();
        deconnecterClavier(); 
        if (chronoManager != null) chronoManager.arreter();
        JeuUtilitaires.cacherCalculatrice();
        if (save != null) {
            if (save.getMode() == Sauvegarde.ModeDeJeu.AVEN) {
                MainApp.changerScene(Constantes.VUE_MENU_AVENTURE);
            } else {
                MainApp.changerScene(Constantes.VUE_MENU_LIBRE);
            }
        } else {
            MainApp.changerScene(Constantes.VUE_MENU);
        }
    }

    private void rafraichirAnnotations(int targetX, int targetY, int valeurJouee) {
        int taille = grilleModele.getTaille();
        for (int i = 0; i < taille; i++) {
            if (i != targetY) {
                Case c = grilleModele.getCase(targetX, i);
                c.supprimerUneNote(valeurJouee);
            }
            if (i != targetX) {
                Case c = grilleModele.getCase(i, targetY);
                c.supprimerUneNote(valeurJouee); 
            }
        }
    }

    @FXML void actionAbandonner(ActionEvent event) {
        if (menuDeroulant != null) menuDeroulant.setVisible(false);
        if (popupAbandon != null) popupAbandon.setVisible(true);
    }

    @FXML void actionAnnulerAbandon(ActionEvent event) {
        if (popupAbandon != null) popupAbandon.setVisible(false);
    }

    @FXML void actionConfirmerAbandon(ActionEvent event) {
        if (popupAbandon != null) popupAbandon.setVisible(false);
        String nomProfil = MainApp.getProfileManager().getProfilActif();
        int secEcoulees = (chronoManager != null) ? chronoManager.getSecondesEcoulees() : 0;
        MainApp.getProfileManager().enregistrerFinDePartie(nomProfil, false, secEcoulees, 0, save.getDiff(), save.getIdGrille());        
        partiePerdue = true; 
        if (save != null) save.effacer(nomProfil);
        actionRetourMenu(event);
    }

    @FXML void actionRecommencer(ActionEvent event) {
        if (menuDeroulant != null) menuDeroulant.setVisible(false);
        partiePerdue = false; 
        conteneurGrille.setDisable(false);
        conteneurBoutonsNombres.setDisable(false);
        if (conteneurGrille.getScene() != null && filtreClavier != null) {
            conteneurGrille.getScene().removeEventFilter(KeyEvent.KEY_PRESSED, filtreClavier);
            conteneurGrille.getScene().addEventFilter(KeyEvent.KEY_PRESSED, filtreClavier);
        }
        for (int y = 0; y < grilleModele.getTaille(); y++) {
            for (int x = 0; x < grilleModele.getTaille(); x++) {
                Case c = grilleModele.getCase(x, y);
                c.setEstHypothese(false);
                c.setValeur(0);
                c.effacerNotes();
                vueGrille.getGrilleVueCases(x, y).getStyleClass().remove(Constantes.CSS_CASE_ERREUR);
                vueGrille.getGrilleVueCases(x, y).setEstHypothese(false);
            }
        }
        save.resetHistorique();
        if (chronoManager != null) chronoManager.arreter();
        save.getTemps().setTempsPrecedent(0.0);
        save.setMalus(0); 
        save.setAidesUtilisees(0); 
        fr.univ.calcudoku.model.DonneesNiveau dataBase = fr.univ.calcudoku.utils.GestionnaireJeu.lireDonneesNiveauRessource(save.getIdGrille() + ".json");
        if (dataBase != null) save.setVies(dataBase.vies);
        quitterModeHypotheseVisuel();
        mettreAJourLabelDefi(); 
        this.chronoManager = new ChronoManager(labelChrono, save, () -> declencherDefaite("Le temps est écoulé !"));
        this.chronoManager.demarrer();
        aideService.lancerAnalyse(grilleModele);
        if (caseModeleSelectionnee != null) rafraichirZoneCombinaisons(caseModeleSelectionnee);
        mettreAJourGuidesVisuels(caseModeleSelectionnee);
    }

    @FXML void actionReglesTechniques(ActionEvent event) {
        sauvegarderPartie();
        deconnecterClavier(); 
        if (menuDeroulant != null) menuDeroulant.setVisible(false);
        if (chronoManager != null) chronoManager.arreter(); 
        JeuUtilitaires.cacherCalculatrice();
        String nomProfil = MainApp.getProfileManager().getProfilActif();
        String sousDossier = (save.getMode() == Sauvegarde.ModeDeJeu.AVEN) ? Constantes.SOUS_DOSSIER_AVENTURE : "";
        java.io.File fichierSave = new java.io.File(Constantes.DOSSIER_PROFILS + nomProfil + Constantes.SOUS_DOSSIER_PARTIES + sousDossier + save.getIdGrille() + ".json");
        javafx.stage.Stage stage = (javafx.stage.Stage) conteneurGrille.getScene().getWindow();
        ReglesTechniquesController.actionRetour = () -> {
            fr.univ.calcudoku.utils.GestionnaireJeu.chargerPartieDepuisFichier(stage, fichierSave);
        };
        MainApp.changerScene(Constantes.VUE_REGLES);
    }

    @FXML void actionCalculatrice(ActionEvent event) { JeuUtilitaires.afficherCalculatrice(event); }
    
    @FXML void actionActualiserAide(ActionEvent event) {
        if (labelMessageAide != null) labelMessageAide.setText("Recherche de techniques en cours...");
        aideService.lancerAnalyse(grilleModele); 
    }
    
    @FXML public void actionBoutonAidePointInterrogation() {
        aideService.lancerAnalyse(grilleModele); 
        if (bulleAide != null && !bulleAide.isVisible()) {
            save.setAidesUtilisees(save.getAidesUtilisees() + 1);
        }
        if (bulleAide != null) bulleAide.setVisible(true); 
        if (btnActualiserAide != null) {
            btnActualiserAide.setVisible(true); 
            btnActualiserAide.setManaged(true);
        }
        aideNavigateur.rafraichirContenu(labelMessageAide, vueGrille, btnAmeliorerAide, btnAidePrecedente, btnAideSuivante);
    }
    
    @FXML public void actionFermerBulleAide() {
        aideNavigateur.fermer();
        if (bulleAide != null) bulleAide.setVisible(false);
    }

    @FXML public void actionAmeliorerAide() {
        aideNavigateur.ameliorer(btnAmeliorerAide, btnAidePrecedente, btnAideSuivante);
        save.setAidesUtilisees(save.getAidesUtilisees() + 1);
    }

    @FXML public void actionAideSuivante() {
        aideNavigateur.suivant(btnAmeliorerAide, btnAidePrecedente, btnAideSuivante);
    }

    @FXML public void actionAidePrecedente() {
        aideNavigateur.precedent(btnAmeliorerAide, btnAidePrecedente, btnAideSuivante);
    }

    private void mettreAJourGuidesVisuels(Case caseActuelle) {
        if (grilleModele == null || vueGrille == null) return;
        int taille = grilleModele.getTaille();
        for (int x = 0; x < taille; x++) {
            for (int y = 0; y < taille; y++) {
                vueGrille.getGrilleVueCases(x, y).getStyleClass().remove("case-guide-visuel");
            }
        }
        if (caseActuelle == null) return;
        int selX = caseActuelle.getX();
        int selY = caseActuelle.getY();
        int selValeur = caseActuelle.getValeur();
        for (int x = 0; x < taille; x++) {
            for (int y = 0; y < taille; y++) {
                Case c = grilleModele.getCase(x, y);
                boolean estSurMemeLigne = (y == selY);
                boolean estSurMemeColonne = (x == selX);
                boolean aMemeValeur = (selValeur != 0 && selValeur <= taille && c.getValeur() == selValeur);
                if ((estSurMemeLigne || estSurMemeColonne || aMemeValeur) && c != caseActuelle) {
                    VueCase vueCase = vueGrille.getGrilleVueCases(x, y);
                    if (!vueCase.getStyleClass().contains("case-guide-visuel")) {
                        vueCase.getStyleClass().add("case-guide-visuel");
                    }
                }
            }
        }
    }

    @FXML public void actionOuvrirDoc() {
        List<Indice> indices = aideNavigateur.getIndicesEnAttente();
        int index = aideNavigateur.getIndexAideActuelle();
        if (indices != null && index < indices.size()) {
            Indice actuel = indices.get(index);
            String nomTech = actuel.getNomTechnique();
            sauvegarderPartie();
            deconnecterClavier();
            if (chronoManager != null) chronoManager.arreter();
            
            javafx.stage.Stage stageCapture = (javafx.stage.Stage) conteneurGrille.getScene().getWindow();
            String sousDossier = (save.getMode() == Sauvegarde.ModeDeJeu.AVEN) ? Constantes.SOUS_DOSSIER_AVENTURE : "";
            
            ReglesTechniquesController.techniqueCiblee = nomTech;
            ReglesTechniquesController.actionRetour = () -> {
                String nomProfil = MainApp.getProfileManager().getProfilActif();
                java.io.File f = new java.io.File(Constantes.DOSSIER_PROFILS + nomProfil + Constantes.SOUS_DOSSIER_PARTIES + sousDossier + save.getIdGrille() + ".json");
                fr.univ.calcudoku.utils.GestionnaireJeu.chargerPartieDepuisFichier(stageCapture, f);
            };
            MainApp.changerScene(Constantes.VUE_REGLES);
        }
    }
}