package com.bond95.litesender;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by bond95 on 7/9/16.
 */
public class ObjectOutputStream extends DataOutputStream {

    public ObjectOutputStream(OutputStream out) {
        super(out);
    }

    public void writeObject(StructObject obj) throws IOException {
        ByteBuffer buf = obj.convertObjectToBuffer();
        System.out.println("//-----------------");
        System.out.println(buf.array().length);
        writeInt(buf.array().length);
        out.write(buf.array());
    }
}
