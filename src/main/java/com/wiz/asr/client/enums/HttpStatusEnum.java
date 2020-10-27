package com.wiz.asr.client.enums;

/**
 * author:zj
 * Date:2019/11/26
 * Time:17:13
 * http 请求返回状态枚举类
 */
public enum HttpStatusEnum {
    OK("200","ok"),
    OPEN("200","recognitionOpened"),
    START("200","recognitionStarted"),
    STOP("200","recognitionStopped"),
    CLOSE("200","recognitionClosed"),
    TRANSCRIPTION_RESULT_CHANGED("200","TranscriptionResultChanged"),
    REPEAT("100","Repeat instructions"),
    ILLEGAL("101","Illegal instruction"),
    ILLEGAL_PARAM("102","Illegal parameter"),
    WIZ_INIT_ERROR("103","asr real-time speech recognition client initialization exception"),
    WIZ_RESULT_ERROR("104","asr real-time voice result processing is abnormal"),
    WIZ_FILE_ERROR("105","asr stream processing exception"),
    HEARTBEAT("200","heartbeat"),
    ILLEGAL_PARAM_SHORT("102","The command lacks English engine parameter information");

    private HttpStatusEnum(String code, String codeMsg){
        this.code= code;
        this.codeMsg = codeMsg;
    }

    static   String getCodeMsg(String code){
        for (HttpStatusEnum value : HttpStatusEnum.values()) {
            if(code.equals(value)){
                return value.getCodeMsg();
            }
        }
        return null;
    }

    private String code;

    private String codeMsg;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeMsg() {
        return codeMsg;
    }

    public void setCodeMsg(String codeMsg) {
        this.codeMsg = codeMsg;
    }
}
