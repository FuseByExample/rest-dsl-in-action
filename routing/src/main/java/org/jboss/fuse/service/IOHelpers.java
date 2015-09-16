package org.jboss.fuse.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class IOHelpers {

    public static String loadFully(URL url) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int l;
        InputStream is = url.openStream();
        try {
            while ((l = is.read(buf)) >= 0) {
                baos.write(buf, 0, l);
            }
        } finally {
            is.close();
        }
        return baos.toString();
    }


}
