package com.geekbrains.krilov.serverNetty;

import com.geekbrains.krilov.ByteCommands;
import com.geekbrains.krilov.FileInfo;
import com.geekbrains.krilov.PackageHandler;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class DataHandler extends ChannelInboundHandlerAdapter {

    private final String login;
    private String currentDir;
    private String homeDir;

    public DataHandler(String login) {
        this.login = login;
        this.homeDir = "./vault/" + login;
        this.currentDir = homeDir;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf buf = ((ByteBuf) msg);

        while (buf.readableBytes() > 0) {

            byte commandByte = buf.readByte();

            switch (commandByte) {
                case ByteCommands.GET_FILE_COMMAND:
                    System.out.println("STATE: Receiving file from " + login);
                    break;
                case ByteCommands.SERVER_ROOT_PATH_COMMAND:
                    System.out.println("STATE: Sending root path to " + login);
                    sendRootPath(ctx);
                    break;
                case ByteCommands.SEND_FILE_COMMAND:
                    System.out.println("STATE: Sending file to " + login);
                    break;
                case ByteCommands.DELETE_FILE_COMMAND:
                    System.out.println("STATE: Deleting file by " + login);
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

    private void deleteFile(ChannelHandlerContext ctx, Object msg) {
        PackageHandler.DeleteFile(msg, login);
    }

    private void getFile(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        PackageHandler.receiveFile(buf, login);
    }

    private void sendFile(ChannelHandlerContext ctx, Object msg) {
        PackageHandler.sendFile(msg, login);
    }

    private String getFileListJson(String pathName) throws IOException {
        Gson gson = new Gson();
        List<FileInfo> list = Files.list(Paths.get(pathName)).map(FileInfo::new).collect(Collectors.toList());
        return gson.toJson(list);
    }
}
