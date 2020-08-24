package com.geekbrains.krilov.serverNetty;

import com.geekbrains.krilov.serverNetty.AuthService.AuthService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    private AuthService authService;
    private Thread authKiller;

    private boolean isAuthorised = false;

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
        String input = (String) msg;

        if (isAuthorised) {
            ctx.fireChannelRead(input);
            return;
        }

        if (input.split(" ")[0].equals("/auth")) {      //todo заменить на байтовую команду
            String login = input.split(" ")[1];
            String password = input.split(" ")[2];

            isAuthorised = authService.logIn(login, password);

            if (isAuthorised) {
                authKiller.interrupt();
                ctx.pipeline().addLast(new DataHandler(login));
            }
        }

    }

    private void connectionTimeOutKiller(ChannelHandlerContext ctx) {
        authKiller = new Thread(() -> {
            try {
                Thread.sleep(60000);
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
