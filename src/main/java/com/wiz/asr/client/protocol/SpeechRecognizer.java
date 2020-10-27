package com.wiz.asr.client.protocol;

import com.wiz.asr.client.IdGen;
import com.wiz.asr.client.enums.ResponseTypeEnum;
import com.wiz.asr.client.transport.Connection;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * describe:
 *
 * @author hyx
 * @date 2020-10-26
 */
@Slf4j
public class SpeechRecognizer extends SpeechReqProtocol {
    static Logger logger = LoggerFactory.getLogger(SpeechRecognizer.class);
    private CountDownLatch completeLatch;
    private CountDownLatch readyLatch;
    protected long lastSendTime = -1L;


    /**
     * speechCode if need to change
     * such as T_SSSSS$$$$14
     */
    private String templateCode="T_wbAuTAuqIH8B$$$10";

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
        listener.setSpeechRecognizer(this);
        //发送open 指令
        String taskId = IdGen.genId();
        this.currentTaskId = taskId;


        this.state = State.STATE_CONNECTED;
    }

    public void send(byte[] data) {
        // 文件流进行高低位处理
        byte title[] = new byte[2];
        title = chaiFenDataIntTo2Byte(messageIndex);
        data = byteMerger(title,data);
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
        // 发送open 指令
        this.conn.sendText(this.serialize(open()));
        this.start(10000L);
    }

    public void start(long milliSeconds) throws Exception {
        this.state.checkStart();

        // 发送start 指令
        this.conn.sendText(this.serialize(starts()));
        this.state = SpeechReqProtocol.State.STATE_REQUEST_SENT;

        this.completeLatch = new CountDownLatch(1);
        this.readyLatch = new CountDownLatch(1);
        boolean result = this.readyLatch.await(milliSeconds, TimeUnit.MILLISECONDS);
        if (!result) {
            String msg = String.format("timeout after %d ms waiting for start confirmation.task_id:%s,state:%s", milliSeconds, this.currentTaskId, this.state);
            logger.error(msg);
            throw new Exception(msg);
        }
    }

    /**
     *@描述 asr open params Assembly
     *@参数
     *@返回值
     *@创建人  zj
     *@创建时间  2020/10/26
     */
    private Map<String, Object> open(){
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("cmd", ResponseTypeEnum.OPEN.getResponse());
        map.put("callId",currentTaskId);
        map.put("language","0");
        return map;
    }

    /**
     *@描述  asr start params assembly
     *@参数
     *@返回值
     *@创建人  zj
     *@创建时间  2020/10/26
     */
    private Map<String, Object> starts(){
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> header = new HashMap<String ,Object>();
        map.put("cmd",ResponseTypeEnum.START.getResponse());
        map.put("samplerate",8000);
        map.put("samplebits",16);
        map.put("speechIndex",messageIndex);
        header.put("callId",currentTaskId);
        header.put("templateId",templateCode);
        header.put("language","0");
        header.put("asrService","ali");
        header.put("asrEnglishService","ali");
        header.put("nodeIndex",null);
        header.put("asrIndonesiaService","google");

        map.put("header",header);
        return map;
    }


    public void stop() throws Exception {
        this.stop(10000L);
    }

    public void stop(long milliSeconds) throws Exception {
        if (this.state == State.STATE_COMPLETE) {
            logger.info("state is {} stop message is discarded", State.STATE_COMPLETE);
        } else {
            this.state.checkStop();
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("cmd", ResponseTypeEnum.STOP.getResponse());
            map.put("speechIndex",messageIndex);
            SpeechReqProtocol req = new SpeechReqProtocol();
            this.conn.sendText(req.serialize(map));
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


    //单个文件流拼接标志
    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2){
        byte[] byte_3 = new byte[byte_1.length+byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }


    public static byte[] chaiFenDataIntTo2Byte(int data) {
        byte[] byteArray = new byte[2];
        byteArray[0] =  (byte) (data >> 8);
        byteArray[1] = (byte) data;
        pinJie2ByteToInt(byteArray[1],byteArray[0]);
        return byteArray;
    }

    public static int pinJie2ByteToInt(byte byte1, byte byte2) {
        int result = byte1;
        result = (result << 8) | (0x00FF & byte2);
        return result;
    }

}
