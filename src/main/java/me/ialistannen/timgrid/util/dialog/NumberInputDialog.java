package me.ialistannen.timgrid.util.dialog;

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

            return numberInput.getValue();
        });

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().setContent(numberInput);

        setOnShown(event -> numberInput.requestFocus());
    }
}
