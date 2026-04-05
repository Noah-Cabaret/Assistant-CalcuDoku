package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.model.DonneesNiveau;
import fr.univ.calcudoku.utils.CacheRessources;
import fr.univ.calcudoku.utils.Constantes;
import fr.univ.calcudoku.utils.GestionnaireJeu;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.Scanner;

/**
 * Contrôleur du menu de sélection des parties en mode libre.
 * Permet de choisir la taille, la difficulté et de lancer une grille.
 */
public class MenuLibreController {
    /** Groupe de boutons radio pour la sélection de la taille de la grille. */
    @FXML 
    private ToggleGroup groupeTaille;
    /** Groupe de boutons radio pour la sélection de la difficulté de la grille. */
    @FXML 
    private ToggleGroup groupeDifficulte;
    /** Conteneur HBox pour afficher les cartes des grilles disponibles. */
    @FXML 
    private HBox boxGrilles;
    /** Icône des paramètres, dont la couleur s'adapte au thème. */
    @FXML 
    private FontIcon imgParametres;
    /** Bouton de retour vers le menu principal. */
    @FXML 
    private Button btnRetour;

    /** Style CSS par défaut pour les boutons non sélectionnés dans les groupes de toggles. */
    private final String STYLE_NORMAL = "-fx-background-color: transparent; -fx-text-fill: #555555; -fx-cursor: hand; -fx-background-radius: 50em; -fx-pref-width: 40px; -fx-pref-height: 40px; -fx-font-size: 14px;";
    /** Style CSS pour les boutons sélectionnés dans les groupes de toggles. */
    private final String STYLE_SELECTIONNE = "-fx-background-color: white; -fx-border-color: black; -fx-border-radius: 50em; -fx-background-radius: 50em; -fx-text-fill: black; -fx-cursor: hand; -fx-pref-width: 40px; -fx-pref-height: 40px; -fx-font-weight: bold; -fx-font-size: 14px;";

    /**
     * Méthode d'initialisation appelée automatiquement après le chargement du fichier FXML.
     * Configure la couleur de l'icône des paramètres, initialise les groupes de toggles
     * pour la taille et la difficulté, et rafraîchit l'affichage des grilles.
     */
    @FXML
    public void initialize() {

        imgParametres.setIconColor(MainApp.isModeSombre() ? Color.WHITE : Color.BLACK);
        configurerToggleGroup(groupeTaille);
        configurerToggleGroup(groupeDifficulte);

        // Adapte le style du bouton de retour au thème sombre/clair
        if (btnRetour != null) {
            btnRetour.setStyle("-fx-background-color: transparent; -fx-cursor: hand;"); 
            
            if (btnRetour.getGraphic() instanceof FontIcon) {
                ((FontIcon) btnRetour.getGraphic()).setIconColor(MainApp.isModeSombre() ? Color.WHITE : Color.BLACK);
            }
        }

        rafraichirGrilles();
    }

    /**
     * Configure un groupe de boutons radio (ToggleGroup) pour appliquer des styles visuels
     * en fonction de l'état sélectionné et déclencher un rafraîchissement des grilles.
     * @param groupe Le ToggleGroup à configurer.
     */
    private void configurerToggleGroup(ToggleGroup groupe) {
        for (Toggle t : groupe.getToggles()) {
            ToggleButton btn = (ToggleButton) t;
            btn.setStyle(btn.isSelected() ? STYLE_SELECTIONNE : STYLE_NORMAL);
        }

        groupe.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                groupe.selectToggle(oldVal);
                return;
            }

            for (Toggle t : groupe.getToggles()) {
                ToggleButton btn = (ToggleButton) t;
                btn.setStyle(btn.isSelected() ? STYLE_SELECTIONNE : STYLE_NORMAL);
            }

