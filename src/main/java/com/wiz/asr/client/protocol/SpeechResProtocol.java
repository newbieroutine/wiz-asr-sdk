package com.wiz.asr.client.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * describe:
 *
 * @author hyx
 * @date 2020-10-26
 */
public class SpeechResProtocol {

    //电话id
    private String callId;

    //每句话标志
    private Integer speechIndex;

    //最终结果
    private String result;

    private String code;

    private String msg;

    private String response ;



    public SpeechResProtocol() {
    }


    public String getName() {
        return this.getResponse();
    }

    public int getStatus() {
        return  Integer.valueOf(this.getCode());
    }

    public String getStatusText() {
        return (String)this.getResult();
    }

    public String getTaskId() {
        return (String)this.getCallId();
    }



    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public Integer getSpeechIndex() {
        return speechIndex;
    }

    public void setSpeechIndex(Integer speechIndex) {
        this.speechIndex = speechIndex;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
