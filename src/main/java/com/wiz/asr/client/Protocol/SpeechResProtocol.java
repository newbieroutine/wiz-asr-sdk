package com.wiz.asr.client.Protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * describe:
 *
 * @author hyx
 * @date 2020-10-26
 */
public class SpeechResProtocol {

    public Map<String, Object> header = new HashMap();
    public Map<String, Object> payload = new HashMap();

    public SpeechResProtocol() {
    }

    public String getNameSpace() {
        return (String)this.header.get("namespace");
    }

    public String getName() {
        return (String)this.header.get("name");
    }

    public int getStatus() {
        return (Integer)this.header.get("status");
    }

    public String getStatusText() {
        return (String)this.header.get("status_text");
    }

    public String getTaskId() {
        return (String)this.header.get("task_id");
    }

    public String getString(String key) {
        return (String)this.payload.get(key);
    }

    public Integer getInt(String key) {
        return (Integer)this.payload.get(key);
    }

    public Object getObject(String key) {
        return this.payload.get(key);
    }
}
