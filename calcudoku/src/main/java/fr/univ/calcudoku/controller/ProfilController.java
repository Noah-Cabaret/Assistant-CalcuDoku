package fr.univ.calcudoku.controller;

import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.model.DonneesNiveau;
import fr.univ.calcudoku.service.ProfileManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.Map;
import java.util.Scanner;

/**
 * Contrôleur de la vue du profil utilisateur.
 * Affiche les statistiques, les paramètres (thème, aide au calcul) et les parties en cours.
 */
public class ProfilController {

    /**
     * Stocke le chemin de la page précédente pour le bouton "Retour".
     */
    public static String pagePrecedente = "/fxml/menu.fxml";

    /** Icône de l'avatar du profil. */
    @FXML private FontIcon imgAvatar;
    /** Label affichant le nom du profil. */
    @FXML private Label lblNomProfil;
    /** Conteneur principal de la vue. */
    @FXML private VBox boxCentrale;
    
    /** Label pour le nombre de parties jouées. */
    @FXML private Label lblPartiesJouees;
    /** Label pour le nombre de victoires. */
    @FXML private Label lblVictoires;
    /** Label pour le temps de jeu moyen. */
    @FXML private Label lblTempsMoyen;
    /** Label pour le taux de réussite. */
    @FXML private Label lblTauxReussite;
    /** Label pour le niveau atteint en mode Aventure. */
    @FXML private Label lblNiveauAventure;
    /** Label pour la difficulté maximale jouée. */
    @FXML private Label lblDifficulteMax;
    /** Label pour le meilleur score obtenu. */
    @FXML private Label lblMeilleurScore;
    
    /** Bouton radio pour activer le thème sombre. */
    @FXML private RadioButton radioSombre;
    /** Bouton radio pour activer le thème clair. */
    @FXML private RadioButton radioClair;
    /** Bouton radio pour sélectionner l'aide "Combinaisons". */
    @FXML private RadioButton radioProfilCombinaisons;
    /** Bouton radio pour sélectionner l'aide "Calculatrice". */
    @FXML private RadioButton radioProfilCalculatrice;
    /** Groupe de toggles pour l'aide au calcul. */
    @FXML private javafx.scene.control.ToggleGroup groupeAide;
    /** Conteneur pour les cartes des parties en cours. */
    @FXML private HBox boxParties;
    /** Groupe de toggles pour le thème. */
    @FXML private javafx.scene.control.ToggleGroup groupeTheme;
    /** Bouton pour retourner à la page précédente. */
    @FXML private Button btnRetour;
    /** Bouton pour se déconnecter et retourner à l'accueil. */
    @FXML private Button btnDeconnexion;

    /**
     * Méthode d'initialisation appelée après le chargement du FXML.
     * Charge les informations du profil actif (nom, avatar, statistiques, paramètres)
     * et les parties sauvegardées. Configure les listeners pour les changements de paramètres.
     */
    @FXML
    public void initialize() {
        ProfileManager manager = MainApp.getProfileManager();
        String nomActuel = manager.getProfilActif();

        lblNomProfil.setText(nomActuel);
        chargerAvatar();
        chargerStatistiquesProfil(nomActuel, manager);
        
        if (boxParties != null) boxParties.getChildren().clear();
        chargerPartiesSauvegardees(nomActuel);
        
        if (radioSombre != null) radioSombre.setOnAction(e -> {
            activerModeSombre(true);
            if (boxParties != null) { boxParties.getChildren().clear(); chargerPartiesSauvegardees(lblNomProfil.getText()); }
            manager.mettreAJourStatistique(lblNomProfil.getText(), "mode_sombre", "true");
        });
        
        if (radioClair != null) radioClair.setOnAction(e -> {
            activerModeSombre(false);
            if (boxParties != null) { boxParties.getChildren().clear(); chargerPartiesSauvegardees(lblNomProfil.getText()); }
            manager.mettreAJourStatistique(lblNomProfil.getText(), "mode_sombre", "false");
        });

        if (radioProfilCombinaisons != null) radioProfilCombinaisons.setOnAction(e -> manager.mettreAJourStatistique(lblNomProfil.getText(), "aide_calcul", "combinaisons"));
        if (radioProfilCalculatrice != null) radioProfilCalculatrice.setOnAction(e -> manager.mettreAJourStatistique(lblNomProfil.getText(), "aide_calcul", "calculatrice"));
    }

