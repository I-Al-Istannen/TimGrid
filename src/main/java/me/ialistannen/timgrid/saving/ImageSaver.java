package me.ialistannen.timgrid.saving;

import java.awt.image.BufferedImage;

import javafx.scene.image.Image;

/**
 * Saves an image
 */
public interface ImageSaver {

    /**
     * Clears any cache you might have built
     */
    void clearCache();

    /**
     * Saves an image to a String
     *
     * @param image The image to save
     *
     * @return The saved image
     */
    String save(Image image);

    /**
     * Loads an image from a String
     *
     * @param string The String to load from
     *
     * @return The reconstructed image
     */
    Image load(String string);

    /**
     * Checks if two images are equal
     *
     * @param first The first image
     * @param second The second image
     *
     * @return True if they are equal
     */
    default boolean areEqual(BufferedImage first, BufferedImage second) {
        if (first.getWidth() != second.getWidth() || first.getHeight() != second.getHeight()) {
            return false;
        }
        if (first.getType() != second.getType()) {
            return false;
        }
        for (int x = 0; x < first.getWidth(); x++) {
            for (int y = 0; y < first.getHeight(); y++) {
                if (first.getRGB(x, y) != second.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }
}
