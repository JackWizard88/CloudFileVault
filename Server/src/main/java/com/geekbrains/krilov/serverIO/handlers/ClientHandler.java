package com.geekbrains.krilov.serverIO.handlers;

import com.geekbrains.krilov.FileUtility;
import com.geekbrains.krilov.serverIO.ServerApp;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


public class ClientHandler {

    private final ServerApp serverApp;
    private final Socket clientSocket;

    private DataInputStream in;
    private DataOutputStream out;

    private String id = null;

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    public ClientHandler(ServerApp serverApp, Socket socket) {
        this.serverApp = serverApp;
        this.clientSocket = socket;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void run() {
        handle(clientSocket);
    }

    private void handle(Socket socket) {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            executorService.execute(() -> {
                try {
                    readData();
                } catch (IOException e) {
                    System.err.println("Connection with " + id + " was closed!");
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readData() throws IOException {

        while (true) {

            String data = in.readUTF();
            String[] dataParts = data.split("\\s+", 3);
            String command = dataParts[0];

            switch (command) {
                case ("/auth") :
                    if (dataParts.length > 1) {
                        auth(dataParts[1]);
                    } else sendMessage("login is empty");
                    break;

                case ("/list") :
                    sendList();
                    break;

                case ("/send") :
                    if (dataParts.length > 1) {
                        sendFile(dataParts[1]);
                    }
                    break;

                case ("/get") :
                    if (dataParts.length > 1) {
                        getFile(dataParts[1]);
                    }
                    break;

                case ("/delete") :
                    if (dataParts.length > 1) {
                        deleteFile(dataParts[1]);
                    }
                    break;

                default:
                    sendMessage("no such command" + command);
            }
        }
    }

    //тут авторизируем пользователя присваивая ему id
    private void auth(String id) {
        if (id != null) {
            this.id = id;
            System.out.println("id " + this.id + " logged in");

            try {
                FileUtility.createDirectory("./Server/" + id);
                out.writeUTF("/info [server]: logged in as " + this.id);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //тут отправляем список файлов пользователю,
    // если пользователь не null, иначе просим авторизироваться
    private void sendList() {
        if (id != null) {
            try {
                List<Path> list = Files.list(Paths.get("./Server/" + id + "/")).collect(Collectors.toList());
                FileUtility.sendFileList(out, list);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else sendMessage("please login first");
    }

    //тут мы принимаем файл от пользователя и сохраняем в его папку,
    // если пользователь не null, иначе просим авторизироваться
    private void sendFile(String fileName) {
        if (id != null) {
            File file = new File("./Server/" + id + "/" + fileName);
            FileUtility.getFile(in, file);

            sendMessage("File saved");


        } else sendMessage("please login first");
    }

    //тут мы отправляем файл пользователю,
    // если пользователь не null, иначе просим авторизироваться
    private void getFile(String fileName) throws IOException {
        if (id != null) {
            File file = new File("./Server/" + id + "/" + fileName);
            FileUtility.sendFile(out, file);
            System.out.println("File sent");
        } else sendMessage("please login first");
    }

    //тут мы удаляем файл из облака по запросу пользователя
    // если пользователь не null, иначе просим авторизироваться
    private void deleteFile(String fileName) {
        if (id != null) {
            System.out.println("File deleted");
        } else sendMessage("please login first");
    }

    //отправка сообщений пользователю
    private void sendMessage(String msg) {
        try {
            out.writeUTF("/info " + msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