    /**
     * Charge les parties sauvegardées pour un profil donné, en parcourant les dossiers
     * du mode libre et du mode aventure.
     * @param nomProfil Le nom du profil dont on veut charger les parties.
     */
    private void chargerPartiesSauvegardees(String nomProfil) {
        File dossierParties = new File("profils/" + nomProfil + "/parties");
        chargerFichiersDossier(nomProfil, dossierParties);

        File dossierAventure = new File("profils/" + nomProfil + "/parties/aventure");
        chargerFichiersDossier(nomProfil, dossierAventure);
    }

    /**
     * Parcourt un dossier spécifique à la recherche de fichiers de sauvegarde (.json)
     * et crée une carte visuelle pour chaque partie trouvée.
     * @param nomProfil Le nom du profil.
     * @param dossier Le dossier à analyser.
     */
    private void chargerFichiersDossier(String nomProfil, File dossier) {
        if (!dossier.exists() || !dossier.isDirectory()) return;

        File[] fichiersJson = dossier.listFiles((dir, name) -> name.endsWith(".json"));

        if (fichiersJson != null && boxParties != null) {
            for (File fichierJson : fichiersJson) {
                DonneesNiveau niveauBase = fr.univ.calcudoku.utils.GestionnaireJeu.lireDonneesNiveauRessource(fichierJson.getName());
                
                if (niveauBase != null) {
                    File fichierIni = new File(dossier, fichierJson.getName().replace(".json", ".ini"));
                    int tempsSauvegarde = lireTempsDepuisIni(fichierIni);
                    
                    VBox carte = creerCartePartie(nomProfil, fichierJson, tempsSauvegarde);
                    boxParties.getChildren().add(carte);
                }
            }
        }
    }

