package fr.univ.calcudoku.utils;

import fr.univ.calcudoku.MainApp;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;

/**
 * Utilitaire pour gérer les changements visuels liés au thème de l'application.
 */
public class ThemeUtil {

    /**
     * Applique un filtre 100% blanc sur les images si le mode sombre est actif.
     * Les "..." permettent de passer autant d'images que l'on veut séparées par des virgules !
     */
    public static void appliquerFiltreBlancSiSombre(ImageView... images) {
        if (MainApp.modeSombreActif) {
            ColorAdjust filtreBlanc = new ColorAdjust();
            filtreBlanc.setBrightness(1.0);
            
            for (ImageView img : images) {
                if (img != null) {
                    img.setEffect(filtreBlanc);
                }
            }
        } else {
            // Si on repasse en mode clair, on enlève le filtre
            for (ImageView img : images) {
                if (img != null) {
                    img.setEffect(null);
                }
            }
        }
    }
}