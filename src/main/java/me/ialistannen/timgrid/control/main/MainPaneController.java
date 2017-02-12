package me.ialistannen.timgrid.control.main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Pair;
import me.ialistannen.timgrid.Main;
import me.ialistannen.timgrid.model.ListImageCell;
import me.ialistannen.timgrid.saving.Base64ImageSaver;
import me.ialistannen.timgrid.saving.ImageSaver;
import me.ialistannen.timgrid.util.Util;
import me.ialistannen.timgrid.util.dialog.WidthInputDialog;
import me.ialistannen.timgrid.view.GridAnchorPane;

import javax.imageio.ImageIO;

/**
 * The controller for the Main pane
 */
public class MainPaneController {
    private final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @FXML
    private SplitPane splitPane;

    @FXML
    private ListView<Image> imageList;

    @FXML
    private ScrollPane scrollPane;

    private GridAnchorPane grid;
    private final ImageSaver imageSaver = new Base64ImageSaver();

    @FXML
    void initialize() {
        {
            grid = new GridAnchorPane(20, 20);
            grid.setPrefSize(2000, 1000);

            Rectangle rectangle = new Rectangle(50, 50, Color.ROYALBLUE);
            rectangle.setTranslateX(200);
            rectangle.setTranslateY(260);

            grid.getChildren().add(rectangle);
            scrollPane.setContent(grid);
        }

        // glue divider in place
        {
            SplitPane.Divider divider = splitPane.getDividers().get(0);
            divider.positionProperty().addListener((observable, oldValue, newValue) ->
                    divider.setPosition(getDividerPosition(splitPane.getWidth()))
            );
        }

        // adjust divider to keep the list at the correct width 
        splitPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (Double.compare(newValue.doubleValue(), 0) == 0) {
                splitPane.setDividerPositions(0.2);
            }
            else {
                splitPane.setDividerPositions(getDividerPosition(newValue.doubleValue()));
            }
        });

        imageList.setCellFactory(param -> new ListImageCell());

        // Enable the deletion of Images in the list 
        imageList.setOnKeyPressed(event -> {
            if (event.getCode() != KeyCode.DELETE) {
                return;
            }
            Image selectedItem = imageList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                imageList.getItems().remove(selectedItem);
            }
        });

        try {
            imageList.getItems().add(
                    SwingFXUtils.toFXImage(
                            ImageIO.read(getClass().getResource("/images/wing.png")),
                            null
                    )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double getDividerPosition(double width) {
        // + 20 for some padding
        return (ListImageCell.LIST_WIDTH + 20) / width;
    }


    @FXML
    void onResizeGridCellSize(ActionEvent event) {
        WidthInputDialog dialog = new WidthInputDialog(grid.getGridWidth(), grid.getGridHeight());
        Optional<Pair<Integer, Integer>> pair = dialog.showAndWait();
        if (!pair.isPresent()) {
            return;
        }
        Pair<Integer, Integer> widthHeight = pair.get();

        grid.setGridSize(widthHeight.getKey(), widthHeight.getValue());
    }

    @FXML
    void onResizeGridSize(ActionEvent event) {
        WidthInputDialog dialog = new WidthInputDialog((int) grid.getWidth(), (int) grid.getHeight());
        Optional<Pair<Integer, Integer>> pair = dialog.showAndWait();
        if (!pair.isPresent()) {
            return;
        }
        Pair<Integer, Integer> widthHeight = pair.get();

        grid.setSize(widthHeight.getKey(), widthHeight.getValue());
    }


    @FXML
    void onLoadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose an image to add");
        fileChooser.getExtensionFilters().add(new ExtensionFilter(
                "Choose an image file",
                "*.png", "*.jpg", "*.gif", "*.jpeg"
        ));
        fileChooser.setSelectedExtensionFilter(fileChooser.getExtensionFilters().get(0));
        File file = fileChooser.showOpenDialog(Main.getInstance().getPrimaryStage());

        if (file == null) {
            return;
        }

        try {
            BufferedImage image = ImageIO.read(file);
            Image fxImage = SwingFXUtils.toFXImage(image, null);

            imageList.getItems().add(fxImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onSave(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a save file");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("A nice save file", "*.nsf"));
        fileChooser.setSelectedExtensionFilter(fileChooser.getExtensionFilters().get(0));

        File file = fileChooser.showSaveDialog(Main.getInstance().getPrimaryStage());

        if (file == null) {
            return;
        }

        JsonObject root = new JsonObject();
        {
            JsonArray array = new JsonArray();
            for (Image image : imageList.getItems()) {
                array.add(imageSaver.save(image));
            }
            root.add("images", array);
        }

        {
            JsonObject gridMeta = new JsonObject();
            gridMeta.addProperty("width", grid.getWidth());
            gridMeta.addProperty("height", grid.getHeight());
            gridMeta.addProperty("grid-width", grid.getGridWidth());
            gridMeta.addProperty("grid-height", grid.getGridHeight());
            root.add("grid-meta", gridMeta);
        }

        {
            JsonArray array = new JsonArray();
            for (Entry<Point2D, Node> entry : grid.getNodesWithGridPosition().entrySet()) {
                if (!(entry.getValue() instanceof ImageView)) {
                    continue;
                }
                ImageView imageView = (ImageView) entry.getValue();

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("x", entry.getKey().getX());
                jsonObject.addProperty("y", entry.getKey().getY());
                jsonObject.addProperty("image", imageSaver.save(imageView.getImage()));
                jsonObject.addProperty("rotate", imageView.getRotate());
                jsonObject.addProperty("width", imageView.getFitWidth());
                jsonObject.addProperty("height", imageView.getFitHeight());
                array.add(jsonObject);
            }
            root.add("grid", array);
        }

        String json = GSON.toJson(root);

        try {
            Files.write(
                    file.toPath(),
                    Collections.singletonList(json),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
            );
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Finished saving");
            alert.setHeaderText("Finished saving");
            alert.show();
        } catch (IOException e) {
            Util.showError(
                    "Error saving the file",
                    "Error saving the file",
                    "An error occurred while saving the file",
                    e
            );
        }
        imageSaver.clearCache();
    }

    @FXML
    void onLoad(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a save file");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("A nice save file", "*.nsf"));
        fileChooser.setSelectedExtensionFilter(fileChooser.getExtensionFilters().get(0));

        File file = fileChooser.showOpenDialog(Main.getInstance().getPrimaryStage());

        if (file == null) {
            return;
        }

        imageList.getItems().clear();
        grid.clear();

        try {
            String json = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)
                    .stream()
                    .collect(Collectors.joining("\n"));

            JsonObject root = GSON.fromJson(json, JsonObject.class);

            for (JsonElement image : root.get("images").getAsJsonArray()) {
                Image loaded = imageSaver.load(image.getAsString());
                imageList.getItems().add(loaded);
            }

            {
                JsonObject gridMeta = root.get("grid-meta").getAsJsonObject();
                int width = gridMeta.get("width").getAsInt();
                int height = gridMeta.get("height").getAsInt();
                int gridWidth = gridMeta.get("grid-width").getAsInt();
                int gridHeight = gridMeta.get("grid-height").getAsInt();

                grid.setGridSize(gridWidth, gridHeight);
                grid.setSize(width, height);
            }

            for (JsonElement element : root.get("grid").getAsJsonArray()) {
                JsonObject gridElement = element.getAsJsonObject();

                int x = gridElement.get("x").getAsInt();
                int y = gridElement.get("y").getAsInt();
                Image image = imageSaver.load(gridElement.get("image").getAsString());
                double rotate = gridElement.get("rotate").getAsDouble();
                double width = gridElement.get("width").getAsDouble();
                double height = gridElement.get("height").getAsDouble();

                grid.setImage(image, x, y, rotate, width, height);
            }
        } catch (IOException e) {
            Util.showError(
                    "Error loading the file",
                    "Error loading the file",
                    "An error occurred while loading the file",
                    e
            );
        }
    }
}
