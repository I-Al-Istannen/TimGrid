package me.ialistannen.timgrid.control.main;

import java.util.OptionalInt;
import java.util.function.Supplier;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;

/**
 * The Controller for the Resize popup
 */
public class ResizePopupController {

    @FXML
    private Spinner<Integer> width;

    @FXML
    private Spinner<Integer> height;

    @FXML
    private void initialize() {
        width.setValueFactory(new IntegerSpinnerValueFactory(1, Integer.MAX_VALUE));
        height.setValueFactory(new IntegerSpinnerValueFactory(1, Integer.MAX_VALUE));
        width.setEditable(true);
        height.setEditable(true);

        width.getEditor().textProperty().addListener(getChangeListener(() -> width, () -> width.getEditor()));
        height.getEditor().textProperty().addListener(getChangeListener(() -> height, () -> height.getEditor()));
    }

    private ChangeListener<String> getChangeListener(Supplier<Spinner<Integer>> spinnerSupplier,
                                                     Supplier<TextField> fieldSupplier) {
        return (observable, oldValue, newValue) -> {
            OptionalInt parsed = parseInt(newValue);
            if (!parsed.isPresent()) {
                fieldSupplier.get().setText(oldValue);
                return;
            }
            if (parsed.getAsInt() < 1) {
                fieldSupplier.get().setText("1");
                return;
            }

            spinnerSupplier.get().getValueFactory().setValue(parsed.getAsInt());
        };
    }

    private OptionalInt parseInt(String string) {
        try {
            int i = Integer.parseInt(string);
            return OptionalInt.of(i);
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    int getWidth() {
        return width.getValue();
    }

    int getHeight() {
        return height.getValue();
    }

}
