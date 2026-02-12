package fr.univ.calcudoku.controller;

import com.google.gson.Gson;
import fr.univ.calcudoku.MainApp;
import fr.univ.calcudoku.service.ProfileManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class ProfilController {

    @FXML private ImageView imgAvatar;
    @FXML private Label lblNomProfil;

    // Labels Stats
    @FXML private Label lblTempsMoyen;
    @FXML private Label lblTauxReussite;
    @FXML private Label lblNiveauAventure;
    @FXML private Label lblDifficulteMax;
    @FXML private Label lblMeilleurScore;
    
    // Checkbox Paramètres (NOUVEAU)
    @FXML private CheckBox checkSombre;
    @FXML private CheckBox checkClair;

    // La zone en bas où on ajoute les grilles (HBox)
    @FXML private HBox boxParties; 

    @FXML
    public void initialize() {
        ProfileManager manager = MainApp.getProfileManager();
        String nomActuel = manager.getProfilActif();
        if (nomActuel == null) nomActuel = "Invité";

        lblNomProfil.setText(nomActuel);

        // 1. Avatar
        try {
            InputStream is = getClass().getResourceAsStream("/images/utilisateur.png");
            if (is != null) imgAvatar.setImage(new Image(is));
        } catch (Exception e) { }

        // 2. Stats & Options (depuis profil.ini)
        chargerStatistiquesProfil(nomActuel, manager);

        // 3. Parties (depuis profils/Nom/jeu/*.json)
        chargerPartiesSauvegardees(nomActuel);
    }

    private void chargerStatistiquesProfil(String nom, ProfileManager manager) {
        Map<String, String> stats = manager.lireStatistiques(nom);

        // --- Remplissage des Stats ---
        lblTempsMoyen.setText("Temps total : " + formatTemps(stats.getOrDefault("temps_total", "0")));
        
        try {
            double ratio = Double.parseDouble(stats.getOrDefault("ratio_parties", "0")) * 100;
            lblTauxReussite.setText("Taux de réussite : " + (int)ratio + "%");
        } catch(Exception e) { lblTauxReussite.setText("Taux : 0%"); }

        lblNiveauAventure.setText("Niveau aventure : " + stats.getOrDefault("progression", "1"));
        lblMeilleurScore.setText("Meilleur score : " + stats.getOrDefault("score_max", "0"));
        
        String d = stats.getOrDefault("difficulte_max", "1");
        lblDifficulteMax.setText("Difficulté max : " + (d.equals("3") ? "Difficile" : (d.equals("2") ? "Moyenne" : "Facile")));

        // --- GESTION DU MODE SOMBRE (NOUVEAU) ---
        // Lit la valeur "true" ou "false" du fichier profil.ini
        boolean isSombre = Boolean.parseBoolean(stats.getOrDefault("mode_sombre", "false"));
        
        // Coche la case correspondante (si les CheckBox existent dans le FXML)
        if (checkSombre != null) checkSombre.setSelected(isSombre);
        if (checkClair != null) checkClair.setSelected(!isSombre);
    }

    private void chargerPartiesSauvegardees(String nomProfil) {
        // Chemin : profils/Toto/jeu/
        File dossierJeux = new File("profils/" + nomProfil + "/jeu");

        // Si le dossier n'existe pas ou est vide, on arrête
        if (!dossierJeux.exists() || !dossierJeux.isDirectory()) {
            return;
        }

        File[] fichiersJson = dossierJeux.listFiles((dir, name) -> name.endsWith(".json"));

        if (fichiersJson != null) {
            Gson gson = new Gson();
            
            for (File fichier : fichiersJson) {
                try (FileReader reader = new FileReader(fichier)) {
                    DonneesNiveau niveau = gson.fromJson(reader, DonneesNiveau.class);
                    VBox carte = creerCartePartie(niveau, fichier.getName());
                    
                    // SÉCURITÉ : On vérifie que boxParties n'est pas null avant d'ajouter
                    if (boxParties != null) {
                        boxParties.getChildren().add(carte);
                    } else {
                        System.err.println("Erreur : boxParties est null. Vérifiez le fx:id dans profil.fxml");
                    }

                } catch (Exception e) {
                    System.err.println("Erreur lecture fichier : " + fichier.getName());
                    // e.printStackTrace(); 
                }
            }
        }
    }

    private VBox creerCartePartie(DonneesNiveau niveau, String nomFichier) {
        VBox vBox = new VBox(5);
        vBox.setAlignment(Pos.CENTER);

        // 1. La grille visuelle
        GridPane grid = new GridPane();
        grid.setGridLinesVisible(true);
        grid.setStyle("-fx-border-color: black; -fx-padding: 2; -fx-background-color: white;");
        
        double taille = 25.0;

        for (BlocData bloc : niveau.blocs) {
            boolean isFirst = true;
            for (Map.Entry<String, Integer> entry : bloc.nums.entrySet()) {
                String[] coords = entry.getKey().split(",");
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);

                StackPane cell = new StackPane();
                cell.setPrefSize(taille, taille);

                if (isFirst) {
                    Label l = new Label(bloc.result + bloc.op);
                    l.setFont(new Font("Arial", 8));
                    StackPane.setAlignment(l, Pos.TOP_LEFT);
                    StackPane.setMargin(l, new javafx.geometry.Insets(1,0,0,2));
                    cell.getChildren().add(l);
                    isFirst = false;
                }
                
                Label v = new Label(String.valueOf(entry.getValue()));
                v.setFont(new Font("Arial", 10));
                v.setStyle("-fx-font-weight: bold;");
                cell.getChildren().add(v);

                grid.add(cell, x, y);
            }
        }

        String nomPropre = nomFichier.replace(".json", ""); 
        Label titre = new Label(nomPropre + " (ID: " + niveau.id + ")"); 
        titre.setStyle("-fx-font-weight: bold;");
        Label sousTitre = new Label("Dim: " + niveau.dim + "x" + niveau.dim);

        vBox.getChildren().addAll(grid, titre, sousTitre);
        return vBox;
    }

    private String formatTemps(String s) {
        try {
            int t = Integer.parseInt(s);
            return (t / 3600 > 0 ? t/3600 + "h " : "") + (t % 3600) / 60 + "min";
        } catch (Exception e) { return "0min"; }
    }

    @FXML private void onRetourClick() { MainApp.changerScene("/fxml/menu.fxml"); }
    @FXML private void onDeconnexionClick() { MainApp.changerScene("/fxml/accueil.fxml"); }

    // --- CLASSES INTERNES GSON ---
    private static class DonneesNiveau {
        int id;
        int dim;
        //List<String> ops;
        List<BlocData> blocs;
    }
    private static class BlocData {
        int result;
        String op;
        Map<String, Integer> nums;
    }
}