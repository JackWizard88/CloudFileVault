package com.geekbrains.krilov.serverNetty;

import com.geekbrains.krilov.ByteCommands;
import com.geekbrains.krilov.FileInfo;
import com.geekbrains.krilov.FileUtility;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DataHandler extends ChannelInboundHandlerAdapter {

    private enum State {
        IDLE, RECEIVING_DATA
    }

    private final String SERVER_VAULT_CATALOG = "./vault/";
    private final String login;
    private final String homeDir;
    private State currentState = State.IDLE;

    private String fileName;
    private String pathName;
    private long fileLength;
    private long receivedFileSize;

    public DataHandler(String login) {
        this.login = login;
        this.homeDir = SERVER_VAULT_CATALOG + login + "/";
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf buf = ((ByteBuf) msg);

        while (buf.readableBytes() > 0) {


            if (currentState == State.IDLE) {

                byte commandByte = buf.readByte();

                switch (commandByte) {
                    case ByteCommands.GET_FILE_COMMAND:
                        System.out.println("STATE: Sending file to " + login);
                        break;
                    case ByteCommands.SERVER_ROOT_PATH_COMMAND:
                        System.out.println("STATE: Sending root path to " + login);
                        sendRootPath(ctx);
                        break;
                    case ByteCommands.SEND_FILE_COMMAND:
                        System.out.println("STATE: Receiving file from " + login);
                        getFile(ctx, buf);
                        break;
                    case ByteCommands.DELETE_FILE_COMMAND:
                        System.out.println("STATE: Deleting file by " + login);
                        deleteFile(ctx, buf);
                        break;
                    case ByteCommands.GET_SERVER_FILE_LIST_COMMAND:
                        System.out.println("STATE: Sending fileList to " + login);
                        sendFileList(ctx, buf);
                        break;
                    default:
                        System.out.println("unknown command byte");
                        break;
                }
            }

            if (currentState == State.RECEIVING_DATA){
                System.out.println("STATE: Receiving file data from " + login);
                getFileData(ctx, buf);
            }
        }


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void sendRootPath(ChannelHandlerContext ctx) throws IOException {
        ByteBuf tmp = Unpooled.buffer();
        tmp.writeByte(ByteCommands.SERVER_ROOT_PATH_COMMAND);
        tmp.writeInt(homeDir.length());
        tmp.writeBytes(homeDir.getBytes());
        ctx.writeAndFlush(tmp);
        tmp.clear();
    }

    private void sendFileList(ChannelHandlerContext ctx, ByteBuf buf) throws IOException {
        //получение директории запроса списка файлов
        int pathLength = buf.readInt();
        byte[] fileListBuf = new byte[pathLength];
        buf.readBytes(fileListBuf);

        String pathName = new String(fileListBuf, StandardCharsets.UTF_8);

        if (pathName.equals(Paths.get(homeDir).getParent().toString())) {
            pathName = homeDir;
        }

        //запрос списка файлов по указанному пути и упаковка в json
        String fileList = getFileListJson(pathName);

        //упаковка и отправка списка клиенту
        ByteBuf tmp = Unpooled.buffer();
        tmp.writeByte(ByteCommands.SEND_SERVER_FILE_LIST_COMMAND);
        tmp.writeInt(fileList.length());
        tmp.writeBytes(fileList.getBytes());
        ctx.writeAndFlush(tmp);
        tmp.clear();
    }

    private void deleteFile(ChannelHandlerContext ctx, ByteBuf buf) {
        int pathLength = buf.readInt();
        byte[] filePathBuf = new byte[pathLength];
        buf.readBytes(filePathBuf);
        String pathName = new String(filePathBuf, StandardCharsets.UTF_8);

        try {
            Files.walk(Paths.get(pathName))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
//            Files.delete(Paths.get(pathName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFile(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        //TODO тут отправка файла по запросу клиента
    }

    private void getFile(ChannelHandlerContext ctx, ByteBuf buf) {

        //получение длины имени файла
        int fileNameLength = buf.readInt();
        //получение имени файла
        byte[] fileNameBuf = new byte[fileNameLength];
        buf.readBytes(fileNameBuf);
        fileName = new String(fileNameBuf, StandardCharsets.UTF_8);
        //получение длины пути назначения
        int pathLength = buf.readInt();
        //получение пути назначения
        byte[] pathNameBuf = new byte[pathLength];
        buf.readBytes(pathNameBuf);
        pathName = new String(pathNameBuf, StandardCharsets.UTF_8);
        System.out.println("путь назначения: " + pathName);
        //получение длины файла
        fileLength = buf.readLong();

        if (fileLength > 0) {
            currentState = State.RECEIVING_DATA;
            receivedFileSize = 0;
        } else {
            try {
                System.out.println("создание каталога " + pathName + "/" + fileName);
                FileUtility.createDirectory(pathName + "/" + fileName);
                ByteBuf tmp = Unpooled.buffer();
                tmp.writeByte(ByteCommands.OK_COMMAND);
                ctx.writeAndFlush(tmp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void getFileData(ChannelHandlerContext ctx, ByteBuf buf) {
        //получение файла
        boolean append = true;

        try (FileOutputStream out = new FileOutputStream(pathName + "/" +  fileName, append)) {
            System.out.println("получение файла");
            while (buf.readableBytes() > 0) {
                System.out.println("получено:" + receivedFileSize + " из " + fileLength);
                int write = out.getChannel().write(buf.nioBuffer());
                receivedFileSize += write;
                buf.readerIndex(buf.readerIndex() + write);
                if (receivedFileSize == fileLength) {
                    ByteBuf tmp = Unpooled.buffer();
                    tmp.writeByte(ByteCommands.OK_COMMAND);
                    ctx.writeAndFlush(tmp);
                    currentState = State.IDLE;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFileListJson(String pathName) throws IOException {
        Gson gson = new Gson();
        List<FileInfo> list = Files.list(Paths.get(pathName)).map(FileInfo::new).collect(Collectors.toList());
        return gson.toJson(list);
    }
}
