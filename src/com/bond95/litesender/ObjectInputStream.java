package com.bond95.litesender;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by bond95 on 7/10/16.
 */
public class ObjectInputStream extends DataInputStream {

    /**
     * Creates a DataInputStream that uses the specified
     * underlying InputStream.
     *
     * @param in the specified input stream
     */
    public ObjectInputStream(InputStream in) {
        super(in);
    }

    public void readObject(StructObject obj) throws IOException {
        int size = readInt();
        System.out.println(size);
        ByteBuffer buf = ByteBuffer.allocate(size);

        int i = 0;
        while (i < size) {
            int b = in.read();
            if (b == -1) {
                break;
            }
            buf.put((byte) b);
            i++;
        }
//        in.read(buf, 0, size);
        obj.convertBufferToObject(buf, size);
    }
}
