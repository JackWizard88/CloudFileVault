package com.geekbrains.krilov.serverNetty;

import com.geekbrains.krilov.ByteCommands;
import com.geekbrains.krilov.FileUtility;
import com.geekbrains.krilov.serverNetty.AuthService.AuthService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.io.IOException;
import java.nio.charset.Charset;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    private AuthService authService;
    private Thread authKiller;

    private boolean isAuthorised = false;
    private String login;

    public AuthHandler(AuthService authService) {
        System.out.println("Client connected");
        this.authService = authService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        connectionTimeOutKiller(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        ByteBuf buf = (ByteBuf) msg;
        if (isAuthorised) {
            ctx.fireChannelRead(buf);
            return;
        }

        String input = buf.toString(Charset.forName("utf-8"));

        if (input.split(" ")[0].equals("/auth")) {  //todo заменить на байтовую команду??
            String login = input.split(" ")[1];
            this.login = login;
            String password = input.split(" ")[2];
            ByteBuf answer = Unpooled.buffer();
            isAuthorised = authService.logIn(login, password);

            if (isAuthorised) {
                authKiller.interrupt();
                answer.writeByte(ByteCommands.AUTH_ACCEPTED_COMMAND);
                ctx.writeAndFlush(answer);
                System.out.println(login + " auth accepted");
                ctx.pipeline().addLast(new DataHandler(login));
            } else {
                answer.writeByte(ByteCommands.AUTH_DECLINED_COMMAND);
                ctx.writeAndFlush(answer);
                System.out.println(login + " auth declined");
            }
        } else if (input.split(" ")[0].equals("/reg")) {
            System.out.println("incoming reg data...");
            String login = input.split(" ")[1];
            String password = input.split(" ")[2];
            authService.registerNewUser(login, password);
            try {
                FileUtility.createDirectory("./vault/" + login);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        authService.logOut(login);
        authKiller.interrupt();
    }

    private void connectionTimeOutKiller(ChannelHandlerContext ctx) {
        authKiller = new Thread(() -> {
            try {
                Thread.sleep(120000);
                System.out.println("Client disconnected by timeout");

                ctx.close();
            } catch (InterruptedException e) {
                System.out.println("TimeOut-Killer Interrupted");
            }
        });
        authKiller.start();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("Client disconnected");
        ctx.close();
    }
}
