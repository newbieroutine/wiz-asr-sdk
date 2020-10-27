package com.wiz.asr.client.protocol;

/**
 * describe:
 *
 * @author hyx
 * @date 2020-10-26
 */
public class SpeechRecognizerResponse extends SpeechResProtocol {

    public SpeechRecognizerResponse() {
    }

    public String getRecognizedText() {
        return (String)this.getResult();
    }


}
