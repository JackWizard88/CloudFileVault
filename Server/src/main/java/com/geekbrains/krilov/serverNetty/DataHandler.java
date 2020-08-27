package com.geekbrains.krilov.serverNetty;

import com.geekbrains.krilov.ByteCommands;
import com.geekbrains.krilov.PackageHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class DataHandler extends ChannelInboundHandlerAdapter {

    private final String login;

    public DataHandler(String login) {
        this.login = login;
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
                case ByteCommands.SEND_FILE_COMMAND:
                    System.out.println("STATE: Sending file to " + login);
                    PackageHandler.sendFile(msg, login);
                    break;
                case ByteCommands.DELETE_FILE_COMMAND:
                    System.out.println("STATE: Deleting file by " + login);
                    break;
                case ByteCommands.GET_SERVER_FILE_LIST_COMMAND:
                    System.out.println("STATE: Sending fileList to " + login);
                    sendFileList(ctx, msg);
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

    private void sendFileList(ChannelHandlerContext ctx, Object msg) {
        PackageHandler.getFileList(msg, login);
    }

    private void deleteFile(ChannelHandlerContext ctx, Object msg) {
        PackageHandler.DeleteFile(msg, login);
    }

    private void getFile(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        PackageHandler.receiveFile(buf, login);
    }

    private void sendFile(ChannelHandlerContext ctx, Object msg) {
        PackageHandler.getFileList(msg, login);
    }
}
