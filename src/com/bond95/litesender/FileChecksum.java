package com.bond95.litesender;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * Created by bond95 on 7/9/16.
 */

/**
 * Create MD5 checksum of file
 */
public class FileChecksum {

    public static byte[] createChecksum(File file) throws Exception {
        InputStream fis = new FileInputStream(file);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

}
