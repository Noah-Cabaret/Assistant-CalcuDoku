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
import java.util.List;

public class GenerateurSnapshot extends Application {

    @Override
    public void start(Stage primaryStage) {
        List<String> args = getParameters().getRaw();
        if (args.isEmpty()) {
            System.err.println("probleme arguments");
            Platform.exit(); 
            return;
        }

        String fichier = args.get(0);
        genererImageGrille(fichier);

        Platform.exit();
    }

    private void genererImageGrille(String nomFichierJson) {
        try {
            File fichierJson = new File("src/main/resources/grilles/json/" + nomFichierJson);
            File dossierImage = new File("src/main/resources/grilles/images/");

            if (!dossierImage.exists()) {
                dossierImage.mkdirs();
            }

            Gson gson = new Gson();
            DonneesNiveau data = gson.fromJson(new FileReader(fichierJson), DonneesNiveau.class);
            Grille grilleModele = JsonToModelAdapter.convertir(data);

            VueGrille vueGrille = new VueGrille(grilleModele);
            vueGrille.rafraichirToutesLesBordures();

            vueGrille.setPrefSize(500, 500);

            Scene scene = new Scene(vueGrille, 500, 500);
            File css = new File("src/main/resources/style/style.css");
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
            e.printStackTrace();
            System.err.println("Echec shapshot");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}