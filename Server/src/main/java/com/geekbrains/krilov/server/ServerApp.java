package com.geekbrains.krilov.server;

import com.geekbrains.krilov.server.handlers.ClientHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ServerApp {

    private final int port;
    private ServerSocket serverSocket;

    public ServerApp(int port) {
        this.port = port;
        start();
    }

    public static void main(String[] args) {
        new ServerApp(8189);
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server STARTED on port " + port);
            while (true) {
                System.out.println("Connection awaiting...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected");
                createClientHandler(clientSocket);
            }
        } catch (SocketException se) {
            System.err.println("Server Socket closed");
        } catch (IOException e) {
            System.err.println("IO Error");
        }
    }

    private void createClientHandler(Socket clientSocket) {
        ClientHandler clientHandler = new ClientHandler(this, clientSocket);
        clientHandler.run();
    }

}
