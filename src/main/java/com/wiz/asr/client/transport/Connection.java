package com.wiz.asr.client.transport;

/**
 * describe:
 *
 * @author hyx
 * @date 2020-10-26
 */
public interface Connection {

    void close();

    void sendText(String var1);

    void sendBinary(byte[] var1);

    void sendPing();

    String getId();

    boolean isActive();

    long getConnectingLatency();

    long getWsHandshakeLatency();
}
