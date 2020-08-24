package com.geekbrains.krilov.clientNIO.Controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ScreenController {

    private static ScreenController screencontroller;
    private Parent authScreen;
    private Parent workScreen;
    private Parent regScreen;
    private Stage stage;

    private Scene authScene;
    private Scene workScene;
    private Scene regScene;

    private ScreenController(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        this.authScreen = FXMLLoader.load(getClass().getResource("/fxml/AuthScreen.fxml"));
        this.workScreen = FXMLLoader.load(getClass().getResource("/fxml/WorkScreen.fxml"));
        this.regScreen = FXMLLoader.load(getClass().getResource("/fxml/RegScreen.fxml"));

        this.regScene = new Scene(regScreen, 300, 250);
        this.authScene = new Scene(authScreen, 300, 250);
    }

    public static synchronized ScreenController getInstance(Stage primaryStage) throws Exception {
        if (screencontroller == null) {
            screencontroller = new ScreenController(primaryStage);
        }

        return screencontroller;
    }

    public static synchronized ScreenController getInstance() throws Exception {
        return screencontroller;
    }

    public void setRegScreen() {
        stage.setScene(regScene);
    }

    public  void setAuthScreen() {
        stage.setScene(authScene);
    }
}
