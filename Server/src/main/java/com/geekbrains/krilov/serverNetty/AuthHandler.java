package com.geekbrains.krilov.serverNetty;

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
        String input = buf.toString(Charset.forName("utf-8"));

        if (isAuthorised) {
            ctx.fireChannelRead(input);
            return;
        }

        if (input.split(" ")[0].equals("/auth")) {      //todo заменить на байтовую команду??
            String login = input.split(" ")[1];
            this.login = login;
            String password = input.split(" ")[2];

            isAuthorised = authService.logIn(login, password);

            if (isAuthorised) {
                authKiller.interrupt();
                ctx.pipeline().addLast(new DataHandler(login));
                String answer = "/auth " + "granted"; //todo заменить на байтовую команду??
                ctx.writeAndFlush(ByteBuffer.wrap(answer.getBytes()));
            } else {
                String answer = "/auth " + "denied"; //todo заменить на байтовую команду??
                ctx.writeAndFlush(answer.getBytes());
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
    }

    private void connectionTimeOutKiller(ChannelHandlerContext ctx) {
        authKiller = new Thread(() -> {
            try {
                Thread.sleep(120000);
                System.out.println("Client disconnected by timeout");

                ctx.close();
            } catch (InterruptedException e) {
                System.out.println("Auth succeeded. TimeOut-Killer Interrupted");
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
