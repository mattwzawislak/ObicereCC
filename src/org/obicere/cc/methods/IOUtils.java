

package org.obicere.cc.methods;

import java.io.*;
import java.net.URL;



public class IOUtils {

    private IOUtils() {
    }


    public static byte[] download(final URL url) throws IOException {
        return readData(url.openStream());
    }



    public static byte[] readData(final File file) throws IOException {
        return readData(new FileInputStream(file));
    }



    private static byte[] readData(final InputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] data = new byte[1024];
        int read;
        while ((read = in.read(data, 0, 1024)) != -1) {
            out.write(data, 0, read);
        }
        in.close();
        return out.toByteArray();
    }



    public static void write(final File file, final byte[] data) throws IOException {
        final FileOutputStream out = new FileOutputStream(file);
        out.write(data);
        out.close();
    }

}
