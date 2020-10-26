package com.wiz.asr.client.protocol;

import com.alibaba.fastjson.JSON;
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
public abstract class SpeechRecognizerListener implements ConnectionListener {

    Logger logger = LoggerFactory.getLogger(SpeechRecognizerListener.class);
    protected SpeechRecognizer recognizer;

    public SpeechRecognizerListener() {
    }

    public void setSpeechRecognizer(SpeechRecognizer recognizer) {
        this.recognizer = recognizer;
    }

    public abstract void onRecognitionResultChanged(SpeechRecognizerResponse var1);

    public abstract void onRecognitionCompleted(SpeechRecognizerResponse var1);

    public abstract void onStarted(SpeechRecognizerResponse var1);

    public abstract void onFail(SpeechRecognizerResponse var1);

    @Override
    public void onOpen() {
        this.logger.debug("connection is ok");
    }

    @Override
    public void onClose(int closeCode, String reason) {
        if (this.recognizer != null) {
            this.recognizer.markClosed();
        }

        this.logger.debug("connection is closed due to {},code:{}", reason, closeCode);
    }

    @Override
    public void onMessage(String message) {
        if (message != null && message.trim().length() != 0) {
            this.logger.debug("on message:{}", message);
            SpeechRecognizerResponse response = (SpeechRecognizerResponse) JSON.parseObject(message, SpeechRecognizerResponse.class);
            if (this.isRecReady(response)) {
                this.onStarted(response);
                this.recognizer.markReady();
            } else if (this.isRecResult(response)) {
                this.onRecognitionResultChanged(response);
            } else if (this.isRecComplete(response)) {
                this.onRecognitionCompleted(response);
                this.recognizer.markComplete();
            } else if (this.isTaskFailed(response)) {
                this.onFail(response);
                this.recognizer.markFail();
            } else {
                this.logger.error(message);
            }

        }
    }

    @Override
    public void onMessage(ByteBuffer message) {
    }

    private boolean isRecReady(SpeechRecognizerResponse response) {
        String name = response.getName();
        return name.equals("RecognitionStarted");
    }

    private boolean isRecResult(SpeechRecognizerResponse response) {
        String name = response.getName();
        return name.equals("RecognitionResultChanged");
    }

    private boolean isRecComplete(SpeechRecognizerResponse response) {
        String name = response.getName();
        return name.equals("RecognitionCompleted");
    }

    private boolean isTaskFailed(SpeechRecognizerResponse response) {
        String name = response.getName();
        return name.equals("TaskFailed");
    }
}
