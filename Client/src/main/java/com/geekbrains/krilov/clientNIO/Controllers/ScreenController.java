package com.geekbrains.krilov.clientNIO.Controllers;

import com.geekbrains.krilov.clientNIO.Callback;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
    private Scene currentScene;

    private ScreenController(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        this.authScreen = FXMLLoader.load(getClass().getResource("/fxml/AuthScreen.fxml"));
        this.workScreen = FXMLLoader.load(getClass().getResource("/fxml/WorkScreen.fxml"));
        this.regScreen = FXMLLoader.load(getClass().getResource("/fxml/RegScreen.fxml"));

        this.regScene = new Scene(regScreen, 300, 250);
        this.authScene = new Scene(authScreen, 300, 250);
        this.workScene = new Scene(workScreen);
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

    public void setRegScene() {
        Platform.runLater(() -> stage.setScene(regScene));
        currentScene = regScene;
    }

    public  void setAuthScene() {
        Platform.runLater(() -> stage.setScene(authScene));
        currentScene = authScene;
    }

    public void setWorkScene() {
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
