package com.wiz.asr.client.enums;

/**
 * @author:zj Date:2020/7/31
 * Time:16:13
 * asr 实时响应状态
 */
public enum ResponseTypeEnum {
    OPEN("open"),
    START("start"),
    STOP("stop"),
    CLOSE("close"),
    HEARTBEAT("heartbeat"),
    SEND("send");
    private String response;

    ResponseTypeEnum(String response){
        this.response =response;
    }

    public String getResponse() {
        return response;
    }


}
