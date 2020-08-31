package com.geekbrains.krilov.clientNIO.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class AuthScreenController extends BaseController implements Initializable {

    @FXML
    private TextField loginText;

    @FXML
    private PasswordField passwordText;

    @FXML
    private Button buttonOk;

    @FXML
    private Button buttonCancel;

    @FXML
    private Button buttonRegister;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buttonRegister.setOnAction(e -> register());
        buttonCancel.setOnAction(e -> System.exit(0));
        buttonOk.setOnAction(e -> sendAuthData());
    }

    private void sendAuthData() {
        ClientController.getInstance().setCurrentState(ClientController.Status.DEMAND_REGISTRATION);
        String login = loginText.getText().trim();
        String pass = passwordText.getText().trim();
        try {
            ClientController.getInstance().sendAuthMessage(login, pass);
        } catch (IOException e) {
            System.out.println("error sending auth message");
            showErrorMessage("connection lost");
        }
    }

    private void register() {
        try {
            ScreenController.getInstance().setRegScene();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showErrorMessage(String errorMessage) {
        Platform.runLater(() ->{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText(null);
            alert.setContentText(errorMessage);
            alert.showAndWait();
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

    @Override
    public void update() {
    }
}
