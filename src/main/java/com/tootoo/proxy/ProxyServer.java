package com.tootoo.proxy;

import com.tootoo.config.ProxyConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class ProxyServer {
    private static final Logger logger = LoggerFactory.getLogger(ProxyServer.class);
    private final ProxyConfig proxyConfig;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    @Autowired
    public ProxyServer(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    @PostConstruct
    public void start() throws Exception {
        logger.info("Starting proxy server on port {}", proxyConfig.getPort());
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                            new HttpProxyInitializer(proxyConfig)
                        );
                    }
                });

        ChannelFuture f = bootstrap.bind(proxyConfig.getPort()).sync();
        logger.info("Proxy server started successfully");
    }

    @PreDestroy
    public void stop() {
        logger.info("Stopping proxy server");
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        logger.info("Proxy server stopped");
    }
} 