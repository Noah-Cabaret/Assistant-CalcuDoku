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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.PauseTransition;

public class JeuController {

    @FXML private StackPane conteneurGrille;
    @FXML private HBox conteneurBoutonsNombres;
    
    @FXML private Button btnUndo, btnRedo, btnAnnoter, btnEffacer, btnCalculatrice;

    @FXML private VBox bulleAide;
    @FXML private Label labelMessageAide;
    @FXML private Button btnAmeliorerAide, btnAidePrecedente, btnAideSuivante, btnActualiserAide, btnValider, btnAide;    

    @FXML private Button btnHypothese, btnValiderHypothese, btnAnnulerHypothese;
    @FXML private HBox conteneurBoutonsHypothese;

    @FXML private VBox boiteCombinaisons;
    @FXML private Label labelCombinaisons;

    @FXML private RadioButton radioCombinaisons, radioCalculatrice;

    private boolean modeHypotheseActif = false;
     
    @FXML private Label labelChrono;
    private Timeline timeline;
    private int secondesEcoulees = 0;

    private Grille grilleModele;
    private VueGrille vueGrille;
    private boolean modeAnnotationActif = false;

    private VueCase vueCaseSelectionnee = null;
    private Case caseModeleSelectionnee = null;

    private final AideService aideService = new AideService();
    private List<CommandeAide> listeAides = new ArrayList<>();
    private List<Indice> indicesEnAttente = new ArrayList<>();
    private int indexAideActuelle = 0;

    private Sauvegarde save;
    private boolean partiePerdue = false;
    
    @FXML private VBox menuDeroulant;
    @FXML private StackPane popupAbandon;

    private EventHandler<KeyEvent> filtreClavier;

