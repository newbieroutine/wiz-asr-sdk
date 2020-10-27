package com.wiz.asr.client.protocol;

import com.alibaba.fastjson.JSON;
import com.wiz.asr.client.enums.HttpStatusEnum;
import com.wiz.asr.client.enums.ResponseTypeEnum;
import com.wiz.asr.client.transport.ConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * describe:
 *
 * @author hyx
 * @date 2020-10-26
 */
public abstract class SpeechRecognizerListener implements ConnectionListener {

    Logger logger = LoggerFactory.getLogger(SpeechRecognizerListener.class);
    protected SpeechRecognizer recognizer;
    List<String> list = new ArrayList<String>();
    String[] errStr = {HttpStatusEnum.ILLEGAL.getCode(),HttpStatusEnum.ILLEGAL_PARAM.getCode(),HttpStatusEnum.ILLEGAL_PARAM.getCode(),HttpStatusEnum.WIZ_INIT_ERROR.getCode(),
            HttpStatusEnum.WIZ_RESULT_ERROR.getCode(),HttpStatusEnum.WIZ_FILE_ERROR.getCode()};

    public SpeechRecognizerListener() {
        list = Arrays.asList(errStr);
    }

    public void setSpeechRecognizer(SpeechRecognizer recognizer) {
        this.recognizer = recognizer;
    }


    public abstract void onOpen(SpeechRecognizerResponse var1);

    public abstract void onTranscriptionComplete(SpeechRecognizerResponse var1);

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
           if (this.isOpen(response)){
               this.onOpen(response);
           }else if (this.isRecReady(response)) {
                this.onStarted(response);
                this.recognizer.markReady();
            } else if (this.isRecComplete(response)) {
                this.onTranscriptionComplete(response);
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

    private boolean isOpen(SpeechRecognizerResponse response){
        String name = response.getResponse();
        return name.equals(ResponseTypeEnum.OPEN.getResponse());
    }

    private boolean isRecReady(SpeechRecognizerResponse response) {
        String name = response.getResponse();
        return name.equals(ResponseTypeEnum.START.getResponse());
    }

    private boolean isRecComplete(SpeechRecognizerResponse response) {
        String name = response.getResponse();
        return name.equals(ResponseTypeEnum.STOP.getResponse());
    }

    private boolean isTaskFailed(SpeechRecognizerResponse response) {

        String name = response.getCode();
        return list.contains(name);
    }
}
