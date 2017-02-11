package me.ialistannen.timgrid.util.dialog;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.Pane;
import javafx.util.Pair;
import me.ialistannen.timgrid.control.util.WidthHeightInputDialogController;

/**
 * A width input dialog
 */
public class WidthInputDialog extends Dialog<Pair<Integer, Integer>> {

    private WidthHeightInputDialogController controller;
    
    /**
     * Creates a new {@link WidthInputDialog}
     */
    public WidthInputDialog(int initialWidth, int initialHeight) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/me/ialistannen/timgrid/view/util/WidthHeightInputDialog.fxml")
            );
            Pane pane = loader.load();
            getDialogPane().getStylesheets().add("/me/ialistannen/timgrid/view/util/WidthHeightInputDialog.css");
            getDialogPane().getStyleClass().add("background-pane");
            getDialogPane().setContent(pane);

            controller = loader.getController();

            controller.setOwnerDialog(this);

            controller.setHeight(initialHeight);
            controller.setWidth(initialWidth);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        setResultConverter(param -> {
            if (param != ButtonType.OK) {
                return null;
            }

            return new Pair<>(controller.getWidth(), controller.getHeight());
        });

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    }
}
