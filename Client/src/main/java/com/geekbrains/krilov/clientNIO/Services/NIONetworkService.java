package com.geekbrains.krilov.clientNIO.Services;

import com.geekbrains.krilov.ByteCommands;
import com.geekbrains.krilov.FileInfo;
import com.geekbrains.krilov.clientNIO.Callback;
import com.geekbrains.krilov.clientNIO.Controllers.ScreenController;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

public class NIONetworkService {

    private final String host;
    private final int port;
    private SocketChannel channel;
    private DataOutputStream out;
    private DataInputStream in;

    public NIONetworkService(int port, String host) {
        this.host = host;
        this.port = port;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public DataInputStream getIn() {
        return in;
    }

    public void connect() throws Exception {
        InetSocketAddress serverAddress = new InetSocketAddress(host, port);
        channel = SocketChannel.open(serverAddress);
        out = new DataOutputStream(channel.socket().getOutputStream());
        in = new DataInputStream(channel.socket().getInputStream());
    }

    public void sendData(ByteBuffer buf, Callback callback) {
        if (buf != null) {
            try {
                channel.write(buf);
            } catch (IOException e) {
                ScreenController.getInstance().showErrorMessage("Сервер недоступен", null);
            } finally {
                if (callback != null) {
                    callback.callback();
                }
            }
        }
    }



}
