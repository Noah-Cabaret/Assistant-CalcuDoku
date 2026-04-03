package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.challenge.Defi;
import fr.univ.calcudoku.model.Case;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.model.GroupementCases;
import fr.univ.calcudoku.model.Indice;
import fr.univ.calcudoku.commande.CommandeAide;
import fr.univ.calcudoku.commande.CommandeAfficherIndice;
import fr.univ.calcudoku.service.ProfileManager;
import fr.univ.calcudoku.service.ValidateurJeu;
import fr.univ.calcudoku.service.aide.AideService;
import fr.univ.calcudoku.view.VueCase;
import fr.univ.calcudoku.view.VueGrille;
import fr.univ.calcudoku.utils.ChronoManager;
import fr.univ.calcudoku.utils.Constantes;
import fr.univ.calcudoku.utils.PopupFactory;
import fr.univ.calcudoku.utils.JeuUtilitaires;
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
import javafx.scene.paint.Color;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Contrôleur principal de la vue du jeu Calcudoku.
 * Gère l'interaction utilisateur avec la grille, les boutons de contrôle,
 * les aides, la sauvegarde et la logique de victoire/défaite.
 */
public class JeuController {

    /** Conteneur principal de la grille de jeu. */
    @FXML private StackPane conteneurGrille;
    /** Conteneur des boutons numériques (1 à N) pour la saisie. */
    @FXML private HBox conteneurBoutonsNombres;
    /** Bouton pour annuler la dernière action. */
    @FXML private Button btnUndo, btnRedo, btnAnnoter, btnEffacer, btnCalculatrice;
    /** Bulle d'aide contextuelle. */
    @FXML private VBox bulleAide;
    /** Label affichant le message de l'aide. */
    @FXML private Label labelMessageAide;
    /** Boutons de contrôle de l'aide. */
    @FXML private Button btnAmeliorerAide, btnAidePrecedente, btnAideSuivante, btnActualiserAide, btnValider, btnAide;    
    /** Boutons pour la gestion des hypothèses. */
    @FXML private Button btnHypothese, btnValiderHypothese, btnAnnulerHypothese;
    /** Boutons de navigation. */
    @FXML private Button btnRetour, btnMenu;
    /** Conteneur des boutons d'hypothèse. */
    @FXML private HBox conteneurBoutonsHypothese;
    /** Boîte affichant les combinaisons possibles pour une cage. */
    @FXML private VBox boiteCombinaisons;
    /** Label affichant les combinaisons. */
    @FXML private Label labelCombinaisons;
    /** Bouton radio pour l'aide "Combinaisons". */
    @FXML private RadioButton radioCombinaisons, radioCalculatrice;
    /** Label affichant le chronomètre. */
    @FXML private Label labelChrono;
    /** Label affichant le défi en cours. */
    @FXML private Label labelDefi;
    /** Menu déroulant (hamburger menu). */
    @FXML private VBox menuDeroulant;
    /** Popup de confirmation d'abandon de partie. */
    @FXML private StackPane popupAbandon;

    /** Modèle de la grille de jeu. */
    private Grille grilleModele;
    /** Vue graphique de la grille. */
    private VueGrille vueGrille;
    /** Objet de sauvegarde de la partie. */
    private Sauvegarde save;
    /** Gestionnaire du chronomètre. */
    private ChronoManager chronoManager; 

    /** Indique si le mode hypothèse est actif. */
    private boolean modeHypotheseActif = false;
    /** Indique si le mode annotation est actif. */
    private boolean modeAnnotationActif = false;
    /** Indique si la partie est perdue (pour éviter les déclenchements multiples). */
    private boolean partiePerdue = false;
    
    /** La vue de la case actuellement sélectionnée. */
    private VueCase vueCaseSelectionnee = null;
    /** Le modèle de la case actuellement sélectionnée. */
    private Case caseModeleSelectionnee = null;
    
    /** Service d'aide pour trouver des indices. */
    private final AideService aideService = new AideService();
    /** Liste des commandes d'aide à afficher. */
    private List<CommandeAide> listeAides = new ArrayList<>();
    /** Liste des indices en attente d'affichage. */
    private List<Indice> indicesEnAttente = new ArrayList<>();
    /** Index de l'aide actuellement affichée. */
    private int indexAideActuelle = 0;
    
