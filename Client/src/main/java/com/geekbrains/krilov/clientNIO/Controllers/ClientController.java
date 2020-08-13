package com.geekbrains.krilov.clientNIO.Controllers;

import com.geekbrains.krilov.clientNIO.Services.NIONetworkService;
import javafx.application.Platform;

import java.io.IOException;

public class ClientController {

    private static ClientController clientController;
    private NIONetworkService nns;

    public ClientController(int port, String address) {
        this.nns = new NIONetworkService(port, address);
    }

    public static synchronized ClientController getInstance(int port, String address) {
        if (clientController == null) {
            clientController = new ClientController(port, address);
        }
        return clientController;
    }

    public static synchronized ClientController getInstance() {
        return clientController;
    }

    public void run() {
        try {
            nns.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        runAuth();

    }

    private void runAuth() {

    }

    public void sendRegMessage(String login, String password) throws IOException {
    }
}
