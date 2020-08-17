package com.geekbrains.krilov.serverNetty;

import com.geekbrains.krilov.serverNetty.AuthService.AuthService;
import com.geekbrains.krilov.serverNetty.AuthService.BaseAuthService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    private AuthService authService;

    private boolean isAuthorised = false;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String input = (String) msg;

        if (isAuthorised) {
            ctx.fireChannelRead(input);
            return;
        }

        if (input.split(" ")[0].equals("/auth")) {
            this.authService = new BaseAuthService();
            authService.start();
            String login = input.split(" ")[1];
            String password = input.split(" ")[2];
            isAuthorised = authService.logIn(login, password);

            if (isAuthorised) {
                ctx.pipeline().addLast(new DataHandler(login));
            } else {
                //todo  отправить сообщение с ошибкой авторизации
            }

        }

    }
}
