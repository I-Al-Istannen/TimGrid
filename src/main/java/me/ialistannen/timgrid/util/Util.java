package me.ialistannen.timgrid.util;

import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.imgscalr.Scalr;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.text.Font;

/**
 * Some static utility methods
 */
public class Util {

    /**
     * @param image The {@link Image} to scale
     * @param targetWidth The target width
     *
     * @return The scaled image
     */
    public static Image scale(Image image, @SuppressWarnings("SameParameterValue") int targetWidth) {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        bufferedImage = Scalr.resize(bufferedImage, targetWidth);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }


    /**
     * Shows an error alert
     *
     * @param title The title of the error
     * @param header The header
     * @param content The content
     * @param throwable The exception
     */
    public static void showError(String title, String header, String content, Throwable throwable) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        TextArea textArea = new TextArea(getExceptionStacktrace(throwable));
        textArea.setFont(Font.font("Monospaced"));
        alert.getDialogPane().setExpandableContent(textArea);

        alert.show();
    }

    /**
     * @param throwable The {@link Throwable}
     *
     * @return The Stacktrace as a String
     */
    private static String getExceptionStacktrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}