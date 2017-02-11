package me.ialistannen.timgrid.util.dialog;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;

/**
 * A dialog to accept a number
 */
public class NumberInputDialog <T extends Number> extends Dialog<T> {

    private final Spinner<T> numberInput;

    /**
     * @param factory The value factory
     */
    @SuppressWarnings("unused")
    public NumberInputDialog(SpinnerValueFactory<T> factory) {
        this(factory, null);
    }

    /**
     * @param factory The value factory
     * @param defaultValue The default value. Nullable
     */
    public NumberInputDialog(SpinnerValueFactory<T> factory, T defaultValue) {
        numberInput = new Spinner<>(factory);
        numberInput.setEditable(true);

        numberInput.getEditor().setOnAction(event -> {
            // let the input field fire the button
            for (ButtonType buttonType : getDialogPane().getButtonTypes()) {
                if (buttonType == ButtonType.OK) {
                    Button button = (Button) getDialogPane().lookupButton(buttonType);
                    button.fire();
                    return;
                }
            }
        });

        numberInput.getEditor().setTextFormatter(new TextFormatter<Integer>(change -> {
            try {
                factory.getConverter().fromString(change.getControlNewText());
                return change;
            } catch (RuntimeException e) {
                return null;
            }
        }));

        if (defaultValue != null) {
            numberInput.getValueFactory().setValue(defaultValue);
        }

        setResultConverter(param -> {
            if (param != ButtonType.OK) {
                return null;
            }

            return numberInput.getValueFactory().getConverter().fromString(numberInput.getEditor().getText());
        });

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().setContent(numberInput);

        numberInput.requestFocus();

        numberInput.getEditor().focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // YAY... You know, we need to modify it AFTER it gained focus
                Platform.runLater(() ->
                        numberInput.getEditor().positionCaret(numberInput.getEditor().getText().length())
                );
            }
        });
        // YAY... You know, we need to modify it AFTER it was shown...
        setOnShown(event -> Platform.runLater(numberInput::requestFocus));
    }
}
