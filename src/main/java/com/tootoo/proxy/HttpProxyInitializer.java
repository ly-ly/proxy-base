package com.tootoo.proxy;

import com.tootoo.config.ProxyConfig;
import com.tootoo.proxy.frontend.FrontendHandler;
import com.tootoo.proxy.ssl.HttpsAwareHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

public class HttpProxyInitializer extends ChannelInitializer<SocketChannel> {
    private final ProxyConfig proxyConfig;
    private final SslContext sslCtx;

    public HttpProxyInitializer(ProxyConfig proxyConfig) throws Exception {
        this.proxyConfig = proxyConfig;
        this.sslCtx = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline()
            .addLast(new LoggingHandler(LogLevel.DEBUG))
            .addLast(new HttpServerCodec())
            .addLast(new HttpsAwareHandler(sslCtx))
            .addLast(new FrontendHandler(proxyConfig));
    }
} 