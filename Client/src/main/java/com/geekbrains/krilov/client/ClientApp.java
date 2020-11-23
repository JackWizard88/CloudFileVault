package com.geekbrains.krilov.client;

import com.geekbrains.krilov.FileUtility;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientApp {

    private final String host = "localhost";
    private final int port = 8189;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private File file;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(1);

    public static void main(String[] args) {
        ClientApp app = new ClientApp();
        try {
            app.connect();
        } catch (IOException e) {
            System.err.println("Server is unreachable. Check Network settings");
        }
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());

        System.out.println("Connected to server");

        runReadThread(); //поток прослушки канала

        while (true) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line = reader.readLine();

            if (line.equals("/end") || line.equals("/exit") ) break;

            if (line.startsWith("/send")) {
                String[] dataParts = line.split("\\s+", 2);
                File file = new File(dataParts[1]);
                FileUtility.sendFile(out, file);
                continue;
            } else if (line.startsWith("/get")) {
                String[] dataParts = line.split("\\s+", 2);
                file = new File("./Client/" + dataParts[1]);
                out.writeUTF("/get " + file.getName());
                continue;
            }
            out.writeUTF(line);
        }

        System.exit(0);

    }

    private void runReadThread() {
        executorService.execute(() -> {
            while (true) {
                try {

                    String data = in.readUTF();
                    String[] dataParts = data.split("\\s+", 2);
                    String command = dataParts[0];

                    switch (command) {
                        case ("/list") :
                            showlist(dataParts[1]);
                            break;
                        case ("/send") :
                            FileUtility.getFile(in, file);
                            file = null;
                            break;
                        case ("/get") :
                            System.out.println(dataParts[0]);
                            break;
                        case ("/info") :
                            System.out.println(dataParts[1]);
                            break;
                    }

                } catch (IOException e) {
                    System.err.println("ReadThread interrupted!");
                    close();
                    return;
                }
            }
        });
    }

    private void showlist(String data) {

        String[] subStr;
        String delimeter = "//";
        subStr = data.split(delimeter);

        for(int i = 0; i < subStr.length - 1; i++) {
            System.out.println(subStr[i]);
        }
    }

    public void close() {
        try {
            socket.close();
            System.err.println("Server shut down! please restart app");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
