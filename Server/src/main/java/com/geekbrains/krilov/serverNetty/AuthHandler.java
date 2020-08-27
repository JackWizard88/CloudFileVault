package com.geekbrains.krilov.serverNetty;

import com.geekbrains.krilov.ByteCommands;
import com.geekbrains.krilov.serverNetty.AuthService.AuthService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.ByteBuffer;
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
            ByteBuffer answer = ByteBuffer.allocate(1);
            isAuthorised = authService.logIn(login, password);

            if (isAuthorised) {
                authKiller.interrupt();
                answer.put(ByteCommands.AUTH_ACCEPTED_COMMAND);
                ctx.writeAndFlush(answer);
                System.out.println(login + " auth accepted");
                ctx.pipeline().addLast(new DataHandler(login));
            } else {
                ctx.writeAndFlush(ByteCommands.AUTH_DECLINED_COMMAND);
                System.out.println(login + " auth declined");
            }
        } else if (input.split(" ")[0].equals("/reg")) {
            System.out.println("incoming reg data...");
            String login = input.split(" ")[1];
            String password = input.split(" ")[2];
            authService.registerNewUser(login, password);
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
