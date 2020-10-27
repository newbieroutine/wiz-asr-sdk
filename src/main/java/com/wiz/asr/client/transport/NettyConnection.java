package com.wiz.asr.client.transport;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NettyConnection implements Connection{
    static Logger logger = LoggerFactory.getLogger(NettyConnection.class);
    Channel channel;
    long connectingLatency;
    long handshakeLatency;

    public NettyConnection(Channel channel) {
        this.channel = channel;
    }

    public NettyConnection(Channel channel, long connectingLatency, long handshakeLatency) {
        this.channel = channel;
        this.connectingLatency = connectingLatency;
        this.handshakeLatency = handshakeLatency;
    }

    @Override
    public String getId() {
        return this.channel != null ? this.channel.id().toString() : null;
    }

    @Override
    public boolean isActive() {
        return this.channel != null && this.channel.isActive();
    }

    @Override
    public long getConnectingLatency() {
        return this.connectingLatency;
    }

    @Override
    public long getWsHandshakeLatency() {
        return this.handshakeLatency;
    }

    @Override
    public void close() {
        this.channel.close();
    }

    @Override
    public void sendText(String payload) {
        logger.info("asr sdk send text:{}",payload);
        if (this.channel != null && this.channel.isActive()) {
            logger.debug("thread:{},send:{}", Thread.currentThread().getId(), payload);
            TextWebSocketFrame frame = new TextWebSocketFrame(payload);
            this.channel.writeAndFlush(frame);
        }

    }

    @Override
    public void sendBinary(byte[] payload) {
        if (this.channel != null && this.channel.isActive()) {
            BinaryWebSocketFrame frame = new BinaryWebSocketFrame(Unpooled.wrappedBuffer(payload));
            this.channel.writeAndFlush(frame);
        }

    }

    @Override
    public void sendPing() {
        PingWebSocketFrame frame = new PingWebSocketFrame();
        if (this.channel != null && this.channel.isActive()) {
            this.channel.writeAndFlush(frame);
        }

    }
}
