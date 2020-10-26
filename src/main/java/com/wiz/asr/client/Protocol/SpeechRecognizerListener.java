package com.wiz.asr.client.Protocol;

import com.wiz.asr.client.transport.ConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * describe:
 *
 * @author hyx
 * @date 2020-10-26
 */
public class SpeechRecognizerListener implements ConnectionListener {

    Logger logger = LoggerFactory.getLogger(SpeechRecognizerListener.class);
    protected SpeechRecognizer recognizer;

    public SpeechRecognizerListener() {
    }

    public void setSpeechRecognizer(SpeechRecognizer recognizer) {
        this.recognizer = recognizer;
    }

    public void onOpen() {

    }

    public void onClose(int var1, String var2) {

    }

    public void onMessage(String var1) {

    }

    public void onMessage(ByteBuffer var1) {

    }
}
