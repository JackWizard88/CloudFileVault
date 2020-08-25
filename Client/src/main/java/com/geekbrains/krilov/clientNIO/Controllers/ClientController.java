package com.geekbrains.krilov.clientNIO.Controllers;

import com.geekbrains.krilov.clientNIO.Callback;
import com.geekbrains.krilov.clientNIO.Services.NIONetworkService;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientController {

    public enum Status {
        IDLE, DEMAND_REGISTRATION, REGISTERED
    }

    private final String AUTH_COMMAND = "/auth ";
    private final String REG_COMMAND = "/reg ";

    private Status currentState = Status.IDLE;


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

    public NIONetworkService getNetworkService() {
        return nns;
    }

    public void run() {

        executorService.execute(() -> {
            try {
                nns.connectAndRead();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    public Status getCurrentState() {
        return currentState;
    }

    public void setCurrentState(Status currentState) {
        this.currentState = currentState;
    }

    public void sendRegMessage(String login, String password, Callback callback) throws IOException {
        String data = REG_COMMAND + login + " " + password;
        ByteBuffer buf = ByteBuffer.wrap(data.getBytes());
        nns.sendData(buf, () -> callback.callback());
    }

    public void sendAuthMessage(String login, String password) throws IOException {
        String data = AUTH_COMMAND + login + " " + password;
        ByteBuffer buf = ByteBuffer.wrap(data.getBytes());
        nns.sendData(buf, null);
    }
}
