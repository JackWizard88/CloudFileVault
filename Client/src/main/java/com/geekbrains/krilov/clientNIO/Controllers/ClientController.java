package com.geekbrains.krilov.clientNIO.Controllers;

import com.geekbrains.krilov.ByteCommands;
import com.geekbrains.krilov.FileInfo;
import com.geekbrains.krilov.clientNIO.Callback;
import com.geekbrains.krilov.clientNIO.Services.NIONetworkService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
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
                try {
                    ScreenController.getInstance().setWorkScene();
                    Platform.runLater(() -> ScreenController.getInstance().getCurrentController().update());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (answer == ByteCommands.AUTH_DECLINED_COMMAND) {
                ScreenController.getInstance().showErrorMessage("Ошибка авторизации. Неверные данные", null);
                currentState = Status.IDLE;
            }
        }).start();

    }

    public String getServerRootPath() throws IOException {

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
        }
        return rootPath;

    }

    public List<FileInfo> getServerFileList(Path path) throws IOException {
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

}
