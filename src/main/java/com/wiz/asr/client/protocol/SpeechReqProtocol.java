package com.wiz.asr.client.protocol;

import com.alibaba.fastjson.JSON;
import com.wiz.asr.client.IdGen;
import com.wiz.asr.client.transport.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * describe:
 *
 * @author hyx
 * @date 2020-10-26
 */
public class SpeechReqProtocol {

    private static Logger logger = LoggerFactory.getLogger(SpeechReqProtocol.class);
    protected String accessToken;
    protected Connection conn;
    protected String currentTaskId;
    protected SpeechReqProtocol.State state;
    public Map<String, String> header;
    public Map<String, Object> payload;
    public Map<String, Object> context;


    public String serialize() {
        Map<String, Object> result = new HashMap();
        result.put("header", this.header);
        if (this.payload != null) {
            result.put("payload", this.payload);
            result.put("context", this.context);
        }

        return JSON.toJSONString(result);
    }

    public String serializeWithContext() {
        Map<String, Object> result = new HashMap();
        result.put("header", this.header);
        if (this.context != null) {
            result.put("context", this.context);
        }

        if (this.payload != null) {
            result.put("payload", this.payload);
        }

        return JSON.toJSONString(result);
    }

    public void start() throws Exception {
        this.state.checkStart();
        Map<String, Long> network = new HashMap();
        network.put("connect_cost", this.conn.getConnectingLatency());
        network.put("upgrade_cost", this.conn.getWsHandshakeLatency());
        this.putContext("network", network);
        String taskId = IdGen.genId();
        this.currentTaskId = taskId;
        this.setTaskId(this.currentTaskId);
        this.conn.sendText(this.serialize());
        this.state = SpeechReqProtocol.State.STATE_REQUEST_SENT;
    }

    public void putContext(String key, Object obj) {
        this.context.put(key, obj);
    }

    protected void setTaskId(String requestId) {
        this.header.put("task_id", requestId);
    }

    public static enum State {
        STATE_FAIL(-1) {
            @Override
            public void checkStart() {
                throw new RuntimeException("can't start,current state is " + this);
            }

            @Override
            public void checkSend() {
                throw new RuntimeException("can't send,current state is " + this);
            }

            @Override
            public void checkStop() {
                throw new RuntimeException("can't stop,current state is " + this);
            }
        },
        STATE_INIT(0) {
            @Override
            public void checkStart() {
                throw new RuntimeException("can't start,current state is " + this);
            }

            @Override
            public void checkSend() {
                throw new RuntimeException("can't send,current state is " + this);
            }

            @Override
            public void checkStop() {
                throw new RuntimeException("can't stop,current state is " + this);
            }
        },
        STATE_CONNECTED(10) {
            @Override
            public void checkSend() {
                throw new RuntimeException("can't send,current state is " + this);
            }

            @Override
            public void checkStart() {
            }

            @Override
            public void checkStop() {
                throw new RuntimeException("can't stop,current state is " + this);
            }
        },
        STATE_REQUEST_SENT(20) {
            @Override
            public void checkSend() {
                throw new RuntimeException("can't send,current state is " + this);
            }

            @Override
            public void checkStart() {
                throw new RuntimeException("can't start,current state is " + this);
            }

            @Override
            public void checkStop() {
                throw new RuntimeException("can't stop,current state is " + this);
            }
        },
        STATE_REQUEST_CONFIRMED(30) {
            @Override
            public void checkSend() {
            }

            @Override
            public void checkStart() {
            }

            @Override
            public void checkStop() {
            }
        },
        STATE_STOP_SENT(40) {
            @Override
            public void checkSend() {
                throw new RuntimeException("only STATE_REQUEST_CONFIRMED can send,current state is " + this);
            }

            @Override
            public void checkStart() {
                throw new RuntimeException("can't start,current state is " + this);
            }

            @Override
            public void checkStop() {
                throw new RuntimeException("can't stop,current state is " + this);
            }
        },
        STATE_COMPLETE(50) {
            @Override
            public void checkSend() {
                SpeechReqProtocol.logger.warn("task is completed before sending binary");
            }

            @Override
            public void checkStart() {
            }

            @Override
            public void checkStop() {
                SpeechReqProtocol.logger.warn("task is completed before sending stop command");
            }
        },
        STATE_CLOSED(60) {
            @Override
            public void checkSend() {
                throw new RuntimeException("can't send,current state is " + this);
            }

            @Override
            public void checkStart() {
                throw new RuntimeException("can't start,current state is " + this);
            }

            @Override
            public void checkStop() {
                throw new RuntimeException("can't stop,current state is " + this);
            }
        };

        int value;

        public abstract void checkSend();

        public abstract void checkStart();

        public abstract void checkStop();

        private State(int value) {
            this.value = value;
        }
    }
}
