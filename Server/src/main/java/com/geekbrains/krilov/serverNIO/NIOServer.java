package com.geekbrains.krilov.serverNIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOServer implements Runnable {
    private ServerSocketChannel server;
    private Selector selector;
    private ByteBuffer readBuffer = ByteBuffer.allocate(1024);

    public NIOServer() throws IOException {
        server = ServerSocketChannel.open();
        server.socket().bind(new InetSocketAddress(8189));
        server.configureBlocking(false);
        selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {

        try {

            System.out.println("server started");

            while (server.isOpen()) {

                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()) {
                        System.out.println("client accepted");
                        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ);
                        channel.write(ByteBuffer.wrap("Connected to Server. \nPlease use \"/auth username\" to LogIn".getBytes()));
                    }
                    if (key.isReadable()) {

                        ByteBuffer readBuffer = ByteBuffer.allocate(80);

                        try {
                            ((SocketChannel) key.channel()).read(readBuffer);
                        } catch (IOException e) {
                            System.out.println("client disconnected");
                            key.channel().close();
                            break;
                        }

                        String line = new String(readBuffer.array());
//                        System.out.println(line);

                        handle(key, line);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handle(SelectionKey key, String line) throws IOException {

        if (line.startsWith("/auth")) {
            String[] lineParts = line.split(" ", 2);
            if (lineParts[1] != null) {
                key.attach(lineParts[1]);
                System.out.println("client authorised as " + key.attachment());
            } else ((SocketChannel) key.channel()).write(ByteBuffer.wrap("user cannot be empty".getBytes()));
        } else if (key.attachment() != null) {
            if (line.startsWith("/get")) {
                System.out.println(line);
                //TODO Server sends FILE
            } else if (line.startsWith("/send")) {
                System.out.println(line);
                //TODO Server saves FILE
            } else if (line.startsWith("/list")) {
                System.out.println(line);
                ((SocketChannel) key.channel()).write(ByteBuffer.wrap("List of files".getBytes()));
                //TODO Server sends List of files
            }
        } else ((SocketChannel) key.channel()).write(ByteBuffer.wrap("Please LogIn first".getBytes()));
    }

    public static void main(String[] args) throws IOException {
        try {
            new Thread(new NIOServer()).start();
        } catch (IOException e) {
            System.out.println("Disconnected");
        }
    }
}
