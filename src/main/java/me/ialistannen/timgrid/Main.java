package me.ialistannen.timgrid;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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

        primaryStage.show();
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
