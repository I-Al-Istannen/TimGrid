package me.ialistannen.timgrid.control.about;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.effect.Bloom;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import me.ialistannen.timgrid.Main;

/**
 * The controller for the About window
 */
public class AboutWindowController {

    @FXML
    private Label about;

    @FXML
    private TextFlow textFlow;

    @FXML
    void initialize() {
        about.setEffect(new Bloom(0.1));
        textFlow.getChildren().addAll(
                getLabel("This program was developed by "),
                getLink("I Al Istannen", "https://github.com/I-Al-Istannen"),
                new Text("\n"),
                getLabel("(Who is terrible at designing things)", "small", "content-text"),
                new Text("\n\n"),
                getLabel("You can find the source code on "),
                getLink("GitHub", "https://github.com/I-Al-Istannen/TimGrid"),
                new Text("\n\n\n\n\n\n\n\n"),
                getLink("Picture by Raakile", "http://raakile.deviantart.com/art/Black-Mage-356147620")
        );
    }

    private Hyperlink getLink(String name, String target) {
        return getLink(name, target, "link");
    }

    private Hyperlink getLink(String name, String target, String styleClass) {
        Hyperlink hyperlink = new Hyperlink(name);
        hyperlink.setOnAction(event ->
                Main.getInstance().getHostServices().showDocument(target)
        );
        hyperlink.getStyleClass().add(styleClass);
        return hyperlink;
    }

    private Label getLabel(String text, String... styleClasses) {
        Label textElement = new Label(text);
        textElement.getStyleClass().addAll(styleClasses);
        return textElement;
    }

    private Label getLabel(String text) {
        return getLabel(text, "content-text");
    }
}
