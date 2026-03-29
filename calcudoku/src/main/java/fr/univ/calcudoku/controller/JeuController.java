package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.challenge.Defi;
import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import fr.univ.calcudoku.MainApp;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.PauseTransition;

import fr.univ.calcudoku.save.*;

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
    @FXML private Button btnActualiserAide;

    @FXML private Button btnValider; // Bouton ✔ à droite de la grille
    @FXML private Button btnAide;    // Bouton ? à droite de la grille

    @FXML private Button btnHypothese;
    @FXML private HBox conteneurBoutonsHypothese;
    @FXML private Button btnValiderHypothese;
    @FXML private Button btnAnnulerHypothese;

    @FXML private VBox boiteCombinaisons;
    @FXML private Label labelCombinaisons;

    private boolean modeHypotheseActif = false;
     
    @FXML private Label labelChrono;
    private Timeline timeline;
    private int secondesEcoulees = 0;
    private boolean tempsEcoule = false;

    private Grille grilleModele;
    private VueGrille vueGrille;
    private boolean modeAnnotationActif = false;

    private VueCase vueCaseSelectionnee = null;
    private Case caseModeleSelectionnee = null;

    private javafx.stage.Popup calcPopup;
    private double xOffset = 0;
    private double yOffset = 0;

    private final AideService aideService = new AideService();
    private List<CommandeAide> listeAides = new ArrayList<>();
    private List<Indice> indicesEnAttente = new ArrayList<>();
    private int indexAideActuelle = 0;

    private Sauvegarde save;
    private boolean partiePerdue = false;

    public void initialiserPartie(Grille grille, Sauvegarde save) {
        this.grilleModele = grille;

        this.save = save;
        // Il manque la sélection de la grille pour initialiser la sauvegarde (mode de jeu, id grille) donc le chargement est en commentaire pour l'instant
        //save.charger(MainApp.getProfileManager().getProfilActif(), grille);

        this.vueGrille = new VueGrille(grille);

        conteneurGrille.getChildren().clear(); 
        conteneurGrille.getChildren().add(vueGrille);
        
        // Gestion de la taille de la grille
        NumberBinding tailleCarree = Bindings.min(conteneurGrille.widthProperty(), conteneurGrille.heightProperty());
        vueGrille.prefWidthProperty().bind(tailleCarree);
        vueGrille.prefHeightProperty().bind(tailleCarree);
        vueGrille.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        vueGrille.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        // Initialisation des cases
        for (int y = 0; y < grille.getTaille(); y++) {
            for (int x = 0; x < grille.getTaille(); x++) {
                VueCase vc = vueGrille.getGrilleVueCases(x, y);
                final Case modeleCase = grille.getCase(x, y);
                vc.setOnMouseClicked(event -> selectionnerCase(vc, modeleCase));
            }
        }
        
        genererBoutonsNombres(grille.getTaille());
        bulleAide.setVisible(false);

        // Service d'aide en arrière-plan
        aideService.setOnSucceeded(event -> {
            indicesEnAttente = aideService.getValue();
        });

        for (fr.univ.calcudoku.model.GroupementCases bloc : grilleModele.getListeGroupements()) {
            bloc.calculerPossibilites(grilleModele);
        }
        
        aideService.lancerAnalyse(grilleModele);

        if(save.getDefi() == Defi.TypeDefi.NOAID)
            btnAide.setDisable(true);

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
            int minutes, secondes;
            if(save.getDefi() == Defi.TypeDefi.CHRON)
            {
                minutes = (int)((save.tmp.getTempsMax() - secondesEcoulees) / 60);
                secondes = (int)((save.tmp.getTempsMax() - secondesEcoulees) % 60);
                if(secondesEcoulees % 60 == 0 && secondes >= 0)
                    save.setMalus(save.getMalus() + 2);
                if(secondes <= 0)
                {
                    tempsEcoule = true;
                    secondes = 0;
                }

            }
            else
            {
                minutes = secondesEcoulees / 60;
                secondes = secondesEcoulees % 60;
            }

            if(tempsEcoule && !partiePerdue)
            {
                System.out.println("DÉFAITE: Temps écoulé");
                partiePerdue = true;
            }
            labelChrono.setText(String.format("%02d:%02d — Score : %d", minutes, secondes, save.calculerScore()));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        save.tmp.lancer();
    }

    public void sauvegarderImageGrille(String nomFichier) {
        try {
            String nomJoueur = MainApp.getProfileManager().getProfilActif();
            if (nomJoueur == null) nomJoueur = "Invité";

            File dossierImages = new File("profils/" + nomJoueur + "/jeu/images");
            if (!dossierImages.exists()) { dossierImages.mkdirs(); }

            String nomImage = nomFichier.endsWith(".png") ? nomFichier : nomFichier.replace(".json", "") + ".png";
            File fichierFinal = new File(dossierImages, nomImage);

            // Nettoyage visuel pour la capture
            actionFermerBulleAide(); 
            if (vueCaseSelectionnee != null) vueCaseSelectionnee.getStyleClass().remove("case-selectionnee");
            
            vueGrille.setStyle("-fx-background-color: white;");

            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT); 
            WritableImage image = vueGrille.snapshot(params, null);
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", fichierFinal);
            
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void selectionnerCase(VueCase vueCase, Case modeleCase) {
        if (vueCaseSelectionnee != null) {
            vueCaseSelectionnee.getStyleClass().remove("case-selectionnee");
        }
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
                // Si on modifie la grille, on propose d'actualiser l'aide
                if (bulleAide.isVisible()) {
                    btnActualiserAide.setVisible(true);
                    btnActualiserAide.setManaged(true);
                }
                aideService.lancerAnalyse(grilleModele);
            }
        }
    }

    @FXML
    void actionBasculeAnnotation(ActionEvent event) {
        modeAnnotationActif = !modeAnnotationActif;
        btnAnnoter.setStyle(modeAnnotationActif ? "-fx-background-color: #bbdefb;" : "");
    }

    @FXML
    void actionEffacer(ActionEvent event) {
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

    @FXML
    public void actionBoutonAidePointInterrogation() {
        if (!listeAides.isEmpty() && indexAideActuelle < listeAides.size()) {
            listeAides.get(indexAideActuelle).masquer();
        }

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

    @FXML
    void actionVerifier(ActionEvent event) {
        List<VueCase> casesEnErreur = new ArrayList<>();
        int taille = grilleModele.getTaille();
        boolean caseIncorrecte = false;

        for (int y = 0; y < taille; y++) {
            for (int x = 0; x < taille; x++) {
                Case c = grilleModele.getCase(x, y);
                if (c.getValeur() != 0 && c.getValeur() != c.getSolution()) {
                    casesEnErreur.add(vueGrille.getGrilleVueCases(x, y));
                    save.setMalus(save.getMalus() + 1);
                    if(save.getDefi() == Defi.TypeDefi.SURVI)
                        caseIncorrecte = true;
                    c.setValidee(false);
                }
                else if(c.getValeur() != 0)
                {
                    if(!c.getValidee())
                    {
                        save.setBonus(save.getBonus() + 1);
                        c.setValidee(true);
                    }
                }
            }
        }

        if(caseIncorrecte)
        {
            save.setVies(save.getVies() - 1);
            if(save.getVies() == 0 && !partiePerdue)
            {
                System.out.println("DÉFAITE: À court de vies");
                partiePerdue = true;
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

    @FXML
    void actionHypothese(ActionEvent event) {
        modeHypotheseActif = true;
        btnHypothese.setDisable(true); 
        conteneurBoutonsHypothese.setVisible(true);
    }

    @FXML
    void actionValiderHypothese(ActionEvent event)
    {
        quitterModeHypotheseVisuel();
        validerHypothese();
    }

    @FXML
    void actionAnnulerHypothese(ActionEvent event)
    {
        quitterModeHypotheseVisuel();
        rollbackHypothese();
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
    
    /* Isolement du undo pour l'utiliser à la fois pour le bouton undo et pour le rollback */
    void undo()
    {
        if(save.hist.getIndex() > 0)
        {
            Etape etapeCourante = save.hist.getEtapeCourante();
            Etape etapePrecedente;
            int i = save.hist.getIndex();
            boolean etapeExiste = false;
            do
                etapePrecedente = save.hist.precedent();
            while((save.hist.getIndex() > 1) && !(etapeCourante.getX() == etapePrecedente.getX() && etapeCourante.getY() == etapePrecedente.getY()));

            if(etapeCourante.getX() == etapePrecedente.getX() && etapeCourante.getY() == etapePrecedente.getY())
                etapeExiste = true;

            save.hist.setIndex(i - 1);

            if(etapeCourante.normale())
            {
                if(modeHypotheseActif)
                    save.hist.suivant();
                else
                {
                    if(etapeExiste)
                    {
                        if(etapePrecedente.annotation())
                        {
                            List<Integer> valeursNote = new ArrayList<Integer>();
                            i = save.hist.getIndex();
                            Case caseCourante = grilleModele.getCase(etapeCourante.getX(), etapeCourante.getY());
                            etapeCourante = save.hist.getEtapeCourante();
                            while(save.hist.getIndex() > 0 && etapeCourante.getN() != 0)
                            {
                                if(etapeCourante.annotation())
                                    valeursNote.add(etapeCourante.getN());
                                etapeCourante = save.hist.precedent();
                            }
                            save.hist.setIndex(i);
                            if(valeursNote.size() > 0)
                            {
                                caseCourante.setValeur(0);
                                caseCourante.effacerNotes();
                                for(Integer note : valeursNote)
                                    caseCourante.basculerNote(note - 10);
                            }
                        }
                        else
                            grilleModele.getCase(etapePrecedente.getX(), etapePrecedente.getY()).setValeur(etapePrecedente.getN());
                    }
                    else
                        grilleModele.getCase(etapeCourante.getX(), etapeCourante.getY()).setValeur(0);
                }
            }
            else if(etapeCourante.annotation())
            {
                if(!modeHypotheseActif)
                    grilleModele.getCase(etapeCourante.getX(), etapeCourante.getY()).basculerNote(etapeCourante.getN() - 10);
                else
                    save.hist.suivant();
            }
            else if(etapeCourante.hypotheseNormale())
            {
                if(etapeExiste)
                {
                    if(etapePrecedente.annotation() || etapePrecedente.hypotheseAnnotation())
                    {
                        List<Integer> valeursNote = new ArrayList<Integer>();
                        i = save.hist.getIndex();
                        Case caseCourante = grilleModele.getCase(etapeCourante.getX(), etapeCourante.getY());
                        etapeCourante = save.hist.getEtapeCourante();
                        while(save.hist.getIndex() > 0 && etapeCourante.getN() != 0)
                        {
                            if(etapeCourante.annotation() || etapeCourante.hypotheseAnnotation())
                                valeursNote.add(etapeCourante.getN());
                            etapeCourante = save.hist.precedent();
                        }
                        save.hist.setIndex(i);
                        if(valeursNote.size() > 0)
                        {
                            caseCourante.setValeur(0);
                            caseCourante.effacerNotes();
                            for(Integer note : valeursNote)
                                caseCourante.basculerNote(note % 10);
                        }
                    }
                    else
                        grilleModele.getCase(etapePrecedente.getX(), etapePrecedente.getY()).setValeur(etapePrecedente.getN() - (etapePrecedente.hypotheseNormale() ? 20 : 0));
                }
                else
                    grilleModele.getCase(etapeCourante.getX(), etapeCourante.getY()).setValeur(0);
            }
            else if(etapeCourante.hypotheseAnnotation())
                grilleModele.getCase(etapeCourante.getX(), etapeCourante.getY()).basculerNote(etapeCourante.getN() - 30);
        }
    }

    void validerHypothese()
    {
        Etape etapeCourante = save.hist.getEtapeCourante();
        while(save.hist.getIndex() > 0 && etapeCourante.hypotheseNormale())
        {
            etapeCourante.setN(etapeCourante.getN() - 20);
            etapeCourante = save.hist.precedent();
        }
        save.hist.setIndex(save.hist.taille() - 1);
    }

    void rollbackHypothese()
    {
        while(save.hist.getIndex() > 0 && (save.hist.getEtapeCourante().hypotheseNormale() || save.hist.getEtapeCourante().hypotheseAnnotation()))
            undo();
        save.hist.viderQueue();
    }

    // Stubs pour Undo/Redo/Calculatrice
    @FXML void actionUndo(ActionEvent event)
    {
        undo();
    }

    @FXML void actionRedo(ActionEvent event)
    {
        if(save.hist.getIndex() < save.hist.taille() - 1)
        {
            Etape etapeSuivante = save.hist.suivant();
            if(etapeSuivante.normale())
            {
                Case caseCourante = grilleModele.getCase(etapeSuivante.getX(), etapeSuivante.getY());
                caseCourante.effacerNotes();
                caseCourante.setValeur(etapeSuivante.getN());
            }
            else if(etapeSuivante.annotation())
                grilleModele.getCase(etapeSuivante.getX(), etapeSuivante.getY()).basculerNote(etapeSuivante.getN() - 10);
            else if(etapeSuivante.hypotheseNormale())
            {
                Case caseCourante = grilleModele.getCase(etapeSuivante.getX(), etapeSuivante.getY());
                caseCourante.effacerNotes();
                caseCourante.setValeur(etapeSuivante.getN() - 20);
            }
            else if(etapeSuivante.hypotheseAnnotation())
                grilleModele.getCase(etapeSuivante.getX(), etapeSuivante.getY()).basculerNote(etapeSuivante.getN() - 30);
        }
    }
    @FXML void actionCalculatrice(ActionEvent event) { 
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
    private void rafraichirZoneCombinaisons(Case modeleCase) {
        if (modeleCase == null || modeleCase.getGroupement() == null) {
            labelCombinaisons.setText("Selectionnez une case");
            return;
        }

        GroupementCases group = modeleCase.getGroupement();
        group.calculerPossibilites(this.grilleModele);
        List<List<Integer>> combis = group.getCombinaisonsMaths();

        /* debug */
        System.out.println("Combinaisons trouvées pour " + group.getResultatCible() + " : " + combis.size());
        if (combis.isEmpty()) {
            labelCombinaisons.setText("Aucune combinaison possible !");
            labelCombinaisons.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(group.getResultatCible()).append(" ").append(group.getOperation().getSymbole()).append(" :\n");

            for (int i = 0; i < combis.size(); i++){
                sb.append(combis.get(i).toString());
                if(i < combis.size() - 1)
                    sb.append(" | ");
            }

            labelCombinaisons.setText(sb.toString());
            labelCombinaisons.setStyle("-fx-text-fill: black; -fx-font-weight: normal;");

            labelCombinaisons.setWrapText(true);
        }
    }
}
