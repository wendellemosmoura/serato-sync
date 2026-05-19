package main.java.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.ui.MainController;

/**
 * @author Wendel Lemos Moura
 */

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainController controller = new MainController();
        Scene scene = new Scene(controller.build(primaryStage), 560, 660);

        controller.applyInitialTheme(scene);

        primaryStage.setTitle("serato-sync");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(480);
        primaryStage.setMinHeight(480);

        java.io.InputStream iconStream = getClass().getResourceAsStream("/icon.png");
        if (iconStream != null) {
            primaryStage.getIcons().add(new javafx.scene.image.Image(iconStream));
        }

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}