package me.ialistannen.timgrid.control.main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Pair;
import me.ialistannen.timgrid.Main;
import me.ialistannen.timgrid.model.BaseGrid;
import me.ialistannen.timgrid.model.IconDisplay;
import me.ialistannen.timgrid.saving.SavedGrid;
import me.ialistannen.timgrid.util.Util;

import javax.imageio.ImageIO;

/**
 * A controller for the Main pane
 */
public class MainPaneController {

    @FXML
    private SplitPane splitPane;

    @FXML
    private ListView<Image> imageList;

    private BaseGrid baseGrid;

    @FXML
    void initialize() {
        baseGrid = new BaseGrid(10, 10);
        splitPane.getItems().add(baseGrid);
        splitPane.setDividerPositions(0.2);

        imageList.setCellFactory(param -> new ListCell<Image>() {
            @Override
            protected void updateItem(Image item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                IconDisplay value = new IconDisplay(item);

                setOnDragDetected(event -> {
                    Dragboard db = startDragAndDrop(TransferMode.COPY);

                    ClipboardContent content = new ClipboardContent();

                    Image scale = Util.scale(item, 100);
                    content.putImage(scale);
                    db.setContent(content);

                    db.setDragView(scale);

                    event.consume();
                });

                value.setPreserveRatio(true);
//                value.fitHeightProperty().bind(this.heightProperty());
                value.fitWidthProperty().bind(this.widthProperty().subtract(10));
                setGraphic(value);
            }
        });

        imageList.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                Image selectedItem = imageList.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    removeImage(selectedItem);
                }
            }
            event.consume();
        });

        addImage(new Image("/images/wing.png"));
    }

    /**
     * @param image The Image to add
     */
    private void addImage(Image image) {
        imageList.getItems().add(image);
    }

    /**
     * @param image The image to remove
     */
    private void removeImage(Image image) {
        imageList.getItems().remove(image);
    }


    @FXML
    void onAddImageClicked(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("All images", "*.png", "*.jpg", "*.jpeg"),
                new ExtensionFilter("JPG files", "*.jpg", "*.jpeg"),
                new ExtensionFilter("PNG files", "*.png")
        );
        fileChooser.setSelectedExtensionFilter(fileChooser.getExtensionFilters().get(0));

        fileChooser.setTitle("Select an Image to add");
        File file = fileChooser.showOpenDialog(Main.getInstance().getPrimaryStage());

        if (file == null) {
            return;
        }

        try {
            BufferedImage read = ImageIO.read(file);
            addImage(SwingFXUtils.toFXImage(read, null));
        } catch (IOException e) {
            e.printStackTrace();
            Util.showError(
                    "Can't read image",
                    "Couldn't read the image file",
                    "You can find a more detailed message below",
                    e
            );
        }
    }

    @FXML
    void onResizeClicked(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("view/main/ResizePopup.fxml"));
            Pane pane = loader.load();
            ResizePopupController controller = loader.getController();

            Dialog<Pair<Integer, Integer>> dialog = new Dialog<>();
            dialog.getDialogPane().setContent(pane);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.setResultConverter(param -> {
                if (param != ButtonType.OK) {
                    return null;
                }
                return new Pair<>(controller.getWidth(), controller.getHeight());
            });

            Optional<Pair<Integer, Integer>> result = dialog.showAndWait();

            result.ifPresent(pair -> baseGrid.resize(pair.getKey(), pair.getValue()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onSaveGrid(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a File to save it to");
        chooser.getExtensionFilters().add(new ExtensionFilter("Nice save file", "*.nsf"));
        chooser.setSelectedExtensionFilter(chooser.getExtensionFilters().get(0));

        File saveFile = chooser.showSaveDialog(Main.getInstance().getPrimaryStage());
        if (saveFile == null) {
            return;
        }

        AtomicBoolean blockClosing = new AtomicBoolean(true);

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Saving...");
        alert.setHeaderText("I am currently saving.");
        alert.setOnCloseRequest(closeEvent -> {
            if (blockClosing.get()) {
                closeEvent.consume();
            }
        });
        alert.show();

        SavedGrid grid = new SavedGrid(baseGrid);
        Thread saverThread = new Thread(() -> {
            String json = grid.toJson();
            try {
                Files.write(
                        saveFile.toPath(),
                        Collections.singletonList(json),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
                );
                blockClosing.set(false);
                Platform.runLater(alert::close);
            } catch (IOException e) {
                Util.showError("Error saving", "An error occurred saving the file", e.getMessage(), e);
            }
        }, "Grid-Saver");

        saverThread.start();
    }

    @FXML
    void onLoadGrid(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load a file to read from");
        chooser.getExtensionFilters().add(new ExtensionFilter("Nice save file", "*.nsf"));
        chooser.setSelectedExtensionFilter(chooser.getExtensionFilters().get(0));

        File saveFile = chooser.showOpenDialog(Main.getInstance().getPrimaryStage());
        if (saveFile == null) {
            return;
        }

        try {
            String collect = Files.readAllLines(saveFile.toPath()).stream().collect(Collectors.joining("\n"));
            SavedGrid savedGrid = SavedGrid.fromJson(collect);
            savedGrid.applyToBaseGrid(baseGrid);
        } catch (IOException e) {
            Util.showError("Error loading", "An error occurred loading the file", e.getMessage(), e);
        }
    }
}
