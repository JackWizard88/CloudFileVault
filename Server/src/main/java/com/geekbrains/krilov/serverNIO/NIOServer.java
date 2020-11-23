package com.geekbrains.krilov.serverNIO;

import com.geekbrains.krilov.FileUtility;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

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
                        channel.write(ByteBuffer.wrap("/info Please use \"/auth username\" to LogIn".getBytes()));
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
                String userid = lineParts[1].trim();
                key.attach(userid);
                System.out.println(userid);
                FileUtility.createDirectory("./" + userid);
                System.out.println("client authorised as " + key.attachment());
            } else ((SocketChannel) key.channel()).write(ByteBuffer.wrap("/info user cannot be empty".getBytes()));
        } else if (key.attachment() != null) {
            if (line.startsWith("/get")) {
                System.out.println(line);
                //TODO Server sends FILE
            } else if (line.startsWith("/send")) {
                System.out.println(line);
                //TODO Server saves FILE
            } else if (line.startsWith("/list")) {
                List<Path> list = Files.list(Paths.get("./" + key.attachment().toString().trim())).collect(Collectors.toList());
                String dirList = "/list " + FileUtility.sendFileList(list);
                ((SocketChannel) key.channel()).write(ByteBuffer.wrap(dirList.getBytes()));

            }
        } else ((SocketChannel) key.channel()).write(ByteBuffer.wrap("/info Please LogIn first".getBytes()));
    }

    public static void main(String[] args) throws IOException {
        try {
            new Thread(new NIOServer()).start();
        } catch (IOException e) {
            System.out.println("Disconnected");
        }
    }
}
