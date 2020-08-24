package com.geekbrains.krilov.clientNIO.Controllers;

import com.geekbrains.krilov.clientNIO.Services.NIONetworkService;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientController {

    private static ClientController clientController;
    private NIONetworkService nns;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(1);

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

        executorService.execute(() -> {
            try {
                nns.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }


    public void sendRegMessage(String login, String password) throws IOException {
    }
}
