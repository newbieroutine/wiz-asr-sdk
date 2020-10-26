package com.wiz.asr.client;

import java.util.UUID;

/**
 * describe:
 *
 * @author hyx
 * @date 2020-10-26
 */
public class IdGen {
    public IdGen() {
    }

    public static String genId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
