package com.tootoo.proxy.frontend;

import com.tootoo.config.ProxyConfig;
import com.tootoo.proxy.backend.BackendInitializer;
import com.tootoo.proxy.util.ProxyUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrontendHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(FrontendHandler.class);
    
    private final ProxyConfig proxyConfig;
    private Channel outboundChannel;
    private HttpRequest currentRequest;

    public FrontendHandler(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            currentRequest = (HttpRequest) msg;
            logger.debug("Received request: {} {}", currentRequest.method(), currentRequest.uri());
            
            if (proxyConfig.isAuthentication()) {
                String auth = currentRequest.headers().get("Proxy-Authorization");
                if (!authenticate(auth)) {
                    logger.warn("Authentication failed for request");
                    sendAuthenticationRequired(ctx);
                    return;
                }
            }

            if (currentRequest.method() == HttpMethod.CONNECT) {
                handleConnectRequest(ctx, currentRequest);
                return;
            }

            if (outboundChannel == null) {
                String host = ProxyUtils.getTargetHost(currentRequest);
                int port = ProxyUtils.getTargetPort(currentRequest);
                
                Bootstrap b = new Bootstrap();
                b.group(ctx.channel().eventLoop())
                    .channel(ctx.channel().getClass())
                    .handler(new BackendInitializer(ctx.channel()))
                    .option(ChannelOption.AUTO_READ, false);

                ChannelFuture f = b.connect(host, port);
                outboundChannel = f.channel();
                f.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) {
                        if (future.isSuccess()) {
                            ctx.channel().read();
                        } else {
                            ctx.channel().close();
                        }
                    }
                });
            }

            outboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        ctx.channel().read();
                    } else {
                        future.channel().close();
                    }
                }
            });
        } else {
            if (outboundChannel != null && outboundChannel.isActive()) {
                outboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) {
                        if (future.isSuccess()) {
                            ctx.channel().read();
                        } else {
                            future.channel().close();
                        }
                    }
                });
            }
        }
    }

    private boolean authenticate(String auth) {
        if (!proxyConfig.isAuthentication()) {
            return true;
        }
        if (auth != null && auth.startsWith("Basic ")) {
            String credentials = auth.substring(6);
            String decoded = new String(java.util.Base64.getDecoder().decode(credentials));
            String[] parts = decoded.split(":");
            if (parts.length == 2) {
                return parts[0].equals(proxyConfig.getUsername()) 
                    && parts[1].equals(proxyConfig.getPassword());
            }
        }
        return false;
    }

    private void sendAuthenticationRequired(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, 
            HttpResponseStatus.PROXY_AUTHENTICATION_REQUIRED
        );
        response.headers().set(
            HttpHeaderNames.PROXY_AUTHENTICATE, 
            "Basic realm=\"Proxy Authentication Required\""
        );
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void handleConnectRequest(ChannelHandlerContext ctx, HttpRequest request) {
        String uri = request.uri();
        String[] hostAndPort = uri.split(":");
        String host = hostAndPort[0];
        int port = Integer.parseInt(hostAndPort[1]);

        Bootstrap b = new Bootstrap();
        b.group(ctx.channel().eventLoop())
            .channel(ctx.channel().getClass())
            .handler(new BackendInitializer(ctx.channel()))
            .option(ChannelOption.AUTO_READ, false);

        b.connect(host, port).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    outboundChannel = future.channel();
                    HttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK
                    );
                    ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) {
                            if (future.isSuccess()) {
                                ctx.pipeline().remove(HttpServerCodec.class);
                                outboundChannel.pipeline().remove(HttpClientCodec.class);
                                ctx.channel().read();
                            } else {
                                future.channel().close();
                            }
                        }
                    });
                } else {
                    ctx.channel().close();
                }
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Frontend proxy error", cause);
        closeOnFlush(ctx.channel());
    }

    static void closeOnFlush(Channel ch) {
        if (ch != null && ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
} 