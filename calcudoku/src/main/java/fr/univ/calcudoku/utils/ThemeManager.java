package fr.univ.calcudoku.utils;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;

import org.kordamp.ikonli.javafx.FontIcon;

public class ThemeManager {

    public static void appliquerModeSombre(Scene scene, boolean sombre, Button... boutonsAvecIcones) {
        if (scene != null) {
            String cssPath = ThemeManager.class.getResource("/styles/sombre.css").toExternalForm();
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
