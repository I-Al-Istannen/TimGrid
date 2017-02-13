package me.ialistannen.timgrid;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * The main class
 */
public class Main extends Application {

    private Stage primaryStage;

    private static Main instance;

    {
        instance = this;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        FXMLLoader loader = new FXMLLoader(Main.class.getResource("view/main/MainPane.fxml"));
        Pane pane = loader.load();
        Scene scene = new Scene(pane);

        primaryStage.setScene(scene);

        primaryStage.setTitle("Grid");
        {
            List<Image> icons = getIconImages();
            primaryStage.getIcons().add(
                    icons.get(ThreadLocalRandom.current().nextInt(icons.size()))
            );
        }
        primaryStage.show();
    }

    private List<Image> getIconImages() {
        List<Image> list = new ArrayList<>();
        list.add(getIconImage("icon"));
        list.add(getIconImage("icon_2"));
        list.add(getIconImage("icon_3"));
        list.add(getIconImage("icon_4"));
        list.add(getIconImage("icon_5"));

        return list;
    }

    private Image getIconImage(String name) {
        return new Image(
                getClass().getResourceAsStream("/images/" + name + ".png")
        );
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static Main getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        launch(Main.class);
    }
}
