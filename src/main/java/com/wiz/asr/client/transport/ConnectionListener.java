package com.wiz.asr.client.transport;

import java.nio.ByteBuffer;

/**
 * describe:
 *
 * @author hyx
 * @date 2020-10-26
 */
public interface ConnectionListener {

    void onOpen();

    void onClose(int var1, String var2);

    void onMessage(String var1);

    void onMessage(ByteBuffer var1);
}
