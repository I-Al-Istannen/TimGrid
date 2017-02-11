package me.ialistannen.timgrid.control.main;

import java.io.IOException;
import java.util.Optional;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;
import me.ialistannen.timgrid.model.ListImageCell;
import me.ialistannen.timgrid.util.dialog.WidthInputDialog;
import me.ialistannen.timgrid.view.GridAnchorPane;

import javax.imageio.ImageIO;

/**
 * The controller for the Main pane
 */
public class MainPaneController {

    @FXML
    private SplitPane splitPane;

    @FXML
    private ListView<Image> imageList;

    @FXML
    private ScrollPane scrollPane;

    private GridAnchorPane grid;

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
            divider.positionProperty().addListener((observable, oldValue, newValue) -> {
                divider.setPosition(getDividerPosition(splitPane.getWidth()));
            });
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
}
