package com.geekbrains.krilov.clientNIO.Controllers;

import com.geekbrains.krilov.ByteCommands;
import com.geekbrains.krilov.FileInfo;
import com.geekbrains.krilov.clientNIO.Callback;
import com.geekbrains.krilov.clientNIO.Services.NIONetworkService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientController {

    public enum Status {
        IDLE, DEMAND_REGISTRATION, REGISTERED
    }

    private final String AUTH_COMMAND = "/auth ";
    private final String REG_COMMAND = "/reg ";
    private static final int BUFFER_SIZE = 1024 * 1024 * 10;

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
                nns.connect();
            } catch (Exception e) {
                ScreenController.getInstance().showErrorMessage("Сервер недоступен" , () -> System.exit(0));
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
        new Thread(() -> {
            String data = AUTH_COMMAND + login + " " + password;
            ByteBuffer buf = ByteBuffer.wrap(data.getBytes());
            nns.sendData(buf, null);

            byte answer = 0;
            try {
                answer = ClientController.getInstance().getNetworkService().getIn().readByte();
            } catch (IOException e) {
                ScreenController.getInstance().showErrorMessage("Сервер разорвал соединение", () -> System.exit(0));
            }
            if (answer == ByteCommands.AUTH_ACCEPTED_COMMAND && currentState == Status.DEMAND_REGISTRATION) {
                currentState = Status.REGISTERED;

                Platform.runLater(() -> {
                    try {
                        ScreenController.getInstance().setWorkScene();
                        ScreenController.getInstance().getCurrentController().update();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } else if (answer == ByteCommands.AUTH_DECLINED_COMMAND) {
                ScreenController.getInstance().showErrorMessage("Ошибка авторизации. Неверные данные", null);
                currentState = Status.IDLE;
            }
        }).start();

    }

    public String getServerRootPath() throws IOException {
        System.out.print("Getting Root Path: ");
        //отправляем запрос на путь корневого каталога
        ByteBuffer buf = ByteBuffer.allocate(1);
        buf.put(ByteCommands.SERVER_ROOT_PATH_COMMAND);
        buf.flip();
        nns.sendData(buf, null);

        //получаем в ответ командный байт, длинну пути, и сам путь
        String rootPath = "";
        byte answerbyte = nns.getIn().readByte();
        if (answerbyte == ByteCommands.SERVER_ROOT_PATH_COMMAND) {
            int pathLength = nns.getIn().readInt();
            byte[] pathBuf = new byte[pathLength];
            int readByte = 0;
            while (readByte < pathLength) {
                readByte += nns.getIn().read(pathBuf);
            }
            rootPath = new String(pathBuf, StandardCharsets.UTF_8 );
            System.out.println(rootPath);
        }
        return rootPath;

    }

    public List<FileInfo> getServerFileList(Path path) throws IOException {
        System.out.println("Getting fileList for" + path.toString());
        //отправляем запрос списка файлов на сервер
        int bufSize = 1 + 4 + path.toString().length();
        ByteBuffer buf = ByteBuffer.allocate(bufSize);
        buf.put(ByteCommands.GET_SERVER_FILE_LIST_COMMAND);
        buf.putInt(path.toString().length());
        buf.put(path.toString().getBytes());
        buf.flip();
        nns.sendData(buf, null);

        //получаем в ответ json со списком
        byte answerbyte = nns.getIn().readByte();
        List<FileInfo> fileInfoList = null;

        if (answerbyte == ByteCommands.SEND_SERVER_FILE_LIST_COMMAND) {
            int listSize = nns.getIn().readInt();
            byte[] fileListBuf = new byte[listSize];
            int readByte = 0;
            while (readByte < listSize) {
                readByte += nns.getIn().read(fileListBuf);
            }
            Gson gson = new Gson();
            String listString = new String(fileListBuf, StandardCharsets.UTF_8);
            fileInfoList = gson.fromJson(listString, new TypeToken<List<FileInfo>>(){}.getType());
        }

        return fileInfoList;
    }

    public void deleteFile(Path path, Callback callback) {
        System.out.println("Delete file" + path.toString());
        int bufSize = 1 + 4 + path.toString().length();
        ByteBuffer buf = ByteBuffer.allocate(bufSize);
        buf.put(ByteCommands.DELETE_FILE_COMMAND);
        buf.putInt(path.toString().length());
        buf.put(path.toString().getBytes());
        buf.flip();
        nns.sendData(buf, () -> callback.callback());

    }

    public void sendFile(Path path, ProgressBar progressBar, Callback callback) throws IOException {
        System.out.println("Sending to server file: " + path.getFileName().toString());
        File srcFile = path.toFile();

        long fileSize = srcFile.length();
        byte[] fileName = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);

        //todo ДОБАВИТЬ ОТПРАВКУ ПУТИ НАЗНАЧЕНИЯ, ИНАЧЕ СОХРАНЯЕТ В КОРЕНЬ ПОЛЬЗОВАТЕЛЯ

        int bufSize = 1 + 4 + fileName.length + 8;
        ByteBuffer buf = ByteBuffer.allocate(bufSize);
        //коммандный байт
        buf.put(ByteCommands.SEND_FILE_COMMAND);
        //4 байта длина имени
        buf.putInt(fileName.length);
        //имя файла
        buf.put(fileName);
        //8 байт размер файла
        buf.putLong(srcFile.length());
        buf.flip();
        //отправляем буфер
        nns.sendData(buf, null);

        if (fileSize > 0) {
            try (FileInputStream in = new FileInputStream(srcFile);) {

                    //шлём сам файл если все прошло успешно
                    System.out.println("Sending file data");
                    ByteBuffer tmpBuf = ByteBuffer.allocate(BUFFER_SIZE);
                    long bytesSent = 0;

                    while (bytesSent < fileSize) {
                        System.out.println("sent: " + bytesSent + " / " + fileSize);
                        try {
                            int readByte = in.getChannel().read(tmpBuf);
                            bytesSent += readByte;
                            tmpBuf.flip();
                            nns.sendData(tmpBuf,null);
                            tmpBuf.clear();
                            float sentPercent = (float) bytesSent / fileSize;

                            if (progressBar != null) {
                                Platform.runLater(() -> progressBar.setProgress(sentPercent));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    System.out.println("sent: " + bytesSent + " / " + fileSize);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        try {
            byte answerByte = nns.getIn().readByte();
            if (answerByte == ByteCommands.OK_COMMAND) {
                callback.callback();
                if (progressBar != null) {
                    Platform.runLater(() -> progressBar.setProgress(0));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