            rafraichirGrilles();
        });
    }

    /**
     * Rafraîchit l'affichage des cartes de grilles dans le conteneur {@code boxGrilles}.
     * Les grilles affichées dépendent de la taille et de la difficulté sélectionnées.
     */
    private void rafraichirGrilles() {
        boxGrilles.getChildren().clear();

        if (groupeTaille.getSelectedToggle() == null || groupeDifficulte.getSelectedToggle() == null) return;

        String taille = ((ToggleButton) groupeTaille.getSelectedToggle()).getText();
        String diff = ((ToggleButton) groupeDifficulte.getSelectedToggle()).getText();

        for (int i = 1; i <= 3; i++) {
            String baseName = Constantes.PREFIX_LIBRE + taille + "_" + diff + "_" + i;
            String nomFichierJson = baseName + ".json";
            
            DonneesNiveau niveau = GestionnaireJeu.lireDonneesNiveauRessource(nomFichierJson);

            if (niveau != null) {
                VBox carte = creerCarteNiveau(niveau, baseName, i);
                boxGrilles.getChildren().add(carte);
            } else {
                VBox carteVide = fr.univ.calcudoku.utils.CarteUIFactory.creerCarteVide("Grille " + i, boxGrilles);
                boxGrilles.getChildren().add(carteVide);
            }
        }
    }

    /**
     * Crée une carte visuelle pour représenter un niveau de grille spécifique.
     * La carte affiche le nom de la grille, le temps de jeu en cours ou le record,
     * et une image de la grille. Elle est interactive pour lancer la partie.
     * @param niveau Les données du niveau (taille, difficulté, etc.).
     * @param baseName Le nom de base de la grille (ex: "libre_4_Facile_1").
     * @param index L'index de la grille dans sa catégorie (ex: 1, 2, 3).
     * @return Un conteneur VBox représentant la carte de la grille.
     */
    private VBox creerCarteNiveau(DonneesNiveau niveau, String baseName, int index) {
        String nomProfil = MainApp.getProfileManager().getProfilActif();

        File fichierJsonSave = new File(Constantes.DOSSIER_PROFILS + nomProfil + Constantes.SOUS_DOSSIER_PARTIES + baseName + ".json");
        File fichierIniSave = new File(Constantes.DOSSIER_PROFILS + nomProfil + Constantes.SOUS_DOSSIER_PARTIES + baseName + ".ini");
        File fichierImageSave = new File(Constantes.DOSSIER_PROFILS + nomProfil + Constantes.SOUS_DOSSIER_IMAGES + baseName + ".png");

        Image imageA_Afficher;
        String texteTemps;
        Runnable actionClic;

        // Récupération du record de la grille
        fr.univ.calcudoku.save.Record rec = fr.univ.calcudoku.save.GestionnaireRecords.getRecord(baseName);

        if (fichierJsonSave.exists() && fichierIniSave.exists()) {
            
            if (fichierImageSave.exists()) {
                imageA_Afficher = new Image(fichierImageSave.toURI().toString());
            } else {
                imageA_Afficher = CacheRessources.getImage(Constantes.CHEMIN_GRILLES_IMAGES + baseName + ".png");
            }
            
            int tempsSave = lireTempsDepuisIni(fichierIniSave);
            texteTemps = String.format("En cours : %02d:%02d", tempsSave / 60, tempsSave % 60);
            
            if (rec != null) {
                texteTemps += String.format("\n🏆 %s : %d pts (%02d:%02d)", rec.joueur, rec.score, rec.temps / 60, rec.temps % 60);
            }
            
            actionClic = () -> {
                javafx.stage.Stage stage = (javafx.stage.Stage) boxGrilles.getScene().getWindow();
                GestionnaireJeu.chargerPartieDepuisFichier(stage, fichierJsonSave);
            };

        } else {
            
            imageA_Afficher = CacheRessources.getImage(Constantes.CHEMIN_GRILLES_IMAGES + baseName + ".png");
            
            if (rec != null) {
                texteTemps = String.format("🏆 %s : %d pts (%02d:%02d)", rec.joueur, rec.score, rec.temps / 60, rec.temps % 60);
            } else {
                texteTemps = "Nouvelle partie";
            }
            
            actionClic = () -> {
                javafx.stage.Stage stage = (javafx.stage.Stage) boxGrilles.getScene().getWindow();
                GestionnaireJeu.chargerPartie(stage, baseName + ".json");
            };
        }

        return fr.univ.calcudoku.utils.CarteUIFactory.creerCarteGrille(
            "Grille " + index, 
            texteTemps, 
            imageA_Afficher, 
            boxGrilles,
            actionClic
        );
    }

    /**
     * Lit le temps de jeu sauvegardé depuis un fichier .ini.
     * @param fichierIni Le fichier .ini contenant le temps.
     * @return Le temps en secondes si trouvé et valide, sinon 0.
     *         Affiche la trace de la pile en cas d'erreur de lecture.
     */
    private int lireTempsDepuisIni(File fichierIni) {
        if (!fichierIni.exists()) return 0;
        try (Scanner sc = new Scanner(fichierIni)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.startsWith("temps=")) {
                    String[] parts = line.split("=");
                    if (parts.length > 1) {
                        return (int) Double.parseDouble(parts[1].trim());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Gère le clic sur le bouton des paramètres.
     * Redirige vers la vue du profil, en spécifiant que la page précédente était le menu libre.
     */
    @FXML 
    private void onParametresClick() { 
        ProfilController.pagePrecedente = Constantes.VUE_MENU_LIBRE; 
        MainApp.changerScene(Constantes.VUE_PROFIL); 
    }
    
    /**
     * Gère le clic sur le bouton de retour.
     * Redirige vers la vue du menu principal.
     */
    @FXML 
    private void onRetourClick() { 
        MainApp.changerScene(Constantes.VUE_MENU); 
    }
}