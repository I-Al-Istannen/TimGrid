package me.ialistannen.timgrid.control.main;

import java.util.OptionalInt;

import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextFormatter;

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

        width.getEditor().setTextFormatter(getFormatter());
        height.getEditor().setTextFormatter(getFormatter());
    }

    private TextFormatter<String> getFormatter() {
        return new TextFormatter<>(change -> {
            OptionalInt parsed = parseInt(change.getControlNewText());
            if (!parsed.isPresent()) {
                return null;
            }
            return change;
        });
    }

    private OptionalInt parseInt(String string) {
        try {
            return OptionalInt.of(Integer.parseInt(string));
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
