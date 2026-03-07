package fr.univ.calcudoku.utils;

import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class CacheRessources {
    private static final Map<String, Image> cacheImages = new HashMap<>();

    public static Image getImage(String cheminRessource) {
        if (!cacheImages.containsKey(cheminRessource)) {
            try {
                InputStream is = CacheRessources.class.getResourceAsStream(cheminRessource);
                if (is != null) {
                    cacheImages.put(cheminRessource, new Image(is));
                } else {
                    System.err.println("Image introuvable");
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return cacheImages.get(cheminRessource);
    }
}