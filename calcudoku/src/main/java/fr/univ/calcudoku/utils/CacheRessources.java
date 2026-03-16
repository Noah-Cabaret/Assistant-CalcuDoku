package fr.univ.calcudoku.utils;

import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache pour optimiser le chargement des ressources (images).
 * Evite de charger plusieurs fois la même image en mémoire.
 */
public class CacheRessources {
    /** Cache stocké en mémoire des images */
    private static final Map<String, Image> cacheImages = new HashMap<>();

    /**
     * Récupère une image depuis le cache ou la charge si elle n'existe pas.
     * @param cheminRessource le chemin de la ressource image
     * @return l'image chargée ou null si non trouvable
     */
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