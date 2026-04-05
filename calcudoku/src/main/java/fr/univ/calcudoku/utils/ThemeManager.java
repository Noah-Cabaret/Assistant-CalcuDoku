package fr.univ.calcudoku.utils;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;

import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Applique ou retire le thème sombre sur une scène JavaFX.
 * Gère aussi la couleur des icônes des boutons.
 */
public class ThemeManager {

    /**
     * Bascule le thème sombre sur la scène et met à jour la couleur des icônes.
     * @param scene la scène à styler
     * @param sombre true pour activer le mode sombre
     * @param boutonsAvecIcones boutons dont l'icône doit changer de couleur
     */
    public static void appliquerModeSombre(Scene scene, boolean sombre, Button... boutonsAvecIcones) {
        if (scene != null) {
            String cssPath = ThemeManager.class.getResource(Constantes.CHEMIN_CSS_SOMBRE).toExternalForm();
            if (sombre && !scene.getStylesheets().contains(cssPath)) {
                scene.getStylesheets().add(cssPath);
            } else if (!sombre) {
                scene.getStylesheets().remove(cssPath);
            }
        }

        Color iconColor = sombre ? Color.WHITE : Color.BLACK;
        for (Button btn : boutonsAvecIcones) {
            if (btn != null && btn.getGraphic() instanceof FontIcon) {
                ((FontIcon) btn.getGraphic()).setIconColor(iconColor);
            }
        }
    }
}
