package me.ialistannen.timgrid.model;

import java.awt.image.BufferedImage;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;

/**
 * The grid
 */
public class BaseGrid extends GridPane {

    {
        getStylesheets().add(
                BaseGrid.class.getResource("/me/ialistannen/timgrid/view/main/BaseGrid.css").toString()
        );
    }

    public BaseGrid(int rows, int columns) {
        resize(rows, columns);
    }

    /**
     * Clears the pane and resizes it
     *
     * @param columns The column amount
     * @param rows The row amount
     */
    public void resize(int columns, int rows) {
        if (columns < 1 || rows < 1) {
            throw new IllegalArgumentException("columns and rows need to be > 0");
        }
        getChildren().clear();
        getColumnConstraints().clear();
        getRowConstraints().clear();

        for (int i = 0; i < columns; i++) {
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setPercentWidth(100.0 / columns);
            constraints.setHalignment(HPos.CENTER);
            getColumnConstraints().add(constraints);
        }

        for (int i = 0; i < rows; i++) {
            RowConstraints constraints = new RowConstraints();
            constraints.setPercentHeight(100.0 / rows);
            constraints.setValignment(VPos.CENTER);
            getRowConstraints().add(constraints);
        }

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                Pane flowPane = new FlowPane();
                flowPane.getStyleClass().add("cell");
                flowPane.setMinSize(0, 0);

                flowPane.setOnDragEntered(event -> flowPane.getStyleClass().add("dragged-over"));
                flowPane.setOnDragExited(event -> flowPane.getStyleClass().remove("dragged-over"));
                flowPane.setOnDragOver(event -> event.acceptTransferModes(TransferMode.COPY_OR_MOVE));

                flowPane.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.DELETE) {
                        flowPane.getChildren().clear();
                    }
                });

                flowPane.setOnDragDropped(event -> {
                    // already an element inside
                    if (!flowPane.getChildren().isEmpty()) {
                        return;
                    }

                    IconDisplay display = null;
                    // we only like our Images OR a nice ListCell. Yes, that is a nightmare code style wise.
                    if (!(event.getGestureSource() instanceof IconDisplay)) {
                        if (event.getGestureSource() instanceof ListCell) {
                            ListCell<?> cell = (ListCell<?>) event.getGestureSource();
                            if (cell.getGraphic() instanceof IconDisplay) {
                                display = (IconDisplay) cell.getGraphic();
                                display = new IconDisplay(display.getImage());
                            }
                        }
                        if (display == null) {
                            return;
                        }
                    }
                    else {
                        if (event.getTransferMode() == TransferMode.COPY) {
                            display = new IconDisplay(((IconDisplay) event.getGestureSource()).getImage());
                        }
                        else {
                            display = (IconDisplay) event.getGestureSource();
                        }
                    }

                    display.fitWidthProperty().bind(flowPane.widthProperty());
                    display.fitHeightProperty().bind(flowPane.heightProperty());
                    flowPane.getChildren().add(display);
                    event.setDropCompleted(true);
                });
                add(flowPane, x, y);
            }
        }
    }

    /**
     * Removes all Images
     */
    public void clear() {
        for (Node node : getChildren()) {
            if (node instanceof Pane) {
                ((Pane) node).getChildren().clear();
            }
        }
    }

    /**
     * @param column The column
     * @param row The row
     * @param image The image
     */
    public void setImage(int column, int row, BufferedImage image) {
        IconDisplay display = new IconDisplay(SwingFXUtils.toFXImage(image, null));
        for (Node node : getChildren()) {
            if (!(node instanceof Pane)) {
                continue;
            }
            if (getColumnIndex(node) == column && getRowIndex(node) == row) {
                Pane pane = (Pane) node;
                pane.getChildren().clear();
                pane.getChildren().add(display);
                display.fitWidthProperty().bind(pane.widthProperty());
                display.fitHeightProperty().bind(pane.heightProperty());
                return;
            }
        }
    }
}
