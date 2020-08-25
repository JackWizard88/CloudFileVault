package com.geekbrains.krilov.clientNIO.Services;

import com.geekbrains.krilov.clientNIO.Callback;
import com.geekbrains.krilov.clientNIO.Controllers.ScreenController;

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
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

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

                while (true) {
                    try {
                        selector.select();
                        for (SelectionKey key : selector.selectedKeys()) {
                            if (key.isConnectable()) {
                                channel.finishConnect();
                                key.interestOps(SelectionKey.OP_READ);
                            } else if (key.isReadable()) {
                                ByteBuffer buffer = ByteBuffer.allocate(1024);
                                buffer.clear();
                                channel.read(buffer);
                                //todo тут надо обработать данные от сервера
                            }
                        }
                    } catch (IOException e) {
                        ScreenController.getInstance().showErrorMessage("Сервер авторизации недоступен", () -> {
                            System.exit(0);
                        });
                    }
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
                callback.callback();
            }
        }
    }


}
