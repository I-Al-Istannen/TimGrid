package me.ialistannen.timgrid.util;

import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import org.imgscalr.Scalr;

import javafx.application.Platform;
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

    /**
     * Runs a runnable later, blocking until it was run
     *
     * @param callable The {@link Callable} to execute
     * @param <V> The type of the return value from the {@link Callable}
     *
     * @return The return value of the {@link Callable}
     *
     * @throws RuntimeException wrapping any exception that might occur
     */
    public static <V> V runLaterBlocking(Callable<V> callable) {
        try {
            if (Platform.isFxApplicationThread()) {
                return callable.call();
            }
            Semaphore semaphore = new Semaphore(1);
            AtomicReference<V> value = new AtomicReference<>();
            runLaterUnchecked(() -> {
                value.set(callable.call());
                semaphore.release();
            });

            semaphore.acquire();

            return value.get();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Runs the Runnable on the FX application thread
     *
     * @param runnable The {@link UncheckedRunnable} to run
     */
    public static void runLaterUnchecked(UncheckedRunnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }

        Platform.runLater(runnable);
    }

    /**
     * An unchecked {@link Runnable}
     */
    @FunctionalInterface
    public interface UncheckedRunnable extends Runnable {

        @Override
        default void run() {
            try {
                runIt();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }

        /**
         * Runs this runnable
         *
         * @throws Throwable Any exception you might throw
         */
        void runIt() throws Throwable;
    }
}