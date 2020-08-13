package com.geekbrains.krilov.clientNIO.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;


public class RegScreenController {

    @FXML
    private TextField loginTextField;

    @FXML
    private PasswordField passTextField1;

    @FXML
    private PasswordField passTextField2;

    @FXML
    private Button registerButton;

    @FXML
    private Button cancelButton;

    @FXML
    void initialize() {

        registerButton.setOnAction(e -> checkRegDataAndSend());

        cancelButton.setOnAction(e -> closeRegWindow());
    }

    private void checkRegDataAndSend() {

        String login = loginTextField.getText().trim();
        String password = passTextField1.getText().trim();
        String password2 = passTextField2.getText().trim();

        if (!login.isEmpty() && !password.isEmpty()) {
            if (password.equals(password2)) {
                try {
                    ClientController.getInstance().sendRegMessage(login, password);
                } catch (IOException e) {
                    e.printStackTrace();
                    showErrorMessage("Ошибка");
                } finally {
                    System.out.println("registration data successfully sent to server");
                    closeRegWindow();
                }

            }
            else showErrorMessage("Пароли не совпадают");
        } else showErrorMessage("Необходимо заполнить все поля");

    }

    public void closeRegWindow() {
        try {
            ScreenController.getInstance().setAuthScreen();
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

}
