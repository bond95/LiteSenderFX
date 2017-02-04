package com.bond95.litesender;

import java.nio.ByteBuffer;

/**
 * Created by bond95 on 7/10/16.
 */

/**
 * Response object class
 */
public class ResponseObject implements StructObject {
    private byte[] hash;
    private byte flag;

    public void setHash(byte[] hash) {
        this.hash = new byte[32];
        int length = hash.length > 32 ? 32 : hash.length;
        for (int i = 0; i < length; i++) {
            this.hash[i] = hash[i];
        }
    }

    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public byte[] getHash() {
        return hash;
    }

    public byte getFlag() {
        return flag;
    }

    public ByteBuffer convertObjectToBuffer() {
        int len = 1 + 32;
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.put(this.hash);
        buf.put(this.flag);
        buf.flip();
        return buf;
    }

    public void convertBufferToObject(ByteBuffer buffer, int size) {
        buffer.flip();
        this.hash = new byte[32];
        buffer.get(this.hash, 0, 32);
        this.flag = buffer.get();
    }
}
