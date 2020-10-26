package com.wiz.asr.client.Protocol;

import com.wiz.asr.client.transport.Connection;
import com.wiz.asr.client.transport.ConnectionListener;
import com.wiz.asr.client.transport.NettyWebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * describe:
 *
 * @author hyx
 * @date 2020-10-26
 */
public class SpeechClient {

    private String token;
    public int DEFAULT_CONNECTION_TIMEOUT = 5000;
    private NettyWebSocketClient client;
    private static Logger logger = LoggerFactory.getLogger(SpeechClient.class);

    public SpeechClient(String uri) {
        try {
            this.token = token;
            this.client = new NettyWebSocketClient(uri);
        } catch (Exception var3) {
            logger.error("fail to create NlsClient", var3);
            throw new RuntimeException(var3);
        }

    }

    public Connection connect(ConnectionListener listener) throws Exception {
        int i = 0;

        while(i < 3) {
            try {
                return this.client.connect(this.token, listener, this.DEFAULT_CONNECTION_TIMEOUT);
            } catch (Exception var4) {
                if (i == 2) {
                    logger.error("failed to connect to server after 3 tries,error msg is :{}", var4.getMessage());
                    throw var4;
                }

                Thread.sleep(100L);
                logger.warn("failed to connect to server the {} time:{} ,try again ", i, var4.getMessage());
                ++i;
            }
        }

        return null;
    }


}
