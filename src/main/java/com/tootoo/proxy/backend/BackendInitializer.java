package com.tootoo.proxy.backend;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class BackendInitializer extends ChannelInitializer<SocketChannel> {
    private final Channel inboundChannel;

    public BackendInitializer(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline()
            .addLast(new LoggingHandler(LogLevel.DEBUG))
            .addLast(new HttpClientCodec())
            .addLast(new BackendHandler(inboundChannel));
    }
} 