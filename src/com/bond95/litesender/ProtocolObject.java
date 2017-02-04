package com.bond95.litesender;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by bond95 on 7/9/16.
 */

/**
 * Object sending file
 */
public class ProtocolObject implements StructObject {
    private long size;
    private byte[] hash;
    private String name;

    public void setSize(long size) {
        this.size = size;
    }

    public void setHash(byte[] hash) {
        this.hash = new byte[32];
        int length = hash.length > 32 ? 32 : hash.length;
        for (int i = 0; i < length; i++) {
            this.hash[i] = hash[i];
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public byte[] getHash() {
        return hash;
    }

    public String getName() {
        return name;
    }

    public ByteBuffer convertObjectToBuffer() {
        int len = 8 + 32 + this.name.length();
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.putLong(this.size);
        buf.put(this.hash);
        System.out.println(this.name);
        buf.put(this.name.getBytes());
        buf.flip();
        return buf;
    }

    public void convertBufferToObject(ByteBuffer buffer, int size) {
        System.out.println(size - 8 - 32);
        byte[] temp_buf = new byte[size - 8 - 32];
        buffer.flip();
        this.size = buffer.getLong();
        this.hash = new byte[32];
        buffer.get(this.hash, 0, 32);
        buffer.get(temp_buf, 0, size - 8 - 32);
        this.name = new String(temp_buf);
        System.out.println(this.name.length());
    }
}
