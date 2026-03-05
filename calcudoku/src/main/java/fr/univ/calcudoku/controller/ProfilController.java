package fr.univ.calcudoku.controller;

import com.google.gson.Gson;
import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.model.DonneesNiveau; // Import du modèle
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.service.JsonToModelAdapter;
import fr.univ.calcudoku.service.ProfileManager;
import fr.univ.calcudoku.view.VueGrille;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Map;

public class ProfilController {

    @FXML private ImageView imgAvatar;
    @FXML private Label lblNomProfil;
    @FXML private Label lblTempsMoyen, lblTauxReussite, lblNiveauAventure, lblDifficulteMax, lblMeilleurScore;
    @FXML private CheckBox checkSombre, checkClair;
    @FXML private HBox boxParties;

    @FXML
    public void initialize() {
        ProfileManager manager = MainApp.getProfileManager();
        String nomActuel = manager.getProfilActif();
        if (nomActuel == null) nomActuel = "Invité";

        lblNomProfil.setText(nomActuel);
        chargerAvatar();
        chargerStatistiquesProfil(nomActuel, manager);
        chargerPartiesSauvegardees(nomActuel);
    }

    private void chargerPartiesSauvegardees(String nomProfil) {
        File dossierJeux = new File("profils/" + nomProfil + "/jeu");
        if (!dossierJeux.exists() || !dossierJeux.isDirectory()) return;

        File[] fichiersJson = dossierJeux.listFiles((dir, name) -> name.endsWith(".json"));

        if (fichiersJson != null && boxParties != null) {
            Gson gson = new Gson();
            boxParties.getChildren().clear();

            for (File fichier : fichiersJson) {
                try (FileReader reader = new FileReader(fichier)) {
                    // 1. Charger JSON
                    DonneesNiveau niveau = gson.fromJson(reader, DonneesNiveau.class);
                    
                    // 2. Appeler la méthode de création qui utilise l'Adaptateur
                    VBox carte = creerCartePartie(niveau, fichier.getName());
                    
                    boxParties.getChildren().add(carte);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private VBox creerCartePartie(DonneesNiveau niveau, String nomFichier) {
        VBox vBox = new VBox(5);
        vBox.setAlignment(Pos.CENTER);

        // --- ADAPTATEUR ---
        // On transforme les données brutes en "Vraie Grille" sans toucher aux classes Grille/Case
        Grille grilleModele = JsonToModelAdapter.convertir(niveau);

        // --- VUE ---
        VueGrille vueGrille = new VueGrille(grilleModele);
        
        // On demande à la vue de dessiner les bordures
        vueGrille.rafraichirToutesLesBordures();

        // --- MINIATURE (ZOOM) ---
        // VueGrille est trop grande. On la met dans un conteneur fixe et on dézoome.
        StackPane conteneur = new StackPane(vueGrille);
        double tailleMiniature = 160.0;
        conteneur.setPrefSize(tailleMiniature, tailleMiniature);
        conteneur.setMaxSize(tailleMiniature, tailleMiniature);
        
        // Facteur de zoom (0.35 = 35% de la taille originale)
        // Tu peux ajuster ce chiffre si c'est trop gros ou trop petit
        vueGrille.setScaleX(0.90); 
        vueGrille.setScaleY(0.90);

        // --- TEXTES ---
        String nomPropre = nomFichier.replace(".json", "");
        Label titre = new Label("Grille " + nomPropre);
        titre.setStyle("-fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-font-size: 11px;");

        int min = niveau.temps / 60;
        int sec = niveau.temps % 60;
        Label lblTemps = new Label(String.format("Temps : %d:%02d", min, sec));
        lblTemps.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 10px; -fx-text-fill: #333333;");

        vBox.getChildren().addAll(conteneur, titre, lblTemps);
        return vBox;
    }

    private void chargerStatistiquesProfil(String nom, ProfileManager manager) {
        Map<String, String> stats = manager.lireStatistiques(nom);

        lblTempsMoyen.setText("Temps total : " + formatTemps(stats.getOrDefault("temps_total", "0")));
        
        try {
            double ratio = Double.parseDouble(stats.getOrDefault("ratio_parties", "0")) * 100;
            lblTauxReussite.setText("Taux de réussite : " + (int)ratio + "%");
        } catch(Exception e) { lblTauxReussite.setText("Taux : 0%"); }

        lblNiveauAventure.setText("Niveau aventure : " + stats.getOrDefault("progression", "1"));
        lblMeilleurScore.setText("Meilleur score : " + stats.getOrDefault("score_max", "0"));
        
        String d = stats.getOrDefault("difficulte_max", "1");
        lblDifficulteMax.setText("Difficulté max : " + (d.equals("3") ? "Difficile" : (d.equals("2") ? "Moyenne" : "Facile")));

        boolean isSombre = Boolean.parseBoolean(stats.getOrDefault("mode_sombre", "false"));
        if (checkSombre != null) checkSombre.setSelected(isSombre);
        if (checkClair != null) checkClair.setSelected(!isSombre);
    }
    
    private void chargerAvatar() {
        try {
            InputStream is = getClass().getResourceAsStream("/images/utilisateur.png");
            if (is != null) imgAvatar.setImage(new Image(is));
        } catch (Exception e) { }
    }
    
    private String formatTemps(String s) {
        try {
            int t = Integer.parseInt(s);
            return (t / 3600 > 0 ? t/3600 + "h " : "") + (t % 3600) / 60 + "min";
        } catch (Exception e) { return "0min"; }
    }

    @FXML private void onRetourClick() { MainApp.changerScene("/fxml/menu.fxml"); }
    @FXML private void onDeconnexionClick() { MainApp.changerScene("/fxml/accueil.fxml"); }
}