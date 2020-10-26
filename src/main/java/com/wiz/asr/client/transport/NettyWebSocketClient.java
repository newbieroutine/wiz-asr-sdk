package com.wiz.asr.client.transport;

import java.net.URI;



import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * describe:
 *
 * @author hyx
 * @date 2020-10-26
 */
public class NettyWebSocketClient {

    private static Logger logger = LoggerFactory.getLogger(NettyWebSocketClient.class);
    private URI websocketURI;
    private int port;
    private SslContext sslCtx;
    EventLoopGroup group = new NioEventLoopGroup(0);
    Bootstrap bootstrap = new Bootstrap();

    public NettyWebSocketClient(String uriStr) throws Exception {
        this.websocketURI = new URI(uriStr);
        boolean ssl = "wss".equalsIgnoreCase(this.websocketURI.getScheme());
        this.port = this.websocketURI.getPort();
        if (ssl) {
            this.sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            if (this.port == -1) {
                this.port = 443;
            }
        } else if (this.port == -1) {
            this.port = 80;
        }

        final String isCompression = System.getProperty("nls.ws.compression", "false");
        ((Bootstrap)((Bootstrap)((Bootstrap)this.bootstrap.option(ChannelOption.TCP_NODELAY, true)).group(this.group)).channel(NioSocketChannel.class)).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline p = ch.pipeline();
                if (NettyWebSocketClient.this.sslCtx != null) {
                    p.addLast(new ChannelHandler[]{NettyWebSocketClient.this.sslCtx.newHandler(ch.alloc(), NettyWebSocketClient.this.websocketURI.getHost(), 443)});
                }

                if ("true".equalsIgnoreCase(isCompression)) {
                    p.addLast(new ChannelHandler[]{new HttpClientCodec(), new HttpObjectAggregator(8192), WebSocketClientCompressionHandler.INSTANCE});
                } else {
                    p.addLast(new ChannelHandler[]{new HttpClientCodec(), new HttpObjectAggregator(8192)});
                }

                p.addLast("hookedHandler", new WebSocketClientHandler());
            }
        });
    }

    public Connection connect(String token, ConnectionListener listener, int connectionTimeout) throws Exception {
        this.bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout);
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        httpHeaders.set("X-NLS-Token", token);
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(this.websocketURI, WebSocketVersion.V13, (String)null, true, httpHeaders, 196608);
        long start = System.currentTimeMillis();
        Channel channel = this.bootstrap.connect(this.websocketURI.getHost(), this.port).sync().channel();
        long connectingTime = System.currentTimeMillis() - start;
        logger.debug("websocket channel is established after sync,connectionId:{} ,use {}", channel.id(), connectingTime);
        WebSocketClientHandler handler = (WebSocketClientHandler)channel.pipeline().get("hookedHandler");
        handler.setListener(listener);
        handler.setHandshaker(handshaker);
        handshaker.handshake(channel);
        start = System.currentTimeMillis();
        handler.handshakeFuture().sync();
        long handshakeTime = System.currentTimeMillis() - start;
        logger.debug("websocket connection is established after handshake,connectionId:{},use {}", channel.id(), handshakeTime);
        return new NettyConnection(channel, connectingTime, handshakeTime);
    }

    public void shutdown() {
        this.group.shutdownGracefully();
    }
}