    /** Filtre d'événements clavier pour la navigation et la saisie. */
    private EventHandler<KeyEvent> filtreClavier;
    /** Filtre d'événements clavier pour les raccourcis (ex: annotation). */
    private EventHandler<KeyEvent> raccourcisClavier;

    /**
     * Initialise une nouvelle partie de jeu.
     * Cette méthode est appelée par le GestionnaireJeu pour configurer la grille
     * et l'interface utilisateur avant le début de la partie.
     *
     * @param grille Le modèle de la grille de jeu à utiliser.
     * @param save L'objet de sauvegarde associé à cette partie.
     */
    public void initialiserPartie(Grille grille, Sauvegarde save) {
        this.grilleModele = grille;
        this.save = save;
        this.partiePerdue = false;

        // Active les contrôles de la grille et des boutons
        conteneurGrille.setDisable(false);
        conteneurBoutonsNombres.setDisable(false);
        if (btnAide != null) {
            btnAide.setDisable(false);
            btnAide.setOpacity(1.0);
        }

        if (this.save != null && (this.save.getIdGrille() == null || this.save.getIdGrille().isEmpty())) {
            this.save.setIdGrille("libre_" + grille.getTaille() + "_1_1"); 
        }

        // Crée et configure la vue de la grille
        this.vueGrille = new VueGrille(grille);
        conteneurGrille.getChildren().clear(); 
        conteneurGrille.getChildren().add(vueGrille);
        
        NumberBinding tailleCarree = Bindings.min(conteneurGrille.widthProperty(), conteneurGrille.heightProperty());
        // Lie la taille de la vue de la grille à la taille du conteneur
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
        
        // Initialise les styles et l'affichage
        initialiserClassesCSS(); 
        appliquerModeSombre(); 
        genererBoutonsNombres(grille.getTaille());
        conteneurBoutonsNombres.applyCss();
        conteneurBoutonsNombres.layout();
        
        // Cache les éléments de popup et de menu au démarrage
        if (popupAbandon != null) popupAbandon.setVisible(false);
        if (menuDeroulant != null) menuDeroulant.setVisible(false);
        if (bulleAide != null) bulleAide.setVisible(false);
        
        if (btnActualiserAide != null) {
            btnActualiserAide.setOnAction(this::actionActualiserAide);
        }

        // Configure le service d'aide
        aideService.setOnSucceeded(event -> { 
            indicesEnAttente = aideService.getValue(); 
            if (bulleAide != null && bulleAide.isVisible()) {
                rafraichirContenuBulleAide();
            }
        });
        // Calcule les possibilités initiales des groupements
        for (GroupementCases bloc : grilleModele.getListeGroupements()) { 
            bloc.calculerPossibilites(grilleModele); 
        }
        aideService.lancerAnalyse(grilleModele);

        // Gère le défi "NOAID" (aucune aide)
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

        // Configure les gestionnaires d'événements clavier
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

        // Installe les filtres clavier et les sécurités de fermeture
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

        // Met à jour le label du défi et démarre le chronomètre
        mettreAJourLabelDefi();
        
        this.chronoManager = new ChronoManager(labelChrono, save, () -> declencherDefaite("Le temps est écoulé !"));
        this.chronoManager.demarrer();
    }

    /**
     * Initialise les classes CSS pour les éléments de l'interface utilisateur.
     * Cela permet d'appliquer des styles prédéfinis et de gérer les changements de thème.
     */
    private void initialiserClassesCSS() {
        if (btnValider != null) { btnValider.setStyle(""); btnValider.getStyleClass().add("bouton-rond"); }
        if (btnAide != null) { btnAide.setStyle(""); btnAide.getStyleClass().add("bouton-rond"); }
        if (btnHypothese != null) { btnHypothese.setStyle(""); btnHypothese.getStyleClass().addAll("bouton-rond", "bouton-hypothese"); }
        if (btnValiderHypothese != null) { btnValiderHypothese.setStyle(""); btnValiderHypothese.getStyleClass().add("bouton-rond-petit"); }
        if (btnAnnulerHypothese != null) { btnAnnulerHypothese.setStyle(""); btnAnnulerHypothese.getStyleClass().add("bouton-rond-petit"); }
        
        if (menuDeroulant != null) { 
            menuDeroulant.setStyle(""); 
            menuDeroulant.getStyleClass().add("menu-deroulant"); 
            for (javafx.scene.Node n : menuDeroulant.getChildren()) {
                n.setStyle(""); 
            }
        }
        if (boiteCombinaisons != null) { boiteCombinaisons.setStyle(""); boiteCombinaisons.getStyleClass().add("boite-combinaisons"); }
    }

