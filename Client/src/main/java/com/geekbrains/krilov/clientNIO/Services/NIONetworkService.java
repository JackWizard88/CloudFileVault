package com.geekbrains.krilov.clientNIO.Services;

import com.geekbrains.krilov.ByteCommands;
import com.geekbrains.krilov.clientNIO.Callback;
import com.geekbrains.krilov.clientNIO.Controllers.ClientController;
import com.geekbrains.krilov.clientNIO.Controllers.ScreenController;
import javafx.application.Platform;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NIONetworkService {

    private final String host;
    private final int port;
    private SocketChannel channel;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(1);

    public NIONetworkService(int port, String host) {
        this.host = host;
        this.port = port;
    }

    public void connectAndRead() throws Exception {
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        Selector selector = Selector.open();
        channel.register(selector, SelectionKey.OP_CONNECT);
        channel.connect(new InetSocketAddress(host, port));

        executorService.execute(() -> {
            while (true) {
                try {
                    selector.select();
                    for (SelectionKey key : selector.selectedKeys()) {
                        if (key.isConnectable()) {
                            channel.finishConnect();
                            key.interestOps(SelectionKey.OP_READ);
                        } else if (key.isReadable()) {
                            System.out.println("incoming data");
                            ByteBuffer buffer = ByteBuffer.allocate(1);
                            channel.read(buffer);

                            byte commandByte = buffer.get();

                            if (commandByte == ByteCommands.AUTH_ACCEPTED_COMMAND && ClientController.getInstance().getCurrentState() == ClientController.Status.DEMAND_REGISTRATION) {
                                ClientController.getInstance().setCurrentState(ClientController.Status.REGISTERED);
                                ScreenController.getInstance().setWorkScene();
                            } else if (commandByte == ByteCommands.AUTH_DECLINED_COMMAND) {
                                ScreenController.getInstance().showErrorMessage("Auth denied", null);
                            }
                        }
                    }
                } catch (IOException e) {
                    ScreenController.getInstance().showErrorMessage("Сервер авторизации недоступен", () -> System.exit(0));
                }
            }
        });
    }

    public void sendData(ByteBuffer buf, Callback callback) {
        if (buf != null) {
            try {
                channel.write(buf);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (callback != null) {
                    callback.callback();
                }
            }
        }
    }


}
