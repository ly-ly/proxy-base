package com.tootoo.proxy.ssl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

public class HttpsAwareHandler extends ChannelInboundHandlerAdapter {
    private final SslContext sslCtx;

    public HttpsAwareHandler(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            
            if (request.method().equals(HttpMethod.CONNECT)) {
                ctx.fireChannelRead(msg);
                return;
            }
            
            String uri = request.uri();
            if (uri.startsWith("https://")) {
                ctx.pipeline().addFirst("ssl", new SslHandler(sslCtx.newEngine(ctx.alloc())));
            }
        }
        ctx.fireChannelRead(msg);
    }
} 