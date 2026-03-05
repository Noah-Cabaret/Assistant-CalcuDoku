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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;

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
    
    // Checkbox Paramètres=> mode sombre
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

        // Avatar
        try {
            InputStream is = getClass().getResourceAsStream("/images/utilisateur.png");
            if (is != null) imgAvatar.setImage(new Image(is));
        } catch (Exception e) { }

        // Stats & Options (depuis profil.ini)
        chargerStatistiquesProfil(nomActuel, manager);

        // Parties (depuis profils/Nom/jeu/*.json)
        chargerPartiesSauvegardees(nomActuel);
    }

    private void chargerStatistiquesProfil(String nom, ProfileManager manager) {
        Map<String, String> stats = manager.lireStatistiques(nom);

        // Remplissage des Stats
        lblTempsMoyen.setText("Temps total : " + formatTemps(stats.getOrDefault("temps_total", "0")));
        
        try {
            double ratio = Double.parseDouble(stats.getOrDefault("ratio_parties", "0")) * 100;
            lblTauxReussite.setText("Taux de réussite : " + (int)ratio + "%");
        } catch(Exception e) { lblTauxReussite.setText("Taux : 0%"); }

        lblNiveauAventure.setText("Niveau aventure : " + stats.getOrDefault("progression", "1"));
        lblMeilleurScore.setText("Meilleur score : " + stats.getOrDefault("score_max", "0"));
        
        String d = stats.getOrDefault("difficulte_max", "1");
        lblDifficulteMax.setText("Difficulté max : " + (d.equals("3") ? "Difficile" : (d.equals("2") ? "Moyenne" : "Facile")));

        // GESTION DU MODE SOMBRE
        // Lit la valeur "true" ou "false" du fichier profil.ini
        boolean isSombre = Boolean.parseBoolean(stats.getOrDefault("mode_sombre", "false"));
        
        // Coche la case correspondante (si les CheckBox existent dans le FXML)
        if (checkSombre != null) checkSombre.setSelected(isSombre);
        if (checkClair != null) checkClair.setSelected(!isSombre);
    }

    private void chargerPartiesSauvegardees(String nomProfil) {
        // Chemin : profils/"nomProfil"/jeu/
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
                    
                    // On vérifie que boxParties n'est pas null avant d'ajouter
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

        // 1. La Grille
        GridPane grid = new GridPane();
        // Plus besoin de setHgap/Vgap car les contraintes gèrent l'espace
        grid.setStyle("-fx-border-color: black; -fx-border-width: 1.5px; -fx-background-color: white;");

        // --- TECHNIQUE RESPONSIVE (INSPIRÉE DE VueGrille) ---
        // On définit une taille fixe pour le conteneur, mais l'intérieur est en %
        double tailleConteneur = 120.0; // Taille globale de la miniature
        grid.setPrefSize(tailleConteneur, tailleConteneur);
        grid.setMinSize(tailleConteneur, tailleConteneur);
        grid.setMaxSize(tailleConteneur, tailleConteneur);

        // Ajout des contraintes de colonnes (Largeur en %)
        for (int i = 0; i < niveau.dim; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / niveau.dim); // Ex: 25% pour grille de 4
            grid.getColumnConstraints().add(col);
            
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100.0 / niveau.dim); // Ex: 25% pour grille de 4
            grid.getRowConstraints().add(row);
        }
        
        // Calcul taille police (approximatif selon la taille de case théorique)
        double tailleCaseTheorique = tailleConteneur / niveau.dim;
        double taillePoliceIndice = Math.max(7, tailleCaseTheorique * 0.35);

        for (int y = 0; y < niveau.dim; y++) {
            for (int x = 0; x < niveau.dim; x++) {
                
                int indexBloc = getBlocIndex(x, y, niveau);
                BlocData bloc = niveau.blocs.get(indexBloc);
                
                StackPane cell = new StackPane();
                
                // NOTE : Avec les contraintes %, on n'a plus besoin de fixer la taille des cellules ici.
                // Elles vont remplir leur case automatiquement.
                
                // --- GESTION DES BORDURES (Style KenKen) ---
                String styleDroit = "none";
                int widthDroit = 0;
                if (x < niveau.dim - 1) {
                    int indexVoisin = getBlocIndex(x + 1, y, niveau);
                    if (indexVoisin == indexBloc) { styleDroit = "dashed"; widthDroit = 1; }
                    else { styleDroit = "solid"; widthDroit = 1; }
                }

                String styleBas = "none";
                int widthBas = 0;
                if (y < niveau.dim - 1) {
                    int indexVoisin = getBlocIndex(x, y + 1, niveau);
                    if (indexVoisin == indexBloc) { styleBas = "dashed"; widthBas = 1; }
                    else { styleBas = "solid"; widthBas = 1; }
                }

                cell.setStyle(String.format(
                    "-fx-background-color: white; " +
                    "-fx-border-color: black; " +
                    "-fx-border-style: solid %s %s solid; " +
                    "-fx-border-width: 0 %d %d 0;",
                    styleDroit, styleBas, widthDroit, widthBas
                ));

                // --- CONTENU ---
                if (isFirstCellOfBlock(x, y, bloc)) {
                    Label l = new Label(bloc.result + bloc.op);
                    l.setFont(new Font("Arial", taillePoliceIndice));
                    l.setStyle("-fx-font-weight: bold;");
                    StackPane.setAlignment(l, Pos.TOP_LEFT);
                    StackPane.setMargin(l, new javafx.geometry.Insets(1, 0, 0, 2));
                    cell.getChildren().add(l);
                }

                grid.add(cell, x, y);
            }
        }

        // 2. Textes
        String nomPropre = nomFichier.replace(".json", "");
        Label titre = new Label("Grille " + nomPropre);
        titre.setStyle("-fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-font-size: 11px;");

        int min = niveau.temps / 60;
        int sec = niveau.temps % 60;
        Label lblTemps = new Label(String.format("Temps : %d:%02d", min, sec));
        lblTemps.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 10px; -fx-text-fill: #333333;");

        vBox.getChildren().addAll(grid, titre, lblTemps);
        return vBox;
    }

    // Trouve l'index du bloc pour une coordonnée donnée
    private int getBlocIndex(int x, int y, DonneesNiveau niveau) {
        if (x < 0 || y < 0 || x >= niveau.dim || y >= niveau.dim) return -1;
        for (int i = 0; i < niveau.blocs.size(); i++) {
            if (niveau.blocs.get(i).nums.containsKey(x + "," + y)) return i;
        }
        return -1;
    }

    // Détermine si c'est la case "principale" pour afficher l'indice (ex: "8+")
    private boolean isFirstCellOfBlock(int x, int y, BlocData bloc) {
        int minX = 1000, minY = 1000;
        for (String key : bloc.nums.keySet()) {
            String[] parts = key.split(",");
            int cx = Integer.parseInt(parts[0]);
            int cy = Integer.parseInt(parts[1]);
            if (cy < minY) { minY = cy; minX = cx; }
            else if (cy == minY && cx < minX) { minX = cx; }
        }
        return (x == minX && y == minY);
    }
    

    private String formatTemps(String s) {
        try {
            int t = Integer.parseInt(s);
            return (t / 3600 > 0 ? t/3600 + "h " : "") + (t % 3600) / 60 + "min";
        } catch (Exception e) { return "0min"; }
    }

    @FXML private void onRetourClick() { MainApp.changerScene("/fxml/menu.fxml"); }
    @FXML private void onDeconnexionClick() { MainApp.changerScene("/fxml/accueil.fxml"); }

    // CLASSES INTERNES GSON 
    private static class DonneesNiveau {
        int temps;
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