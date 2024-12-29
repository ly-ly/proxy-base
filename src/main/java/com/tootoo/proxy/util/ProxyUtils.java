package com.tootoo.proxy.util;

import io.netty.handler.codec.http.HttpRequest;
import java.net.URI;
import java.net.URISyntaxException;

public class ProxyUtils {
    
    public static String getTargetHost(HttpRequest request) throws URISyntaxException {
        URI uri = new URI(request.uri());
        if (uri.getHost() == null) {
            String hostHeader = request.headers().get("Host");
            if (hostHeader != null) {
                return hostHeader.split(":")[0];
            }
            throw new IllegalArgumentException("Cannot resolve target host");
        }
        return uri.getHost();
    }

    public static int getTargetPort(HttpRequest request) throws URISyntaxException {
        URI uri = new URI(request.uri());
        if (uri.getPort() != -1) {
            return uri.getPort();
        }
        
        String scheme = uri.getScheme();
        if (scheme == null) {
            scheme = "http";
        }
        
        switch (scheme.toLowerCase()) {
            case "http":
                return 80;
            case "https":
                return 443;
            default:
                throw new IllegalArgumentException("Unknown scheme: " + scheme);
        }
    }
} 