    public void initialiserPartie(Grille grille, Sauvegarde save) {
        this.grilleModele = grille;
        this.save = save;
        this.partiePerdue = false;

        conteneurGrille.setDisable(false);
        conteneurBoutonsNombres.setDisable(false);
        btnAide.setDisable(false);
        btnAide.setOpacity(1.0);

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

        aideService.setOnSucceeded(event -> { indicesEnAttente = aideService.getValue(); });
        for (GroupementCases bloc : grilleModele.getListeGroupements()) { bloc.calculerPossibilites(grilleModele); }
        aideService.lancerAnalyse(grilleModele);

        if(save.getDefi() == Defi.TypeDefi.NOAID) {
            btnAide.setDisable(true);
            btnAide.setOpacity(0.5);
        }

        ProfileManager manager = MainApp.getProfileManager();
        String nomProfil = manager.getProfilActif() != null ? manager.getProfilActif() : "Invité";
        java.util.Map<String, String> statsConfig = manager.lireStatistiques(nomProfil);
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

        if (conteneurGrille.getScene() != null) {
            conteneurGrille.getScene().removeEventFilter(KeyEvent.KEY_PRESSED, filtreClavier);
            conteneurGrille.getScene().addEventFilter(KeyEvent.KEY_PRESSED, filtreClavier);
            JeuUtilitaires.installerSecuritesFermeture(conteneurGrille.getScene(), () -> sauvegarderPartie(), () -> sauvegarderPartie());
        } else {
            conteneurGrille.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.removeEventFilter(KeyEvent.KEY_PRESSED, filtreClavier);
                    newScene.addEventFilter(KeyEvent.KEY_PRESSED, filtreClavier);
                    JeuUtilitaires.installerSecuritesFermeture(newScene, () -> sauvegarderPartie(), () -> sauvegarderPartie());
                }
            });
        }

        demarrerChrono();
    }

    private void sauvegarderPartie() {
        if (save != null && !partiePerdue && save.getIdGrille() != null && !save.getIdGrille().isEmpty()) {
            String nomProfil = MainApp.getProfileManager().getProfilActif();
            if (nomProfil == null) nomProfil = "Invité";
            
            save.enreg(nomProfil, grilleModele);
            JeuUtilitaires.sauvegarderImageGrille(grilleModele, vueGrille, vueCaseSelectionnee, save.getIdGrille() + ".png", () -> actionFermerBulleAide());
        }
    }
    
    private void deconnecterClavier() {
        if (conteneurGrille.getScene() != null && filtreClavier != null) {
            conteneurGrille.getScene().removeEventFilter(KeyEvent.KEY_PRESSED, filtreClavier);
        }
    }

    private void demarrerChrono() {
        if (save != null && save.tmp != null && save.tmp.getTempsPrecedent() != null) {
            secondesEcoulees = save.tmp.getTempsPrecedent().intValue();
        } else { secondesEcoulees = 0; }

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (partiePerdue) return;
            secondesEcoulees++;
            
            if(save.getDefi() == Defi.TypeDefi.CHRON) {
                int restant = Math.max(0, save.tmp.getTempsMax().intValue() - secondesEcoulees);
                labelChrono.setText(String.format("%02d:%02d", restant / 60, restant % 60));
                if(restant <= 0) declencherDefaite("Le temps est écoulé !");
            } else {
                labelChrono.setText(String.format("%02d:%02d", secondesEcoulees / 60, secondesEcoulees % 60));
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        save.tmp.lancer();
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

    // --- NOUVEAU : Gestion des Défaites ---
    private void declencherDefaite(String message) {
        if (partiePerdue) return;
        partiePerdue = true;
        
        if (timeline != null) timeline.stop();
        conteneurGrille.setDisable(true);
        conteneurBoutonsNombres.setDisable(true);
        deconnecterClavier(); 
        
        String nomProfil = MainApp.getProfileManager().getProfilActif();
        if (nomProfil == null) nomProfil = "Invité";

        // Enregistrer la défaite dans les statistiques
        MainApp.getProfileManager().enregistrerFinDePartie(nomProfil, false, secondesEcoulees, 0, save.getDiff(), false);
        
        if (save != null) save.effacer(nomProfil);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Défaite");
        alert.setHeaderText("Défi échoué !");
        alert.setContentText(message);
        alert.showAndWait();

        actionRetourMenu(null);
    }
    
    // --- NOUVEAU : Détection des Victoires ---
    private void verifierVictoire() {
        boolean complet = true;
        for (int y = 0; y < grilleModele.getTaille(); y++) {
            for (int x = 0; x < grilleModele.getTaille(); x++) {
                Case c = grilleModele.getCase(x, y);
                // Si la case est vide ou erronée, ce n'est pas encore gagné
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

            // Plus la grille est grande et plus on est rapide, meilleur est le score (Minimum 0)
            long scoreCalcul = Math.max(0, (grilleModele.getTaille() * 1000) - (secondesEcoulees * 10));
            boolean estAventure = (save.getMode() == Sauvegarde.ModeDeJeu.AVEN);

            // Enregistrer la victoire et le score
            MainApp.getProfileManager().enregistrerFinDePartie(nomProfil, true, secondesEcoulees, scoreCalcul, save.getDiff(), estAventure);

            // Supprimer la sauvegarde car elle est terminée
            if (save != null) save.effacer(nomProfil);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Victoire !");
            alert.setHeaderText("Grille complétée !");
            alert.setContentText("Félicitations, vous avez terminé la grille en " + (secondesEcoulees / 60) + " min " + (secondesEcoulees % 60) + " s.\nScore obtenu : " + scoreCalcul);
            alert.showAndWait();

            actionRetourMenu(null);
        }
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
                rafraichirAnnotations(caseModeleSelectionnee.getX(), caseModeleSelectionnee.getY(), valeur);
                rafraichirZoneCombinaisons(caseModeleSelectionnee);
                if (bulleAide.isVisible()) {
                    btnActualiserAide.setVisible(true);
                    btnActualiserAide.setManaged(true);
                }
                aideService.lancerAnalyse(grilleModele);
                
                // --- ON VÉRIFIE LA VICTOIRE À CHAQUE CHIFFRE POSÉ ---
                verifierVictoire();
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
            if (bulleAide.isVisible()) {
                btnActualiserAide.setVisible(true);
                btnActualiserAide.setManaged(true);
            }
            aideService.lancerAnalyse(grilleModele);
            save.hist.addEtape(caseModeleSelectionnee.getX(), caseModeleSelectionnee.getY(), (modeHypotheseActif ? 20 : 0));
        }
    }

    @FXML void actionBoutonAidePointInterrogation() {
        if (!listeAides.isEmpty() && indexAideActuelle < listeAides.size()) listeAides.get(indexAideActuelle).masquer();
        listeAides.clear();
        indexAideActuelle = 0;

        if (indicesEnAttente != null) {
            for (Indice ind : indicesEnAttente) {
                listeAides.add(new CommandeAfficherIndice(ind, labelMessageAide, vueGrille));
            }
        }
        if (listeAides.isEmpty()) return;

        btnActualiserAide.setVisible(false);
        btnActualiserAide.setManaged(false);
        bulleAide.setVisible(true);
        listeAides.get(indexAideActuelle).afficher();
        mettreAJourBoutonsNavigation();
    }

    @FXML void actionFermerBulleAide() {
        if (!listeAides.isEmpty()) listeAides.get(indexAideActuelle).masquer();
        bulleAide.setVisible(false);
    }

    @FXML void actionAmeliorerAide() {
        if (!listeAides.isEmpty()) {
            listeAides.get(indexAideActuelle).ameliorerNiveau();
            mettreAJourBoutonsNavigation();
        }
    }

    @FXML void actionAideSuivante() {
        if (indexAideActuelle < listeAides.size() - 1) {
            listeAides.get(indexAideActuelle).masquer();
            indexAideActuelle++;
            listeAides.get(indexAideActuelle).afficher();
            mettreAJourBoutonsNavigation();
        }
    }

    @FXML void actionAidePrecedente() {
        if (indexAideActuelle > 0) {
            listeAides.get(indexAideActuelle).masquer();
            indexAideActuelle--;
            listeAides.get(indexAideActuelle).afficher();
            mettreAJourBoutonsNavigation();
        }
    }

    @FXML void actionVerifier(ActionEvent event) {
        List<VueCase> casesEnErreur = new ArrayList<>();
        int taille = grilleModele.getTaille();
        boolean caseIncorrecte = false;

        for (int y = 0; y < taille; y++) {
            for (int x = 0; x < taille; x++) {
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
        btnAidePrecedente.setDisable(indexAideActuelle == 0);
        btnAideSuivante.setDisable(indexAideActuelle == listeAides.size() - 1);
        btnAmeliorerAide.setDisable(!listeAides.get(indexAideActuelle).peutEtreAmeliore());
    }

    @FXML void actionHypothese(ActionEvent event) {
        modeHypotheseActif = true;
        btnHypothese.setDisable(true); 
        conteneurBoutonsHypothese.setVisible(true);
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
        btnHypothese.setDisable(false);
        conteneurBoutonsHypothese.setVisible(false);
        for (int y = 0; y < grilleModele.getTaille(); y++) {
            for (int x = 0; x < grilleModele.getTaille(); x++) {
                vueGrille.getGrilleVueCases(x, y).setEstHypothese(false);
            }
        }
    }

    @FXML void actionUndo(ActionEvent event) { save.hist.appliquerUndo(grilleModele, modeHypotheseActif); }
    @FXML void actionRedo(ActionEvent event) { save.hist.appliquerRedo(grilleModele); }

    @FXML void actionCalculatrice(ActionEvent event) { 
        JeuUtilitaires.afficherCalculatrice(event);
    }
    
    @FXML void actionRetourMenu(ActionEvent event) {
        sauvegarderPartie();
        deconnecterClavier(); // On coupe le clavier en quittant
        if (timeline != null) timeline.stop();
        JeuUtilitaires.cacherCalculatrice();
        MainApp.changerScene("/fxml/menu.fxml");
    }

    private void rafraichirZoneCombinaisons(Case modeleCase) {
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
            labelCombinaisons.setStyle("-fx-text-fill: black; -fx-font-weight: normal;");
            labelCombinaisons.setWrapText(true);
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
    
    @FXML 
    void actionBasculerMenu(ActionEvent event) {
        if (menuDeroulant != null) {
            menuDeroulant.setVisible(!menuDeroulant.isVisible());
        }
    }

    @FXML 
    void actionAbandonner(ActionEvent event) {
        menuDeroulant.setVisible(false);
        if (popupAbandon != null) popupAbandon.setVisible(true);
    }

    @FXML 
    void actionAnnulerAbandon(ActionEvent event) {
        if (popupAbandon != null) popupAbandon.setVisible(false);
    }

    @FXML 
    void actionConfirmerAbandon(ActionEvent event) {
        if (popupAbandon != null) popupAbandon.setVisible(false);
        
        // --- NOUVEAU : Enregistrer l'abandon comme une défaite ! ---
        String nomProfil = MainApp.getProfileManager().getProfilActif();
        if (nomProfil == null) nomProfil = "Invité";
        MainApp.getProfileManager().enregistrerFinDePartie(nomProfil, false, secondesEcoulees, 0, save.getDiff(), false);
        
        partiePerdue = true; 
        if (save != null) save.effacer(nomProfil);
        actionRetourMenu(event);
    }

    @FXML 
    void actionRecommencer(ActionEvent event) {
        menuDeroulant.setVisible(false);
        
        for (int y = 0; y < grilleModele.getTaille(); y++) {
            for (int x = 0; x < grilleModele.getTaille(); x++) {
                Case c = grilleModele.getCase(x, y);
                c.setValeur(0);
                c.effacerNotes();
            }
        }
        
        save.hist = new fr.univ.calcudoku.save.Historique();
        if (timeline != null) timeline.stop();
        save.tmp.setTempsPrecedent(0.0);
        
        demarrerChrono();
        aideService.lancerAnalyse(grilleModele);
    }

    @FXML 
    void actionReglesTechniques(ActionEvent event) {
        sauvegarderPartie();
        deconnecterClavier(); 
        menuDeroulant.setVisible(false);
        if (timeline != null) timeline.pause(); 
        
        String nomProfil = MainApp.getProfileManager().getProfilActif();
        if (nomProfil == null) nomProfil = "Invité";
        
        String sousDossier = (save.getMode() == Sauvegarde.ModeDeJeu.AVEN) ? "aventure/" : "";
        java.io.File fichierSave = new java.io.File("profils/" + nomProfil + "/parties/" + sousDossier + save.getIdGrille() + ".json");
        
        javafx.stage.Stage stage = (javafx.stage.Stage) conteneurGrille.getScene().getWindow();
        
        ReglesTechniquesController.actionRetour = () -> {
            fr.univ.calcudoku.utils.GestionnaireJeu.chargerPartieDepuisFichier(stage, fichierSave);
        };

        MainApp.changerScene("/fxml/reglesTechniques.fxml");
    }
    
    private void appliquerModeAide(String mode) {
        if (mode.equals("calculatrice")) {
            boiteCombinaisons.setVisible(false);
            boiteCombinaisons.setManaged(false); 
            btnCalculatrice.setVisible(true);
            btnCalculatrice.setManaged(true);
        } else {
            boiteCombinaisons.setVisible(true);
            boiteCombinaisons.setManaged(true);
            btnCalculatrice.setVisible(false);
            btnCalculatrice.setManaged(false);
        }
    }
}