    /**
     * Lit le temps de jeu écoulé depuis un fichier de sauvegarde .ini.
     * @param fichierIni Le fichier .ini à lire.
     * @return Le temps en secondes, ou 0 si non trouvé ou en cas d'erreur.
     */
    private int lireTempsDepuisIni(File fichierIni) {
        if (!fichierIni.exists()) return 0;
        try (Scanner sc = new Scanner(fichierIni)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.startsWith("temps=")) return (int) Double.parseDouble(line.split("=")[1].trim());
            }
        } catch (Exception e) {
            System.err.println("Erreur lecture temps INI: " + e.getMessage());
        
            System.err.println("Erreur lecture temps INI: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Crée une carte visuelle (VBox) pour une partie sauvegardée.
     * La carte affiche une image de la grille, son nom et le temps de jeu.
     * Elle est cliquable pour reprendre la partie.
     * @param nomProfil Le nom du profil.
     * @param fichierJson Le fichier de sauvegarde de la partie.
     * @param temps Le temps de jeu écoulé en secondes.
     * @return Un VBox représentant la carte de la partie.
     */
    private VBox creerCartePartie(String nomProfil, File fichierJson, int temps) {
        File fichierImage = new File("profils/" + nomProfil + "/jeu/images/" + fichierJson.getName().replace(".json", ".png"));
        ImageView imgView = new ImageView();
        if (fichierImage.exists()) imgView.setImage(new Image(fichierImage.toURI().toString()));
        else imgView.setStyle("-fx-background-color: lightgray;");

        imgView.setFitHeight(150); imgView.setFitWidth(150); imgView.setPreserveRatio(true);

        String nomPropre = fichierJson.getName().replace(".json", "");
        Label lblTitre = new Label("Grille " + nomPropre);
        Label lblTemps = new Label(String.format("Temps : %d:%02d", temps / 60, temps % 60));

        if (MainApp.isModeSombre()) {
            lblTitre.setStyle("-fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: white;");
            lblTemps.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 11px; -fx-text-fill: #cccccc;");
        } else {
            lblTitre.setStyle("-fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: black;");
            lblTemps.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 11px; -fx-text-fill: #555555;");
        }

        VBox carte = new VBox(10, imgView, lblTitre, lblTemps);
        carte.setAlignment(Pos.CENTER); carte.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        String styleNormal = "-fx-cursor: hand; -fx-padding: 10; -fx-background-color: transparent; -fx-border-color: transparent; -fx-border-radius: 10; -fx-background-radius: 10;";
        String styleHover = "-fx-cursor: hand; -fx-padding: 10; -fx-background-color: #f5f5f5; -fx-border-color: #cccccc; -fx-border-radius: 10; -fx-background-radius: 10;";

        if (MainApp.isModeSombre()) styleHover = "-fx-cursor: hand; -fx-padding: 10; -fx-background-color: #444444; -fx-border-color: #777777; -fx-border-radius: 10; -fx-background-radius: 10;";

        carte.setStyle(styleNormal);
        String finalStyleHover = styleHover;
        carte.setOnMouseEntered(e -> carte.setStyle(finalStyleHover));
        carte.setOnMouseExited(e -> carte.setStyle(styleNormal));

        carte.setOnMouseClicked(e -> {
            javafx.stage.Stage stage = (javafx.stage.Stage) boxParties.getScene().getWindow();
            fr.univ.calcudoku.utils.GestionnaireJeu.chargerPartieDepuisFichier(stage, fichierJson);
        });

        return carte;
    }

    /**
     * Charge et affiche les statistiques et les paramètres du profil.
     * @param nom Le nom du profil.
     * @param manager Le gestionnaire de profils.
     */
    private void chargerStatistiquesProfil(String nom, ProfileManager manager) {
        Map<String, String> stats = manager.lireStatistiques(nom);

        if (lblPartiesJouees != null) lblPartiesJouees.setText("Parties jouées : " + stats.getOrDefault("parties_jouees", "0"));
        if (lblVictoires != null) lblVictoires.setText("Victoires : " + stats.getOrDefault("victoires", "0"));
        lblTempsMoyen.setText("Temps moyen : " + formatTemps(stats.getOrDefault("temps_moyen", "0")));
        
        try {
            double ratio = Double.parseDouble(stats.getOrDefault("ratio_parties", "0")) * 100;
            lblTauxReussite.setText("Taux de réussite : " + (int)ratio + "%");
        } catch(Exception e) { lblTauxReussite.setText("Taux : 0%"); }

        lblNiveauAventure.setText("Niveau aventure : " + stats.getOrDefault("progression", "1"));
        lblMeilleurScore.setText("Meilleur score : " + stats.getOrDefault("score_max", "0"));
        
        String d = stats.getOrDefault("difficulte_max", "1");
        lblDifficulteMax.setText("Difficulté max : " + (d.equals("3") ? "Difficile" : (d.equals("2") ? "Moyenne" : "Facile")));

        boolean isSombre = Boolean.parseBoolean(stats.getOrDefault("mode_sombre", "false"));
        if (radioSombre != null) radioSombre.setSelected(isSombre);
        if (radioClair != null) radioClair.setSelected(!isSombre);

        String aide = stats.getOrDefault("aide_calcul", "combinaisons");
        if (aide.equals("calculatrice") && radioProfilCalculatrice != null) radioProfilCalculatrice.setSelected(true);
        else if (radioProfilCombinaisons != null) radioProfilCombinaisons.setSelected(true);

        // On force le redessinage complet des icônes au chargement de la page
        javafx.application.Platform.runLater(() -> activerModeSombre(isSombre));
    }
    
    private void chargerAvatar() { imgAvatar.setIconColor(MainApp.isModeSombre() ? Color.WHITE : Color.BLACK); }
    
    /**
     * Formate un temps en secondes en une chaîne de caractères "MM:SS".
     * @param s Le temps en secondes, sous forme de chaîne.
     * @return La chaîne formatée, ou "00:00" en cas d'erreur.
     */
    private String formatTemps(String s) {
        try {
            int totalSecondes = Integer.parseInt(s);
            int minutes = totalSecondes / 60;
            int secondes = totalSecondes % 60;
            return String.format("%02d:%02d", minutes, secondes);
        } catch (Exception e) { 
            return "00:00"; 
        }
    }

    /**
     * Active ou désactive le mode sombre pour la scène actuelle et met à jour
     * les couleurs des éléments de l'interface.
     * @param activer true pour activer le mode sombre, false pour le désactiver.
     */
    private void activerModeSombre(boolean activer) {
        MainApp.setModeSombre(activer);
        javafx.scene.Scene sceneActuelle = boxParties.getScene();
        if (sceneActuelle != null) {
            String cssPath = getClass().getResource("/styles/sombre.css").toExternalForm();
            if (activer) {
                if (!sceneActuelle.getStylesheets().contains(cssPath)) sceneActuelle.getStylesheets().add(cssPath);
            } else {
                sceneActuelle.getStylesheets().remove(cssPath);
            }
        }

        Color couleurC = activer ? Color.WHITE : Color.BLACK;
        String couleurT = activer ? "white" : "black";

        // --- CORRECTION DE LA FLÈCHE DE RETOUR ---
        if (btnRetour != null) {
            btnRetour.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: " + couleurT + ";");
            if (btnRetour.getGraphic() instanceof FontIcon) {
                ((FontIcon) btnRetour.getGraphic()).setIconColor(couleurC);
            }
        }

        // --- CORRECTION DU BOUTON DÉCONNEXION---
        if (btnDeconnexion != null) {
            if (btnDeconnexion.getGraphic() instanceof FontIcon) {
                ((FontIcon) btnDeconnexion.getGraphic()).setIconColor(couleurC);
            }
        }

        // --- CORRECTION DES BORDURES DES BOÎTES ---
        String couleurBordure = activer ? "white" : "black";
        
        // 1. La boîte "Partie en cours" (C'est le 2ème élément de la boxCentrale)
        if (boxCentrale.getChildren().size() > 1 && boxCentrale.getChildren().get(1) instanceof VBox) {
            boxCentrale.getChildren().get(1).setStyle("-fx-border-color: " + couleurBordure + "; -fx-border-radius: 20; -fx-border-width: 1; -fx-padding: 10;");
        }
        
        // 2. Les 3 boîtes du haut (Touches, Stats, Paramètres)
        if (boxCentrale.getChildren().size() > 0 && boxCentrale.getChildren().get(0) instanceof HBox) {
            HBox ligneHaut = (HBox) boxCentrale.getChildren().get(0);
            for (javafx.scene.Node boite : ligneHaut.getChildren()) {
                boite.setStyle("-fx-border-color: " + couleurBordure + "; -fx-border-radius: 20; -fx-border-width: 1; -fx-padding: 10;");
            }
        }
        
        // --- CORRECTION DE L'AVATAR ---
        if (imgAvatar != null) {
            imgAvatar.setIconColor(couleurC);
        }
    }

    /**
     * Gère le clic sur le bouton "Retour".
     * Redirige vers la page précédente stockée dans {@link #pagePrecedente}.
     */
    @FXML private void onRetourClick() { MainApp.changerScene(pagePrecedente); }
    @FXML private void onDeconnexionClick() { MainApp.changerScene("/fxml/accueil.fxml"); MainApp.setModeSombre(false); }
}