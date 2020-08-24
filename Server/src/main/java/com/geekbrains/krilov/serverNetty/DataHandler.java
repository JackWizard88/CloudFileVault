package com.geekbrains.krilov.serverNetty;

import com.geekbrains.krilov.PackageHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

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
                case (byte) 25:
                    System.out.println("STATE: Receiving file from " + login);
                    PackageHandler.receiveFile(buf, login);
                    break;
                case (byte) 26:
                    System.out.println("STATE: Sending file to " + login);
                    PackageHandler.sendFile(msg, login);
                    break;
                case (byte) 27:
                    System.out.println("STATE: Deleting file by " + login);
                    PackageHandler.DeleteFile(msg, login);
                    break;
                case (byte) 28:
                    System.out.println("STATE: Sending fileList to " + login);
                    PackageHandler.sendList(msg, login);
                    break;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
