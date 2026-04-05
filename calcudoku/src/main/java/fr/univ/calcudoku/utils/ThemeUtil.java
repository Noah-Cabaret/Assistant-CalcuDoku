package fr.univ.calcudoku.utils;

import fr.univ.calcudoku.MainApp;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;

/**
 * Utilitaire pour gérer les changements visuels liés au thème de l'application.
 */
public class ThemeUtil {

    /**
     * Applique un filtre blanc sur les images si le mode sombre est actif.
     * @param images les images à filtrer
     */
    public static void appliquerFiltreBlancSiSombre(ImageView... images) {
        if (MainApp.isModeSombre()) {
            ColorAdjust filtreBlanc = new ColorAdjust();
            filtreBlanc.setBrightness(1.0);
            
            for (ImageView img : images) {
                if (img != null) {
                    img.setEffect(filtreBlanc);
                }
            }
        } else {
            for (ImageView img : images) {
                if (img != null) {
                    img.setEffect(null);
                }
            }
        }
    }
}