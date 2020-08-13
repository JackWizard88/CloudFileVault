package com.geekbrains.krilov.clientNIO.Services;

import com.geekbrains.krilov.clientNIO.Controllers.ClientController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.channels.SelectionKey.OP_WRITE;

public class NIONetworkService {

    private final String host;
    private final int port;
    private ClientController controller;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(1);
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public NIONetworkService(int port, String host) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws Exception {
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        Selector selector = Selector.open();
        channel.register(selector, SelectionKey.OP_CONNECT);
        channel.connect(new InetSocketAddress(host, port));
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(2);

        executorService.execute(() -> {
            while (true) {

                try {
                    String line = reader.readLine();
                    if (line.equals("/end") || line.equals("/exit") ) break;
                    else ;  //todo тут отправляем данные на сервер
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

            }

            System.exit(0);

        });

        while (true) {
            selector.select();

            for (SelectionKey key : selector.selectedKeys()) {
                if (key.isConnectable()) {
                    channel.finishConnect();
                    key.interestOps(SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    buffer.clear();
                    channel.read(buffer);
                    //todo тут надо обработать данныве от сервера
                }
            }
        }

    }


}
