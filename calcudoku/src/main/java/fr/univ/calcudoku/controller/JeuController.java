package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.challenge.Defi;
import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.commande.CommandeAide;
import fr.univ.calcudoku.commande.CommandeAfficherIndice;
import fr.univ.calcudoku.service.ProfileManager;
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
import javafx.scene.input.KeyEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
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
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JeuController {

    @FXML private StackPane conteneurGrille, popupAbandon;
    @FXML private HBox conteneurBoutonsNombres, conteneurBoutonsHypothese;
    @FXML private Button btnUndo, btnRedo, btnAnnoter, btnEffacer, btnCalculatrice, btnHypothese, btnRetour, btnMenu;
    @FXML private VBox boiteCombinaisons, bulleAide, menuDeroulant;
    @FXML private Label labelChrono, labelCombinaisons, labelMessageAide;
    @FXML private RadioButton radioCombinaisons, radioCalculatrice;
    @FXML private ToggleGroup groupeConfigAide;
    @FXML private Button btnAmeliorerAide, btnAidePrecedente, btnAideSuivante, btnActualiserAide;
    @FXML private Button btnValider, btnAide, btnValiderHypothese, btnAnnulerHypothese;

    private boolean modeHypotheseActif = false, modeAnnotationActif = false;
    private Timeline timeline;
    private int secondesEcoulees = 0;

    private Grille grilleModele;
    private VueGrille vueGrille;
    private VueCase vueCaseSelectionnee = null;
    private Case caseModeleSelectionnee = null;
    private Sauvegarde save;
    private boolean partiePerdue = false;

    private final AideService aideService = new AideService();
    private List<CommandeAide> listeAides = new ArrayList<>();
    private List<Indice> indicesEnAttente = new ArrayList<>();
    private int indexAideActuelle = 0;

    private EventHandler<KeyEvent> filtreClavier;

    public static String pagePrecedente = "/fxml/menu.fxml";

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
            }
        }
        
        genererBoutonsNombres(grille.getTaille());
        appliquerModeSombre(); 
        
        if (popupAbandon != null) popupAbandon.setVisible(false);
        if (menuDeroulant != null) menuDeroulant.setVisible(false);
        if (bulleAide != null) bulleAide.setVisible(false);

        aideService.setOnSucceeded(event -> { indicesEnAttente = aideService.getValue(); });
        for (GroupementCases bloc : grilleModele.getListeGroupements()) { bloc.calculerPossibilites(grilleModele); }
        aideService.lancerAnalyse(grilleModele);

        if(save.getDefi() == Defi.TypeDefi.NOAID && btnAide != null) {
            btnAide.setDisable(true);
            btnAide.setOpacity(0.5);
        }

        ProfileManager manager = MainApp.getProfileManager();
        String nomProfil = manager.getProfilActif() != null ? manager.getProfilActif() : "Invité";
        Map<String, String> statsConfig = manager.lireStatistiques(nomProfil);
        String aideCalcul = statsConfig.getOrDefault("aide_calcul", "combinaisons");

        if (aideCalcul.equals("calculatrice") && radioCalculatrice != null) {
            radioCalculatrice.setSelected(true);
            appliquerModeAide("calculatrice");
        } else if (radioCombinaisons != null) {
            radioCombinaisons.setSelected(true);
            appliquerModeAide("combinaisons");
        }

        if (radioCombinaisons != null) {
            radioCombinaisons.setOnAction(e -> {
                appliquerModeAide("combinaisons");
                manager.mettreAJourStatistique(nomProfil, "aide_calcul", "combinaisons");
            });
        }
        if (radioCalculatrice != null) {
            radioCalculatrice.setOnAction(e -> {
                appliquerModeAide("calculatrice");
                manager.mettreAJourStatistique(nomProfil, "aide_calcul", "calculatrice");
            });
        }
        
        filtreClavier = ClavierHandler.creerFiltre(
            grilleModele,
            () -> caseModeleSelectionnee,
            (x, y) -> selectionnerCase(vueGrille.getGrilleVueCases(x, y), grilleModele.getCase(x, y)),
            val -> actionChiffreClique(val),
            () -> actionEffacer(null)
        );

        Platform.runLater(() -> {
            Scene scene = conteneurGrille.getScene();
            if (scene != null) {
                scene.removeEventFilter(KeyEvent.KEY_PRESSED, filtreClavier);
                scene.addEventFilter(KeyEvent.KEY_PRESSED, filtreClavier);
                JeuUtilitaires.installerSecuritesFermeture(scene, () -> sauvegarderPartie(), () -> sauvegarderPartie());
            }
        });

        demarrerChrono();
    }

    // ========================================================
    // GESTION DU MODE SOMBRE
    // ========================================================

    private void appliquerModeSombre() {
        boolean sombre = MainApp.modeSombreActif;
        
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
            if (vueGrille != null) {
                vueGrille.lookupAll(".label").forEach(noeud -> noeud.setStyle("-fx-text-fill: black;"));
            }
        });

        String couleurTexte = sombre ? "white" : "black";
        String couleurFond = sombre ? "#2b2b2b" : "white";
        String couleurBordure = sombre ? "#888888" : "black";
        
        if (labelChrono != null) labelChrono.setStyle("-fx-text-fill: " + couleurTexte + ";");
        if (labelCombinaisons != null) labelCombinaisons.setStyle("-fx-text-fill: " + couleurTexte + ";");
        if (boiteCombinaisons != null) boiteCombinaisons.setStyle("-fx-background-color: " + couleurFond + "; -fx-border-color: " + couleurBordure + "; -fx-border-width: 1px; -fx-padding: 10px;");
        
        String couleurMenu = sombre ? "-fx-background-color: #333333; -fx-border-color: #555555; -fx-border-width: 1px; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-padding: 15px;" 
                                    : "-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-width: 1px; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-padding: 15px;";
        if (menuDeroulant != null) menuDeroulant.setStyle(couleurMenu);

        if (menuDeroulant != null) {
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
        }

        String styleRondBase = " -fx-border-width: 3px; -fx-border-radius: 50%; -fx-background-radius: 50%; -fx-cursor: hand; ";
        String couleurBoutonRond = sombre ? "-fx-background-color: #444444; -fx-border-color: #888888;" : "-fx-background-color: white; -fx-border-color: black;";
        
        if (btnValider != null) btnValider.setStyle(couleurBoutonRond + styleRondBase + "-fx-min-width: 60px; -fx-min-height: 60px;");
        if (btnAide != null) btnAide.setStyle(couleurBoutonRond + styleRondBase + "-fx-min-width: 60px; -fx-min-height: 60px;");
        if (btnHypothese != null) btnHypothese.setStyle(couleurBoutonRond + styleRondBase + "-fx-min-width: 60px; -fx-min-height: 60px; -fx-font-size: 38px; -fx-font-family: 'Times New Roman', serif; -fx-font-style: italic; -fx-text-fill: " + couleurTexte + "; -fx-padding: 0;");
        if (btnValiderHypothese != null) btnValiderHypothese.setStyle(couleurBoutonRond + styleRondBase + "-fx-min-width: 45px; -fx-min-height: 45px;");
        if (btnAnnulerHypothese != null) btnAnnulerHypothese.setStyle(couleurBoutonRond + styleRondBase + "-fx-min-width: 45px; -fx-min-height: 45px;");

        Color iconColor = sombre ? Color.WHITE : Color.BLACK;
        Button[] boutonsAvecIcones = {btnRetour, btnMenu, btnValider, btnAide, btnValiderHypothese, btnAnnulerHypothese, btnUndo, btnRedo, btnAnnoter, btnEffacer, btnCalculatrice, btnActualiserAide};
        
        for (Button btn : boutonsAvecIcones) {
            if (btn != null && btn.getGraphic() instanceof FontIcon) {
                ((FontIcon) btn.getGraphic()).setIconColor(iconColor);
            }
        }

        // ---> CORRECTION DE LA POPUP D'ABANDON <---
        if (popupAbandon != null && !popupAbandon.getChildren().isEmpty()) {
            VBox boitePopup = (VBox) popupAbandon.getChildren().get(0);
            boitePopup.setStyle("-fx-background-color: " + couleurFond + "; -fx-background-radius: 12px; -fx-padding: 20px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 15, 0, 0, 0);");
            
            for (javafx.scene.Node n : boitePopup.getChildren()) {
                if (n instanceof Label) {
                    Label lbl = (Label) n;
                    if (lbl.getText().contains("Abandonner")) {
                        lbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + couleurTexte + ";");
                    } else if (lbl.getText().contains("progression")) {
                        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #e57373;");
                    }
                } else if (n instanceof javafx.scene.layout.HBox) {
                    javafx.scene.layout.HBox boxBoutons = (javafx.scene.layout.HBox) n;
                    if (!boxBoutons.getChildren().isEmpty() && boxBoutons.getChildren().get(0) instanceof Button) {
                        Button btnAnnuler = (Button) boxBoutons.getChildren().get(0);
                        String couleurBtnAnnuler = sombre ? "#444444" : "#f5f5f5";
                        btnAnnuler.setStyle("-fx-background-color: " + couleurBtnAnnuler + "; -fx-border-color: " + couleurBordure + "; -fx-text-fill: " + couleurTexte + "; -fx-cursor: hand; -fx-min-width: 90px;");
                    }
                }
            }
        }
    }

    private void appliquerModeAide(String mode) {
        if (mode.equals("calculatrice")) {
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
        }
    }

    // ========================================================
    // CHANGEMENTS DE SCENES ET SAUVEGARDES
    // ========================================================

    @FXML void actionBasculerMenu(ActionEvent event) {
        if (menuDeroulant != null) {
            menuDeroulant.setVisible(!menuDeroulant.isVisible());
        }
    }

    @FXML void actionRetourMenu(ActionEvent event) { 
        sauvegarderPartie();
        deconnecterClavier();
        if (timeline != null) timeline.stop();
        JeuUtilitaires.cacherCalculatrice();
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
        if (popupAbandon != null) popupAbandon.setVisible(false);
        
        String nomProfil = MainApp.getProfileManager().getProfilActif();
        if (nomProfil == null) nomProfil = "Invité";
        MainApp.getProfileManager().enregistrerFinDePartie(nomProfil, false, secondesEcoulees, 0, save.getDiff(), false);
        
        partiePerdue = true; 
        if (save != null) save.effacer(nomProfil);
        
        deconnecterClavier();
        if (timeline != null) timeline.stop();
        JeuUtilitaires.cacherCalculatrice();
        changerScene("/fxml/menu.fxml");
    }

    private void changerScene(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML void actionRecommencer(ActionEvent event) {
        if (menuDeroulant != null) menuDeroulant.setVisible(false);
        
        for (int y = 0; y < grilleModele.getTaille(); y++) {
            for (int x = 0; x < grilleModele.getTaille(); x++) {
                Case c = grilleModele.getCase(x, y);
                c.setValeur(0);
                c.effacerNotes();
                vueGrille.getGrilleVueCases(x, y).setEstHypothese(false);
            }
        }
        
        save.hist = new fr.univ.calcudoku.save.Historique();
        if (timeline != null) timeline.stop();
        save.tmp.setTempsPrecedent(0.0);
        secondesEcoulees = 0;
        
        quitterModeHypotheseVisuel();
        aideService.lancerAnalyse(grilleModele);
        if (caseModeleSelectionnee != null) rafraichirZoneCombinaisons(caseModeleSelectionnee);
        demarrerChrono();
    }

    @FXML void actionReglesTechniques(ActionEvent event) {
        sauvegarderPartie();
        deconnecterClavier(); 
        if (menuDeroulant != null) menuDeroulant.setVisible(false);
        if (timeline != null) timeline.pause(); 
        
        String nomProfil = MainApp.getProfileManager().getProfilActif();
        if (nomProfil == null) nomProfil = "Invité";
        
        ReglesTechniquesController.pagePrecedente = "/fxml/partie.fxml";
        MainApp.changerScene("/fxml/reglesTechniques.fxml");
    }

    private void sauvegarderPartie() {
        if (save != null && !partiePerdue && save.getIdGrille() != null && !save.getIdGrille().isEmpty()) {
            String nomProfil = MainApp.getProfileManager().getProfilActif();
            if (nomProfil == null) nomProfil = "Invité";
            
            if (timeline != null) timeline.stop();
            if (save.tmp != null) save.tmp.setTempsPrecedent(save.tmp.tempsTotal());
            
            save.enreg(nomProfil, grilleModele);
            JeuUtilitaires.sauvegarderImageGrille(grilleModele, vueGrille, vueCaseSelectionnee, save.getIdGrille() + ".png", () -> actionFermerBulleAide());
        }
    }
    
    private void deconnecterClavier() {
        if (conteneurGrille != null && conteneurGrille.getScene() != null && filtreClavier != null) {
            conteneurGrille.getScene().removeEventFilter(KeyEvent.KEY_PRESSED, filtreClavier);
        }
    }

    private void demarrerChrono() {
        if (save != null && save.tmp != null && save.tmp.getTempsPrecedent() != null) {
            secondesEcoulees = save.tmp.getTempsPrecedent().intValue();
        } else { 
            secondesEcoulees = 0; 
        }

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (partiePerdue) return;
            secondesEcoulees++;
            
            if(save.getDefi() == Defi.TypeDefi.CHRON) {
                int restant = Math.max(0, save.tmp.getTempsMax().intValue() - secondesEcoulees);
                if (labelChrono != null) labelChrono.setText(String.format("%02d:%02d", restant / 60, restant % 60));
                if(restant <= 0) declencherDefaite("Le temps est écoulé !");
            } else {
                if (labelChrono != null) labelChrono.setText(String.format("%02d:%02d", secondesEcoulees / 60, secondesEcoulees % 60));
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        if (save != null && save.tmp != null) save.tmp.lancer();
    }

    // ========================================================
    // GESTION DES VICTOIRES / DEFAITES
    // ========================================================

    private void declencherDefaite(String message) {
        if (partiePerdue) return;
        partiePerdue = true;
        
        if (timeline != null) timeline.stop();
        conteneurGrille.setDisable(true);
        conteneurBoutonsNombres.setDisable(true);
        deconnecterClavier(); 
        
        String nomProfil = MainApp.getProfileManager().getProfilActif();
        if (nomProfil == null) nomProfil = "Invité";

        MainApp.getProfileManager().enregistrerFinDePartie(nomProfil, false, secondesEcoulees, 0, save.getDiff(), false);
        if (save != null) save.effacer(nomProfil);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Défaite");
        alert.setHeaderText("Défi échoué !");
        alert.setContentText(message);
        alert.showAndWait();

        changerScene("/fxml/menu.fxml");
    }

    private void verifierVictoire() {
        boolean complet = true;
        for (int y = 0; y < grilleModele.getTaille(); y++) {
            for (int x = 0; x < grilleModele.getTaille(); x++) {
                Case c = grilleModele.getCase(x, y);
                if (c.getValeur() == 0 || c.getValeur() != c.getSolution()) {
                    complet = false;
                    break;
                }
            }
            if (!complet) break;
        }

        if (complet && !partiePerdue) {
            partiePerdue = true; 
            if (timeline != null) timeline.stop();
            conteneurGrille.setDisable(true);
            conteneurBoutonsNombres.setDisable(true);
            deconnecterClavier();

            String nomProfil = MainApp.getProfileManager().getProfilActif();
            if (nomProfil == null) nomProfil = "Invité";

            long scoreCalcul = Math.max(0, (grilleModele.getTaille() * 1000) - (secondesEcoulees * 10));
            boolean estAventure = (save.getMode() == Sauvegarde.ModeDeJeu.AVEN);

            MainApp.getProfileManager().enregistrerFinDePartie(nomProfil, true, secondesEcoulees, scoreCalcul, save.getDiff(), estAventure);
            if (save != null) save.effacer(nomProfil);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Victoire !");
            alert.setHeaderText("Grille complétée !");
            alert.setContentText("Félicitations, vous avez terminé la grille en " + (secondesEcoulees / 60) + " min " + (secondesEcoulees % 60) + " s.\nScore obtenu : " + scoreCalcul);
            alert.showAndWait();

            changerScene("/fxml/menu.fxml");
        }
    }

    // ========================================================
    // GESTION DU CLAVIER ET INTERACTIONS GRILLE
    // ========================================================

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
                if (bulleAide != null && bulleAide.isVisible()) {
                    if (btnActualiserAide != null) {
                        btnActualiserAide.setVisible(true);
                        btnActualiserAide.setManaged(true);
                    }
                }
                aideService.lancerAnalyse(grilleModele);
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
            caseModeleSelectionnee.setValeur(0);
            caseModeleSelectionnee.effacerNotes();
            if (bulleAide != null && bulleAide.isVisible()) {
                if (btnActualiserAide != null) {
                    btnActualiserAide.setVisible(true);
                    btnActualiserAide.setManaged(true);
                }
            }
            aideService.lancerAnalyse(grilleModele);
            save.hist.addEtape(caseModeleSelectionnee.getX(), caseModeleSelectionnee.getY(), (modeHypotheseActif ? 20 : 0));
        }
    }

    private void genererBoutonsNombres(int taille) {
        if (conteneurBoutonsNombres != null) {
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
        if (btnHypothese != null) btnHypothese.setDisable(true); 
        if (conteneurBoutonsHypothese != null) conteneurBoutonsHypothese.setVisible(true);
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
        if (btnHypothese != null) btnHypothese.setDisable(false); 
        if (conteneurBoutonsHypothese != null) conteneurBoutonsHypothese.setVisible(false);
        for (int y = 0; y < grilleModele.getTaille(); y++) {
            for (int x = 0; x < grilleModele.getTaille(); x++) vueGrille.getGrilleVueCases(x, y).setEstHypothese(false);
        }
    }

    // ========================================================
    // AIDE & UTILITAIRES
    // ========================================================

    @FXML void actionCalculatrice(ActionEvent event) { JeuUtilitaires.afficherCalculatrice(event); }
    
    @FXML public void actionBoutonAidePointInterrogation() {
        if (!listeAides.isEmpty() && indexAideActuelle < listeAides.size()) listeAides.get(indexAideActuelle).masquer();
        listeAides.clear(); indexAideActuelle = 0;
        if (indicesEnAttente != null) {
            for (Indice ind : indicesEnAttente) listeAides.add(new CommandeAfficherIndice(ind, labelMessageAide, vueGrille));
        }
        if (listeAides.isEmpty()) return;
        
        if (btnActualiserAide != null) {
            btnActualiserAide.setVisible(false); 
            btnActualiserAide.setManaged(false);
        }
        if (bulleAide != null) bulleAide.setVisible(true); 
        listeAides.get(indexAideActuelle).afficher();
        mettreAJourBoutonsNavigation();
    }

    @FXML public void actionFermerBulleAide() {
        if (!listeAides.isEmpty()) listeAides.get(indexAideActuelle).masquer();
        if (bulleAide != null) bulleAide.setVisible(false);
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
        boolean caseIncorrecte = false;

        if (btnHypothese != null) btnHypothese.setDisable(false);
        if (conteneurBoutonsHypothese != null) conteneurBoutonsHypothese.setVisible(false);
        
        for (int y = 0; y < grilleModele.getTaille(); y++) {
            for (int x = 0; x < grilleModele.getTaille(); x++) {
                vueGrille.getGrilleVueCases(x, y).setEstHypothese(false);
                Case c = grilleModele.getCase(x, y);
                if (c.getValeur() != 0 && c.getValeur() != c.getSolution()) {
                    casesEnErreur.add(vueGrille.getGrilleVueCases(x, y));
                    caseIncorrecte = true;
                }
            }
        }

        if(caseIncorrecte && save.getDefi() == Defi.TypeDefi.SURVI) {
            save.setVies(save.getVies() - 1);
            if(save.getVies() <= 0) {
                declencherDefaite("Vous n'avez plus de vies !");
                return;
            }
        }

        for (VueCase vc : casesEnErreur) vc.getStyleClass().add("case-erreur");

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
            for (VueCase vc : casesEnErreur) vc.getStyleClass().remove("case-erreur");
        });
        pause.play();
    }

    private void mettreAJourBoutonsNavigation() {
        if (listeAides.isEmpty()) return;
        if (btnAidePrecedente != null) btnAidePrecedente.setDisable(indexAideActuelle == 0);
        if (btnAideSuivante != null) btnAideSuivante.setDisable(indexAideActuelle == listeAides.size() - 1);
        if (btnAmeliorerAide != null) btnAmeliorerAide.setDisable(!listeAides.get(indexAideActuelle).peutEtreAmeliore());
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
            String couleurTexte = MainApp.modeSombreActif ? "white" : "black";
            labelCombinaisons.setStyle("-fx-text-fill: " + couleurTexte + "; -fx-font-weight: normal;");
            labelCombinaisons.setWrapText(true);
        }
    }
}