    /**
     * Applique le thème sombre ou clair à l'interface utilisateur.
     * Met à jour les couleurs des textes, fonds, bordures et icônes.
     */
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

            String couleurTexte = sombre ? "white" : "black";
            String couleurFond = sombre ? "#2b2b2b" : "white";
            String couleurBordure = sombre ? "#888888" : "black";
            String couleurMenuFond = sombre ? "#3b3b3b" : "white"; 
            String couleurBtnMenu = sombre ? "#4a4a4a" : "#f4f4f4";
            
            if (labelChrono != null) labelChrono.setStyle("-fx-text-fill: " + couleurTexte + ";");
            if (labelCombinaisons != null) labelCombinaisons.setStyle("-fx-text-fill: " + couleurTexte + ";");
            if (boiteCombinaisons != null) boiteCombinaisons.setStyle("-fx-background-color: " + couleurFond + "; -fx-border-color: " + couleurBordure + "; -fx-border-width: 1px; -fx-padding: 10px;");
            
            String couleurMenu = "-fx-background-color: " + couleurMenuFond + "; -fx-border-color: " + couleurBordure + "; -fx-border-width: 1px; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-padding: 15px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 5);";
            if (menuDeroulant != null) {
                menuDeroulant.setStyle(couleurMenu);
                for (javafx.scene.Node node : menuDeroulant.getChildren()) {
                    if (node instanceof Button) {
                        node.setStyle("-fx-background-color: " + couleurBtnMenu + "; -fx-text-fill: " + couleurTexte + "; -fx-border-color: " + couleurBordure + "; -fx-cursor: hand; -fx-padding: 8px; -fx-font-weight: bold;");
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
            Button[] boutonsAvecIcones = {btnRetour, btnMenu, btnValider, btnAide, btnValiderHypothese, btnAnnulerHypothese, btnUndo, btnRedo, btnAnnoter, btnEffacer, btnCalculatrice};
            
            for (Button btn : boutonsAvecIcones) {
                if (btn != null && btn.getGraphic() instanceof FontIcon) {
                    ((FontIcon) btn.getGraphic()).setIconColor(iconColor);
                }
            }

            if (popupAbandon != null && !popupAbandon.getChildren().isEmpty()) {
                VBox boitePopup = (VBox) popupAbandon.getChildren().get(0);
                boitePopup.setStyle("-fx-background-color: " + couleurMenuFond + "; -fx-background-radius: 12px; -fx-padding: 20px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 15, 0, 0, 0);");
                
                for (javafx.scene.Node n : boitePopup.getChildren()) {
                    if (n instanceof Label) {
                        Label lbl = (Label) n;
                        if (lbl.getText().contains("Abandonner")) {
                            lbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + couleurTexte + ";");
                        } else if (lbl.getText().contains("progression")) {
                            lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #ff6b6b;");
                        }
                    } else if (n instanceof javafx.scene.layout.HBox) {
                        javafx.scene.layout.HBox boxBoutons = (javafx.scene.layout.HBox) n;
                        if (!boxBoutons.getChildren().isEmpty() && boxBoutons.getChildren().get(0) instanceof Button) {
                            Button btnAnnuler = (Button) boxBoutons.getChildren().get(0);
                            String couleurBtnAnnuler = sombre ? "#555555" : "#f5f5f5";
                            btnAnnuler.setStyle("-fx-background-color: " + couleurBtnAnnuler + "; -fx-border-color: " + couleurBordure + "; -fx-text-fill: " + couleurTexte + "; -fx-cursor: hand; -fx-min-width: 90px;");
                        }
                    }
                }
            }
        });
    }

    /**
     * Met à jour le label affichant le défi en cours (si applicable).
     */
    private void mettreAJourLabelDefi() {
        if (labelDefi == null || save == null) return;

        labelDefi.setManaged(true);
        labelDefi.setVisible(true);

        switch (save.getDefi()) {
            case CHRON:
                labelDefi.setText("Défi : Contre la montre");
                break;
            case NOAID:
                labelDefi.setText("Défi : Aucune aide disponible");
                break;
            case SURVI:
                labelDefi.setText("Défi : Mode Survie, il vous reste " + save.getVies() + " vie(s)");
                break;
            default:
                labelDefi.setVisible(false);
                labelDefi.setManaged(false); 
                break;
        }
    }

    /**
     * Applique le mode d'aide sélectionné (Combinaisons ou Calculatrice).
     * @param mode La chaîne de caractères représentant le mode d'aide (Constantes.VALEUR_AIDE_CALCULATRICE ou Constantes.VALEUR_AIDE_COMBINAISONS).
     */
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

    /**
     * Génère dynamiquement les boutons numériques (1 à N) en fonction de la taille de la grille.
     * @param taille La taille de la grille (N).
     */
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

    /**
     * Rafraîchit la zone d'affichage des combinaisons possibles pour la cage de la case sélectionnée.
     * @param modeleCase La case modèle actuellement sélectionnée.
     */
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

    /**
     * Sauvegarde l'état actuel de la partie.
     */
    private void sauvegarderPartie() {
        if (save != null && !partiePerdue && save.getIdGrille() != null && !save.getIdGrille().isEmpty()) {
            if (save.getMode() == Sauvegarde.ModeDeJeu.AVEN && partiePerdue) return;
            
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

    /**
     * Déconnecte les gestionnaires d'événements clavier de la scène.
     */
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

    /**
     * Déclenche la fin de partie en cas de défaite (ex: temps écoulé, vies épuisées).
     * @param message Le message à afficher au joueur.
     */
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
    
    /**
     * Vérifie si la grille est résolue correctement et déclenche la victoire si c'est le cas.
     */
    private void verifierVictoire() {
        if (ValidateurJeu.estVictoire(grilleModele) && !partiePerdue) {
            partiePerdue = true; 
            if (chronoManager != null) chronoManager.arreter();
            conteneurGrille.setDisable(true);
            conteneurBoutonsNombres.setDisable(true);
            deconnecterClavier();

            String nomProfil = MainApp.getProfileManager().getProfilActif();
            int secEcoulees = chronoManager.getSecondesEcoulees();
            
            int taille = grilleModele.getTaille();
            long pointsBase = (taille * taille) * 100L; 
            
            long penaliteTemps = Math.min((long)(secEcoulees * 2L), (long)(pointsBase * 0.5));
            long penaliteErreurs = save.getMalus() * 50L;
            long penaliteAides = save.getAidesUtilisees() * 50L;
            
            long scoreCalcul = pointsBase - penaliteTemps - penaliteErreurs - penaliteAides;
            scoreCalcul = Math.max(100L, scoreCalcul);
            
            if (save.getDiff() == Sauvegarde.Difficulte.MOYEN) scoreCalcul = (long)(scoreCalcul * 1.5);
            else if (save.getDiff() == Sauvegarde.Difficulte.DIFFI) scoreCalcul *= 2;

            MainApp.getProfileManager().enregistrerFinDePartie(nomProfil, true, secEcoulees, scoreCalcul, save.getDiff(), save.getIdGrille());            
            if (save != null) save.effacer(nomProfil);

            String msg = "Temps : " + (secEcoulees / 60) + " min " + (secEcoulees % 60) + " s.\nScore obtenu : " + scoreCalcul;
            PopupFactory.afficherPopupFinPartie("Victoire !", msg, Constantes.ICONE_VICTOIRE, Constantes.COULEUR_VICTOIRE, true, () -> actionRecommencer(null), () -> actionRetourMenu(null));
        }
    }

    /**
     * Sélectionne une case sur la grille. Met à jour la surbrillance et les informations affichées.
     * @param vueCase La vue de la case sélectionnée.
     * @param modeleCase Le modèle de la case sélectionnée.
     */
    private void selectionnerCase(VueCase vueCase, Case modeleCase) {
        if (vueCaseSelectionnee != null) vueCaseSelectionnee.getStyleClass().remove(Constantes.CSS_CASE_SELECTIONNEE);
        this.vueCaseSelectionnee = vueCase;
        this.caseModeleSelectionnee = modeleCase;
        vueCaseSelectionnee.getStyleClass().add(Constantes.CSS_CASE_SELECTIONNEE);
        rafraichirZoneCombinaisons(modeleCase);
        
        mettreAJourGuidesVisuels(modeleCase);
    }

    /**
     * Gère le clic sur un bouton numérique ou la saisie d'un chiffre au clavier.
     * @param valeur La valeur numérique à insérer ou annoter.
     */
    private void actionChiffreClique(int valeur) {
        if (caseModeleSelectionnee != null) {
            if (modeAnnotationActif) {
                caseModeleSelectionnee.basculerNote(valeur); 
                if(caseModeleSelectionnee.getValeur() < 10)
                    save.hist.addEtape(caseModeleSelectionnee.getX(), caseModeleSelectionnee.getY(), valeur + 10 + (modeHypotheseActif ? 20 : 0));
            } else {
                save.hist.addEtape(caseModeleSelectionnee.getX(), caseModeleSelectionnee.getY(), valeur + (modeHypotheseActif ? 20 : 0));
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

    /**
     * Bascule le mode annotation (pour écrire des petites notes dans les cases).
     * @param event L'événement de clic.
     */
    @FXML void actionBasculeAnnotation(ActionEvent event) {
        modeAnnotationActif = !modeAnnotationActif;
        if (btnAnnoter != null) btnAnnoter.setStyle(modeAnnotationActif ? "-fx-background-color: #bbdefb;" : "");
    }

    /**
     * Efface la valeur et les annotations de la case sélectionnée.
     * @param event L'événement de clic.
     */
    @FXML void actionEffacer(ActionEvent event) {
        if (caseModeleSelectionnee != null) {
            caseModeleSelectionnee.setEstHypothese(false);
            caseModeleSelectionnee.setValeur(0);
            caseModeleSelectionnee.effacerNotes();
            
            aideService.lancerAnalyse(grilleModele);
            save.hist.addEtape(caseModeleSelectionnee.getX(), caseModeleSelectionnee.getY(), (modeHypotheseActif ? 20 : 0));
            
            mettreAJourGuidesVisuels(caseModeleSelectionnee);
        }
    }

    /**
     * Vérifie la grille pour les erreurs et les signale visuellement.
     * @param event L'événement de clic.
     */
    @FXML void actionVerifier(ActionEvent event) {
        List<VueCase> vuesEnErreur = new ArrayList<>();
        boolean caseIncorrecte = false;

        for (int y = 0; y < grilleModele.getTaille(); y++) {
            for (int x = 0; x < grilleModele.getTaille(); x++) {
                Case c = grilleModele.getCase(x, y);
                if (c.getValeur() != 0 && c.getValeur() != c.getSolution()) {
                    vuesEnErreur.add(vueGrille.getGrilleVueCases(x, y));
                    save.setMalus(save.getMalus() + 1); 
                    if (save.getDefi() == Defi.TypeDefi.SURVI) caseIncorrecte = true;
                }
            }
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

    /**
     * Annule la dernière action du joueur.
     * @param event L'événement de clic.
     */
    @FXML void actionUndo(ActionEvent event) {
        save.hist.appliquerUndo(grilleModele, modeHypotheseActif);
        synchroniserVuesApresHistorique();
    }

    /**
     * Rétablit la dernière action annulée.
     * @param event L'événement de clic.
     */
    @FXML void actionRedo(ActionEvent event) {
        save.hist.appliquerRedo(grilleModele);
        synchroniserVuesApresHistorique();
    }

    /**
     * Synchronise l'affichage de la grille après une opération d'historique (undo/redo).
     * Met à jour les combinaisons et les guides visuels.
     */
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

    /**
     * Active le mode hypothèse.
     * @param event L'événement de clic.
     */
    @FXML void actionHypothese(ActionEvent event) {
        modeHypotheseActif = true;
        if (btnHypothese != null) btnHypothese.setDisable(true); 
        if (conteneurBoutonsHypothese != null) conteneurBoutonsHypothese.setVisible(true);
    }

    /**
     * Valide les hypothèses en cours, les transformant en valeurs réelles.
     * @param event L'événement de clic.
     */
    @FXML void actionValiderHypothese(ActionEvent event) {
        quitterModeHypotheseVisuel();
        save.hist.validerHypotheses();
    }

    /**
     * Annule toutes les hypothèses en cours, restaurant l'état précédent.
     * @param event L'événement de clic.
     */
    @FXML void actionAnnulerHypothese(ActionEvent event) {
        quitterModeHypotheseVisuel();
        save.hist.rollbackHypotheses(grilleModele, modeHypotheseActif);
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

    /**
     * Bascule la visibilité du menu déroulant.
     * @param event L'événement de clic.
     */
    @FXML void actionBasculerMenu(ActionEvent event) {
        if (menuDeroulant != null) menuDeroulant.setVisible(!menuDeroulant.isVisible());
    }

    /**
     * Retourne au menu principal, en sauvegardant la partie si nécessaire.
     * @param event L'événement de clic.
     */
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

    /**
     * Rafraîchit les annotations des cases en supprimant la valeur jouée
     * des notes des cases sur la même ligne et colonne.
     * @param targetX La coordonnée X de la case modifiée.
     * @param targetY La coordonnée Y de la case modifiée.
     * @param valeurJouee La valeur qui a été jouée.
     */
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

    /**
     * Affiche la popup de confirmation d'abandon.
     * @param event L'événement de clic.
     */
    @FXML void actionAbandonner(ActionEvent event) {
        if (menuDeroulant != null) menuDeroulant.setVisible(false);
        if (popupAbandon != null) popupAbandon.setVisible(true);
    }

    /**
     * Annule l'abandon de la partie et cache la popup.
     * @param event L'événement de clic.
     */
    @FXML void actionAnnulerAbandon(ActionEvent event) {
        if (popupAbandon != null) popupAbandon.setVisible(false);
    }

    /**
     * Confirme l'abandon de la partie, enregistre la défaite et retourne au menu.
     * @param event L'événement de clic.
     */
    @FXML void actionConfirmerAbandon(ActionEvent event) {
        if (popupAbandon != null) popupAbandon.setVisible(false);
        String nomProfil = MainApp.getProfileManager().getProfilActif();
        int secEcoulees = (chronoManager != null) ? chronoManager.getSecondesEcoulees() : 0;
        MainApp.getProfileManager().enregistrerFinDePartie(nomProfil, false, secEcoulees, 0, save.getDiff(), save.getIdGrille());        
        partiePerdue = true; 
        if (save != null) save.effacer(nomProfil);
        actionRetourMenu(event);
    }

    /**
     * Réinitialise la partie en cours.
     * @param event L'événement de clic.
     */
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
        
        save.hist = new fr.univ.calcudoku.save.Historique();
        if (chronoManager != null) chronoManager.arreter();
        save.tmp.setTempsPrecedent(0.0);
        
        save.setMalus(0); 
        save.setAidesUtilisees(0); 
        
        fr.univ.calcudoku.model.DonneesNiveau dataBase = fr.univ.calcudoku.utils.GestionnaireJeu.lireDonneesNiveauRessource(save.getIdGrille() + ".json");
        if (dataBase != null) save.setVies(dataBase.vies);
        
        quitterModeHypotheseVisuel();
        mettreAJourLabelDefi(); 
        if (chronoManager != null) chronoManager.demarrer();
        aideService.lancerAnalyse(grilleModele);
        if (caseModeleSelectionnee != null) rafraichirZoneCombinaisons(caseModeleSelectionnee);
        mettreAJourGuidesVisuels(caseModeleSelectionnee);
    }

    /**
     * Sauvegarde la partie et redirige vers la vue des règles et techniques.
     * @param event L'événement de clic.
     */
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

    /**
     * Affiche la calculatrice contextuelle.
     * @param event L'événement de clic.
     */
    @FXML void actionCalculatrice(ActionEvent event) { JeuUtilitaires.afficherCalculatrice(event); }
    
    /**
     * Actualise les indices d'aide en relançant l'analyse de la grille.
     * @param event L'événement de clic.
     */
    @FXML void actionActualiserAide(ActionEvent event) {
        if (labelMessageAide != null) labelMessageAide.setText("Recherche de techniques en cours...");
        aideService.lancerAnalyse(grilleModele); 
    }
    
    /**
     * Affiche la bulle d'aide et lance la recherche d'indices.
     */
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
        
        rafraichirContenuBulleAide();
    }
    
    /**
     * Rafraîchit le contenu de la bulle d'aide avec les indices trouvés.
     */
    private void rafraichirContenuBulleAide() {
        if (!listeAides.isEmpty() && indexAideActuelle < listeAides.size()) listeAides.get(indexAideActuelle).masquer();
        listeAides.clear(); 
        indexAideActuelle = 0;
        
        if (indicesEnAttente != null) {
            for (Indice ind : indicesEnAttente) listeAides.add(new CommandeAfficherIndice(ind, labelMessageAide, vueGrille));
        }
        
        if (!listeAides.isEmpty()) {
            listeAides.get(indexAideActuelle).afficher();
            mettreAJourBoutonsNavigation();
        } else {
            if (labelMessageAide != null) labelMessageAide.setText("Aucune technique trouvée pour le moment.");
            if (btnAmeliorerAide != null) btnAmeliorerAide.setDisable(true);
            if (btnAidePrecedente != null) btnAidePrecedente.setDisable(true);
            if (btnAideSuivante != null) btnAideSuivante.setDisable(true);
        }
    }

    /**
     * Ferme la bulle d'aide.
     */
    @FXML public void actionFermerBulleAide() {
        if (!listeAides.isEmpty()) listeAides.get(indexAideActuelle).masquer();
        if (bulleAide != null) bulleAide.setVisible(false);
    }

    /**
     * Améliore le niveau de détail de l'aide actuellement affichée.
     */
    @FXML public void actionAmeliorerAide() {
        if (!listeAides.isEmpty()) { 
            listeAides.get(indexAideActuelle).ameliorerNiveau(); 
            save.setAidesUtilisees(save.getAidesUtilisees() + 1); 
            mettreAJourBoutonsNavigation(); 
        }
    }

    /**
     * Affiche l'aide suivante dans la liste des indices.
     */
    @FXML public void actionAideSuivante() {
        if (indexAideActuelle < listeAides.size() - 1) {
            listeAides.get(indexAideActuelle).masquer(); indexAideActuelle++;
            listeAides.get(indexAideActuelle).afficher(); mettreAJourBoutonsNavigation();
        }
    }

    /**
     * Affiche l'aide précédente dans la liste des indices.
     */
    @FXML public void actionAidePrecedente() {
        if (indexAideActuelle > 0) {
            listeAides.get(indexAideActuelle).masquer(); indexAideActuelle--;
            listeAides.get(indexAideActuelle).afficher(); mettreAJourBoutonsNavigation();
        }
    }

    /**
     * Met à jour l'état des boutons de navigation de l'aide (précédent, suivant, améliorer).
     */
    private void mettreAJourBoutonsNavigation() {
        if (listeAides.isEmpty()) return;
        btnAidePrecedente.setDisable(indexAideActuelle == 0);
        btnAideSuivante.setDisable(indexAideActuelle == listeAides.size() - 1);
        btnAmeliorerAide.setDisable(!listeAides.get(indexAideActuelle).peutEtreAmeliore());
    }

    /**
     * Met à jour les guides visuels sur la grille (cases sur la même ligne/colonne ou ayant la même valeur).
     * @param caseActuelle La case actuellement sélectionnée.
     */
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
}