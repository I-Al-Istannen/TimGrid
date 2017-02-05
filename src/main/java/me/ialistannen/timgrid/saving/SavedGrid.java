package me.ialistannen.timgrid.saving;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import me.ialistannen.timgrid.model.BaseGrid;
import me.ialistannen.timgrid.model.IconDisplay;

import javax.imageio.ImageIO;

/**
 * A saved BaseGrid
 */
public class SavedGrid {
    private static final BufferedImageSerializer IMAGE_SERIALIZER = new BufferedImageSerializer();
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(BufferedImage.class, new BufferedImageDeserializer())
            .registerTypeAdapter(BufferedImage.class, IMAGE_SERIALIZER)
            .setPrettyPrinting()
            .create();

    private BufferedImage[][] images;

    public SavedGrid(BaseGrid grid) {
        images = new BufferedImage[grid.getRowConstraints().size()][grid.getColumnConstraints().size()];
        for (Node node : grid.getChildren()) {
            if (!(node instanceof FlowPane)) {
                continue;
            }
            if (((FlowPane) node).getChildren().isEmpty()) {
                continue;
            }
            Node child = ((FlowPane) node).getChildren().get(0);
            if (!(child instanceof IconDisplay)) {
                continue;
            }
            int rowIndex = GridPane.getRowIndex(node);
            int columnIndex = GridPane.getColumnIndex(node);
            images[rowIndex][columnIndex] = SwingFXUtils.fromFXImage(((IconDisplay) child).getImage(), null);
        }
    }

    /**
     * @param grid The grid to apply it to
     */
    public void applyToBaseGrid(BaseGrid grid) {
        grid.clear();
        grid.resize(images.length, images[0].length);
        for (int y = 0; y < images.length; y++) {
            BufferedImage[] image = images[y];
            for (int x = 0; x < image.length; x++) {
                BufferedImage bufferedImage = image[x];
                if (bufferedImage != null) {
                    grid.setImage(x, y, bufferedImage);
                }
            }
        }
    }

    /**
     * @param json The JSON string
     *
     * @return The reconstructed {@link SavedGrid}
     */
    public static SavedGrid fromJson(String json) {
        return GSON.fromJson(json, SavedGrid.class);
    }

    /**
     * @return This {@link SavedGrid} as a JSON String
     */
    public String toJson() {
        String json = GSON.toJson(this);
        IMAGE_SERIALIZER.cache.clear();
        return json;
    }

    @Override
    public String toString() {
        return "SavedGrid{" +
                "images=" +
                Arrays.stream(images)
                        .filter(Objects::nonNull)
                        .flatMap(Arrays::stream)
                        .filter(Objects::nonNull)
                        .count() +
                '}';
    }

    /**
     * @param image The image to encode
     *
     * @return The image bytes
     */
    private static byte[] imageToBytes(BufferedImage image) {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream(image.getWidth() * image.getHeight() * 8);
        try {
            ImageIO.write(image, "png", arrayOutputStream);
            return arrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error encoding image", e);
        }
    }

    /**
     * @param data The image bytes
     *
     * @return The image
     */
    private static BufferedImage bytesToImage(byte[] data) {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(data);
        try {
            return ImageIO.read(arrayInputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error decoding image", e);
        }
    }

    /**
     * Serializes a BufferedImage
     */
    private static class BufferedImageSerializer implements JsonSerializer<BufferedImage> {

        private List<Pair<BufferedImage, String>> cache = new ArrayList<>();

        /**
         * Gson invokes this call-back method during serialization when it encounters a field of the
         * specified type.
         * <p>
         * <p>In the implementation of this call-back method, you should consider invoking
         * {@link JsonSerializationContext#serialize(Object, Type)} method to create JsonElements for any
         * non-trivial field of the {@code src} object. However, you should never invoke it on the
         * {@code src} object itself since that will cause an infinite loop (Gson will call your
         * call-back method again).</p>
         *
         * @param src the object that needs to be converted to Json.
         * @param typeOfSrc the actual type (fully genericized version) of the source object.
         * @param context The context
         *
         * @return a JsonElement corresponding to the specified object.
         */
        @Override
        public JsonElement serialize(BufferedImage src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null) {
                return null;
            }
            Optional<String> cached = getCached(src);
            if (cached.isPresent()) {
                return new JsonPrimitive(cached.get());
            }

            byte[] encoded = imageToBytes(src);
            String string = new String(Base64.getEncoder().encode(encoded));

            cache.add(new Pair<>(src, string));

            return new JsonPrimitive(string);
        }

        private Optional<String> getCached(BufferedImage image) {
            for (Pair<BufferedImage, String> entry : cache) {
                // slow, but most likely still faster than writing it again (writing may take over a second)
                if (bufferedImagesEqual(image, entry.getKey())) {
                    return Optional.of(entry.getValue());
                }
            }
            return Optional.empty();
        }

        private static boolean bufferedImagesEqual(BufferedImage img1, BufferedImage img2) {
            if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
                for (int x = 0; x < img1.getWidth(); x++) {
                    for (int y = 0; y < img1.getHeight(); y++) {
                        if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                            return false;
                        }
                    }
                }
            }
            else {
                return false;
            }
            return true;
        }
    }

    private static class BufferedImageDeserializer implements JsonDeserializer<BufferedImage> {

        /**
         * Gson invokes this call-back method during deserialization when it encounters a field of the
         * specified type.
         * <p>In the implementation of this call-back method, you should consider invoking
         * {@link JsonDeserializationContext#deserialize(JsonElement, Type)} method to create objects
         * for any non-trivial field of the returned object. However, you should never invoke it on the
         * the same type passing {@code json} since that will cause an infinite loop (Gson will call your
         * call-back method again).
         *
         * @param json The Json data being deserialized
         * @param typeOfT The type of the Object to deserialize to
         * @param context The context
         *
         * @return a deserialized object of the specified type typeOfT which is a subclass of {@code T}
         *
         * @throws JsonParseException if json is not in the expected format of {@code typeofT}
         */
        @Override
        public BufferedImage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                JsonParseException {
            if (!json.isJsonPrimitive()) {
                throw new JsonParseException("Json not a String, while trying to deserialize Image: " + json);
            }

            return bytesToImage(Base64.getDecoder().decode(json.getAsString()));
        }
    }
}
