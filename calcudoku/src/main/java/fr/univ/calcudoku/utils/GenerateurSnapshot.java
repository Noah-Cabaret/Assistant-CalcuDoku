package fr.univ.calcudoku.utils;

import com.google.gson.Gson;
import fr.univ.calcudoku.model.DonneesNiveau;
import fr.univ.calcudoku.model.Grille;
import fr.univ.calcudoku.service.JsonToModelAdapter;
import fr.univ.calcudoku.view.VueGrille;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileReader;

public class GenerateurSnapshot extends Application {

    private String getBasePath() {
        if (new File("calcudoku/src/main/resources/").exists()) {
            return "calcudoku/src/main/resources/";
        }
        return "src/main/resources/";
    }

    @Override
    public void start(Stage primaryStage) {
        String base = getBasePath();
        File dossierJson = new File(base + "grilles/json/");
        
        if (!dossierJson.exists() || !dossierJson.isDirectory()) {
            System.err.println("Erreur : Le dossier des grilles JSON est introuvable (" + dossierJson.getAbsolutePath() + ")");
            Platform.exit();
            return;
        }

        File[] fichiersJson = dossierJson.listFiles((dir, nom) -> nom.startsWith("libre_") && nom.endsWith(".json"));

        if (fichiersJson == null || fichiersJson.length == 0) {
            System.out.println("Aucun fichier libre_*.json trouvé dans le dossier.");
            Platform.exit();
            return;
        }

        System.out.println("Début de la génération pour " + fichiersJson.length + " fichiers...");

        for (File fichier : fichiersJson) {
            System.out.println(" -> Génération de l'image pour : " + fichier.getName());
            genererImageGrille(fichier.getName(), base);
        }

        System.out.println("Génération terminée avec succès !");
        Platform.exit();
    }

    private void genererImageGrille(String nomFichierJson, String base) {
        try {
            File fichierJson = new File(base + "grilles/json/" + nomFichierJson);
            File dossierImage = new File(base + "grilles/images/");

            if (!dossierImage.exists()) {
                dossierImage.mkdirs();
            }

            Gson gson = new Gson();
            DonneesNiveau data = gson.fromJson(new FileReader(fichierJson), DonneesNiveau.class);
            Grille grilleModele = JsonToModelAdapter.convertir(data);

            VueGrille vueGrille = new VueGrille(grilleModele);
            vueGrille.setPrefSize(500, 500);

            Scene scene = new Scene(vueGrille, 500, 500);
            File css = new File(base + "style/style.css");
            if (css.exists()) {
                scene.getStylesheets().add(css.toURI().toString());
            }

            vueGrille.setStyle("-fx-background-color: white;");
            vueGrille.applyCss();
            vueGrille.layout();

            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.WHITE); 
            WritableImage image = vueGrille.snapshot(params, null);

            String nomImage = nomFichierJson.replace(".json", ".png");
            File fichierImage = new File(dossierImage, nomImage);
            
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", fichierImage);

        } catch (Exception e) {
            System.err.println("Échec de la capture pour : " + nomFichierJson);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}