package com.geekbrains.krilov.clientNIO;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.channels.SelectionKey.OP_WRITE;


public class ClientAppNIO {

    static final int PORT = 8189;
    static final  String ADDRESS = "localhost";

    private File file;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(1);
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));


    private void run() throws Exception {
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        Selector selector = Selector.open();
        channel.register(selector, SelectionKey.OP_CONNECT);
        channel.connect(new InetSocketAddress(ADDRESS, PORT));
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(2);

        executorService.execute(() -> {
            while (true) {

                try {
                    String line = reader.readLine();
                    if (line.equals("/end") || line.equals("/exit") ) break;
                    else queue.put(line);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SelectionKey key = channel.keyFor(selector);
                key.interestOps(OP_WRITE);
                selector.wakeup();

            }

            System.exit(0);

        });

        while (true) {
            selector.select();

            for (SelectionKey key : selector.selectedKeys()) {
                if (key.isConnectable()) {
                    channel.finishConnect();
                    key.interestOps(SelectionKey.OP_READ);
                } else if (key.isWritable()) {
                    String line = queue.poll();
                    if (line != null) {
                        channel.write(ByteBuffer.wrap(line.getBytes()));
                    }
                    key.interestOps(SelectionKey.OP_READ);
                    break;
                } else if (key.isReadable()) {
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    buffer.clear();
                    channel.read(buffer);
                    if (key.readyOps() == key.interestOps()) {    // КОСТЫЛЬ ЧТОБЫ НЕ ВЫВОДИТЬ ПУСТУЮ СТРОКУ. НЕ ЗНАЮ КАК ОБОЙТИ ЭТО
                        String line = new String(buffer.array());
                        System.out.println("Server: " + line);
                    }
                }
            }
        }

    }

    public static void main(String[] args) throws Exception{
        try {
            new ClientAppNIO().run();
        } catch (IOException e) {
            System.out.println("Disconnected");
        }
    }

}
