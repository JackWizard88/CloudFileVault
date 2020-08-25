package com.geekbrains.krilov.clientNIO;

import com.geekbrains.krilov.clientNIO.Controllers.ClientController;
import com.geekbrains.krilov.clientNIO.Controllers.ScreenController;
import javafx.application.Application;
import javafx.stage.Stage;

public class ClientApp extends Application {

    static final int PORT = 8189;
    static final  String ADDRESS = "localhost";

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("CFV CloudFileVault");
        ScreenController.getInstance(primaryStage).setAuthScene();
        ClientController.getInstance(PORT, ADDRESS).run();
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
