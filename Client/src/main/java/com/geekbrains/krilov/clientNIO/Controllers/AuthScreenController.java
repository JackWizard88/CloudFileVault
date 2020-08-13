package com.geekbrains.krilov.clientNIO.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class AuthScreenController implements Initializable {

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
        //тут отправляем пакет данных на сервер для регистрации
    }

    private void register() {
        try {
            ScreenController.getInstance().setRegScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
