package me.ialistannen.timgrid.model;

import java.util.Optional;

import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import me.ialistannen.timgrid.util.Util;
import me.ialistannen.timgrid.util.dialog.NumberInputDialog;

/**
 * An Icon label
 */
public class IconDisplay extends ImageView {

    private ContextMenu contextMenu;

    /**
     * @param image The image to use
     */
    public IconDisplay(Image image) {
        super(image);

        setOnDragDetected(event -> {
            Dragboard db;
            if (event.isMiddleButtonDown()) {
                db = startDragAndDrop(TransferMode.COPY);
            }
            else {
                db = startDragAndDrop(TransferMode.MOVE);
            }

            ClipboardContent content = new ClipboardContent();
            Image scale = Util.scale(image, 100);
            db.setDragView(scale);

            content.putImage(scale);
            db.setContent(content);

            event.consume();
        });

        setOnContextMenuRequested(clickEvent -> {
            if (contextMenu != null && contextMenu.isShowing()) {
                return;
            }
            contextMenu = new ContextMenu();

            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(event -> {
                if (getParent() instanceof Pane) {
                    ((Pane) getParent()).getChildren().remove(this);
                }
            });
            MenuItem rotate = new MenuItem("Rotate");
            rotate.setOnAction(event -> {
                double currentRotation = getRotate();
                Dialog<Double> dialog = new NumberInputDialog<>(new DoubleSpinnerValueFactory(0, 360), currentRotation);
                Optional<Double> newRotation = dialog.showAndWait();

                newRotation.ifPresent(this::setRotate);
            });
            contextMenu.getItems().addAll(deleteItem, rotate);

            contextMenu.show(this, Side.TOP, clickEvent.getX(), clickEvent.getY());
        });
    }
}
