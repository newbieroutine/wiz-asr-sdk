package com.wiz.asr.client.Protocol;

import com.wiz.asr.client.transport.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * describe:
 *
 * @author hyx
 * @date 2020-10-26
 */
public class SpeechRecognizer extends SpeechReqProtocol {
    static Logger logger = LoggerFactory.getLogger(SpeechRecognizer.class);
    private CountDownLatch completeLatch;
    private CountDownLatch readyLatch;
    protected long lastSendTime = -1L;
    private static final String DEFAULT_FORMAT = "pcm";
    private static final Integer DEFAULT_SAMPLE_RATE = 16000;

    /**
     * speechCode if need to change
     * such as T_SSSSS$$$$14
     */
    private String templateCode;

    /**
     * the index of message
     */
    private int messageIndex = 0;



    public SpeechRecognizer(SpeechClient client, SpeechRecognizerListener listener) throws Exception {
        Connection conn = client.connect(listener);
        this.conn = conn;
        this.afterConnection(listener);
    }

    public SpeechRecognizer(SpeechClient client, SpeechRecognizerListener listener, String templateCode) throws Exception {
        this.templateCode = templateCode;
        Connection conn = client.connect(listener);
        this.conn = conn;
        this.afterConnection(listener);
    }


    protected void afterConnection(SpeechRecognizerListener listener) {
        this.payload = new HashMap();
        this.header.put("namespace", "SpeechRecognizer");
        this.header.put("name", "StartRecognition");
        this.payload.put("format", "pcm");
        this.payload.put("sample_rate", DEFAULT_SAMPLE_RATE);
        listener.setSpeechRecognizer(this);
        this.state = State.STATE_CONNECTED;
    }

    public void send(byte[] data) {
        this.send(data, data.length);
    }

    public void send(byte[] data, int length) {
        if (this.state == State.STATE_COMPLETE) {
            logger.info("state is {} stop send", State.STATE_COMPLETE);
        } else {
            long sendInterval;
            if (this.lastSendTime != -1L && (sendInterval = System.currentTimeMillis() - this.lastSendTime) > 5000L) {
                logger.warn("too large binary send interval: {} million second", sendInterval);
            }

            this.state.checkSend();

            try {
                this.conn.sendBinary(Arrays.copyOfRange(data, 0, length));
                this.lastSendTime = System.currentTimeMillis();
            } catch (Exception var6) {
                logger.error("fail to send binary,current_task_id:{},state:{}", new Object[]{this.currentTaskId, this.state, var6});
                throw new RuntimeException(var6);
            }
        }
    }

    public void send(InputStream ins) {
        this.state.checkSend();

        try {
            byte[] bytes = new byte[8000];

            int len;
            for(boolean var3 = false; (len = ins.read(bytes)) > 0; this.lastSendTime = System.currentTimeMillis()) {
                if (this.state == State.STATE_COMPLETE) {
                    logger.info("state is {} stop send", State.STATE_COMPLETE);
                    return;
                }

                long sendInterval;
                if (this.lastSendTime != -1L && (sendInterval = System.currentTimeMillis() - this.lastSendTime) > 5000L) {
                    logger.warn("too large binary send interval: {} million seconds", sendInterval);
                }

                this.conn.sendBinary(Arrays.copyOfRange(bytes, 0, len));
            }

        } catch (Exception var6) {
            logger.error("fail to send binary,current_task_id:{},state:{}", new Object[]{this.currentTaskId, this.state, var6});
            throw new RuntimeException(var6);
        }
    }

    public void send(InputStream ins, int batchSize, int sleepInterval) {
        this.state.checkSend();

        try {
            byte[] bytes = new byte[batchSize];
            boolean var5 = false;

            int len;
            while((len = ins.read(bytes)) > 0) {
                if (this.state == State.STATE_COMPLETE) {
                    logger.info("state is {} stop send", State.STATE_COMPLETE);
                    return;
                }

                long sendInterval;
                if (this.lastSendTime != -1L && (sendInterval = System.currentTimeMillis() - this.lastSendTime) > 5000L) {
                    logger.warn("too large binary send interval: {} million second", sendInterval);
                }

                this.conn.sendBinary(Arrays.copyOfRange(bytes, 0, len));
                this.lastSendTime = System.currentTimeMillis();
                Thread.sleep((long)sleepInterval);
            }

        } catch (Exception var8) {
            logger.error("fail to send binary,current_task_id:{},state:{}", new Object[]{this.currentTaskId, this.state, var8});
            throw new RuntimeException(var8);
        }
    }

    void markReady() {
        this.state = State.STATE_REQUEST_CONFIRMED;
        if (this.readyLatch != null) {
            this.readyLatch.countDown();
        }

    }

    void markComplete() {
        this.state = State.STATE_COMPLETE;
        if (this.completeLatch != null) {
            this.completeLatch.countDown();
        }

    }

    void markFail() {
        this.state = State.STATE_FAIL;
        if (this.readyLatch != null) {
            this.readyLatch.countDown();
        }

        if (this.completeLatch != null) {
            this.completeLatch.countDown();
        }

    }

    void markClosed() {
        this.state = State.STATE_CLOSED;
        if (this.readyLatch != null) {
            this.readyLatch.countDown();
        }

        if (this.completeLatch != null) {
            this.completeLatch.countDown();
        }

    }

    @Override
    public void start() throws Exception {
        this.start(10000L);
    }

    public void start(long milliSeconds) throws Exception {
        super.start();
        this.completeLatch = new CountDownLatch(1);
        this.readyLatch = new CountDownLatch(1);
        boolean result = this.readyLatch.await(milliSeconds, TimeUnit.MILLISECONDS);
        if (!result) {
            String msg = String.format("timeout after %d ms waiting for start confirmation.task_id:%s,state:%s", milliSeconds, this.currentTaskId, this.state);
            logger.error(msg);
            throw new Exception(msg);
        }
    }

    public void stop() throws Exception {
        this.stop(10000L);
    }

    public void stop(long milliSeconds) throws Exception {
        if (this.state == State.STATE_COMPLETE) {
            logger.info("state is {} stop message is discarded", State.STATE_COMPLETE);
        } else {
            this.state.checkStop();
            SpeechReqProtocol req = new SpeechReqProtocol();
            req.header.put("namespace", "SpeechRecognizer");
            req.header.put("name", "StopRecognition");
            req.header.put("task_id", this.currentTaskId);
            this.conn.sendText(req.serialize());
            this.state = State.STATE_STOP_SENT;
            boolean result = this.completeLatch.await(milliSeconds, TimeUnit.MILLISECONDS);
            if (!result) {
                String msg = String.format("timeout after %d ms waiting for complete confirmation.task_id:%s,state:%s", milliSeconds, this.currentTaskId, this.state);
                logger.error(msg);
                throw new Exception(msg);
            }
        }
    }

    public void close() {
        this.conn.close();
    }

}
