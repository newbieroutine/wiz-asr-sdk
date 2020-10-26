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
        return (String)this.payload.get("result");
    }

    public String getLexicalText() {
        return (String)this.payload.get("lexical_result");
    }

    public Double getConfidence() {
        Object o = this.payload.get("confidence");
        return o != null ? Double.parseDouble(o.toString()) : null;
    }
}
