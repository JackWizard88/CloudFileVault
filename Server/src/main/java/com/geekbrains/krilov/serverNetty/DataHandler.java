package com.geekbrains.krilov.serverNetty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class DataHandler extends ChannelInboundHandlerAdapter {

    private final String login;

    public DataHandler(String login) {
        this.login = login;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //todo тут читаем данные от клиента
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
