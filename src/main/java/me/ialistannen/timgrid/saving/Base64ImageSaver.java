package me.ialistannen.timgrid.saving;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;

/**
 * Saves images as Base64
 */
public class Base64ImageSaver implements ImageSaver {

    private static final Base64.Decoder DECODER = Base64.getDecoder();
    private static final Base64.Encoder ENCODER = Base64.getEncoder();

    private final Map<BufferedImage, String> cache = new HashMap<>();
    private final Map<String, BufferedImage> stringToImageCache = new HashMap<>();

    /**
     * Clears any cache you might have built
     */
    @Override
    public void clearCache() {
        cache.clear();
        stringToImageCache.clear();
    }

    /**
     * Saves an image to a String
     *
     * @param image The image to save
     *
     * @return The saved image
     */
    @Override
    public String save(Image image) {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        for (Entry<BufferedImage, String> entry : cache.entrySet()) {
            if (areEqual(bufferedImage, entry.getKey())) {
                return entry.getValue();
            }
        }
        String encode = encode(toBytes(SwingFXUtils.fromFXImage(image, null)));
        cache.put(bufferedImage, encode);
        return encode;
    }

    /**
     * Turns an image to bytes
     *
     * @param image The {@link BufferedImage} to convert
     *
     * @return The bytes the image consists of
     */
    private byte[] toBytes(BufferedImage image) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return outputStream.toByteArray();
    }

    /**
     * @param data The data to encode
     *
     * @return The encoded String
     */
    private String encode(byte[] data) {
        return ENCODER.encodeToString(data);
    }

    /**
     * Loads an image from a String
     *
     * @param string The String to load from
     *
     * @return The reconstructed image
     */
    @Override
    public Image load(String string) {
        if (stringToImageCache.containsKey(string)) {
            return SwingFXUtils.toFXImage(stringToImageCache.get(string), null);
        }
        BufferedImage image = fromBytes(decode(string));
        stringToImageCache.put(string, image);
        return SwingFXUtils.toFXImage(image, null);
    }

    /**
     * Turns bytes to an image (<em>magic!</em>)
     *
     * @param bytes The image bytes
     *
     * @return The image
     */
    private BufferedImage fromBytes(byte[] bytes) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        try {
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param data The encoded String
     *
     * @return The decoded data
     */
    private byte[] decode(String data) {
        return DECODER.decode(data);
    }
}
