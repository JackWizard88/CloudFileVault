package com.geekbrains.krilov.clientNIO.Controllers;

import com.geekbrains.krilov.clientNIO.Callback;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class ScreenController {

    private static ScreenController screencontroller;
    private Parent authScreen;
    private Parent workScreen;
    private Parent regScreen;
    private Stage stage;

    private FXMLLoader regLoader;
    private FXMLLoader authLoader;
    private FXMLLoader workLoader;

    private Scene authScene;
    private Scene workScene;
    private Scene regScene;
    private Scene currentScene;

    private BaseController currentController;

    private ScreenController(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
    }

    public static synchronized ScreenController getInstance(Stage primaryStage) throws Exception {
        if (screencontroller == null) {
            screencontroller = new ScreenController(primaryStage);
        }
        return screencontroller;
    }

    public static synchronized ScreenController getInstance() {
        return screencontroller;
    }

    public Scene getCurrentScene() {
        return currentScene;
    }

    public BaseController getCurrentController() {
        return currentController;
    }

    public void setRegScene() throws IOException {
        if (regScreen == null) {
            regLoader = new FXMLLoader(getClass().getResource("/fxml/WorkScreen.fxml"));
            this.regScreen = regLoader.load();
            this.regScene = new Scene(regScreen);
        }
        currentController = regLoader.getController();
        Platform.runLater(() -> stage.setScene(regScene));
        currentScene = regScene;
    }

    public  void setAuthScene() throws IOException {
        if (authScreen == null) {
            authLoader = new FXMLLoader(getClass().getResource("/fxml/AuthScreen.fxml"));
            this.authScreen = authLoader.load();
            this.authScene = new Scene(authScreen);
        }
        currentController = authLoader.getController();
        Platform.runLater(() -> stage.setScene(authScene));
        currentScene = authScene;
    }

    public void setWorkScene() throws IOException {
        if (workScreen == null) {
            workLoader = new FXMLLoader(getClass().getResource("/fxml/WorkScreen.fxml"));
            this.workScreen = workLoader.load();
            this.workScene = new Scene(workScreen);
        }
        currentController = workLoader.getController();
        Platform.runLater(() -> stage.setScene(workScene));
        currentScene = workScene;
    }

    public void showErrorMessage(String errorMessage, Callback callback) {
        Platform.runLater(() ->{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText(null);
            alert.setContentText(errorMessage);
            alert.showAndWait();
            callback.callback();
        });
    }

    public void showInfoMessage(String infoMessage) {
        Platform.runLater(() ->{
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("INFORMATION");
            alert.setHeaderText(null);
            alert.setContentText(infoMessage);
            alert.showAndWait();
        });
    }
}
