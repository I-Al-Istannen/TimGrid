package me.ialistannen.timgrid.control.util;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.util.Duration;

/**
 * The Controller for the WidthHeightInputDialog.fxml file
 */
public class WidthHeightInputDialogController {

    @FXML
    private Spinner<Integer> widthSpinner;

    @FXML
    private Spinner<Integer> heightSpinner;

    private Dialog<?> ownerDialog;

    @FXML
    private void initialize() {
        widthSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, Integer.MAX_VALUE));
        heightSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, Integer.MAX_VALUE));

        widthSpinner.getEditor().focusedProperty().addListener(getFocusChangeListener(widthSpinner.getEditor()));
        heightSpinner.getEditor().focusedProperty().addListener(getFocusChangeListener(heightSpinner.getEditor()));

        widthSpinner.setEditable(true);
        heightSpinner.setEditable(true);
    }

    private ChangeListener<Boolean> getFocusChangeListener(TextField field) {
        return (observable, oldValue, newValue) -> {
            if (!newValue) {
                return;
            }

            // YAY... You know, we need to modify it AFTER it gained focus
            Platform.runLater(() ->
                    field.positionCaret(field.getText().length())
            );
        };
    }

    /**
     * @return The width
     */
    public int getWidth() {
        return widthSpinner.getValueFactory()
                .getConverter()
                .fromString(widthSpinner.getEditor().getText());
    }

    /**
     * @return The height
     */
    public int getHeight() {
        return heightSpinner.getValueFactory()
                .getConverter()
                .fromString(heightSpinner.getEditor().getText());
    }

    /**
     * @param width The width
     */
    public void setWidth(int width) {
        widthSpinner.getEditor().setText(Integer.toString(width));
    }

    /**
     * @param height The height
     */
    public void setHeight(int height) {
        heightSpinner.getEditor().setText(Integer.toString(height));
    }

    /**
     * @param ownerDialog The owner dialog
     */
    public void setOwnerDialog(Dialog<?> ownerDialog) {
        this.ownerDialog = ownerDialog;

        heightSpinner.getEditor().setOnAction(event -> fireOkayButton());
        widthSpinner.getEditor().setOnAction(event -> fireOkayButton());

        ownerDialog.setOnShown(event -> {
            PauseTransition pause = new PauseTransition(Duration.millis(50));
            pause.setOnFinished(event1 -> widthSpinner.requestFocus());
            pause.play();
        });
    }

    private void fireOkayButton() {
        Button button = (Button) ownerDialog.getDialogPane().lookupButton(ButtonType.OK);
        button.fire();
    }
}
