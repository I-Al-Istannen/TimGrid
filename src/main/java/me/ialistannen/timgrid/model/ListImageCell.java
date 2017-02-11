package me.ialistannen.timgrid.model;

import java.util.HashMap;

import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import me.ialistannen.timgrid.util.Util;

/**
 * A Cell for a ListView containing an image
 */
public class ListImageCell extends ListCell<Image> implements ImageSupplier {

    public static final int LIST_WIDTH = 150;

    {
        setOnDragDetected(event -> {
            Dragboard dragboard = startDragAndDrop(TransferMode.COPY);
            dragboard.setDragView(snapshot(null, null));
            HashMap<DataFormat, Object> format = new HashMap<>();
            Image image = getImage();

            if (image == null) {
                return;
            }

            format.put(DataFormat.IMAGE, Util.scale(image, 100));
            dragboard.setContent(format);
        });
    }

    @Override
    protected void updateItem(Image item, boolean empty) {
        super.updateItem(item, empty);

        // this cell may be reused, so clear it appropriately
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        ImageView imageView = new ImageView(item);
        imageView.setSmooth(true);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(LIST_WIDTH);
        setGraphic(imageView);
    }

    /**
     * @return The image
     */
    @Override
    public Image getImage() {
        if (getGraphic() instanceof ImageView) {
            return ((ImageView) getGraphic()).getImage();
        }
        return null;
    }
}
