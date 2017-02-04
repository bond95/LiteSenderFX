package com.bond95.litesender;

import java.nio.ByteBuffer;

/**
 * Created by bond95 on 7/9/16.
 */
public interface StructObject {
    ByteBuffer convertObjectToBuffer();
    void convertBufferToObject(ByteBuffer buf, int size);
}
