/*
This file is part of ObicereCC.

ObicereCC is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

ObicereCC is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with ObicereCC.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.obicere.cc.methods;

import java.io.*;
import java.net.URL;

/**
 * File downloader utility
 *
 * @author Obicere
 * @since 1.0
 */

public class IOUtils {

    private IOUtils() {
    }

    /**
     * Will download file data ready to be exported to a file
     *
     * @param url The valid URL you wish to read data from
     * @return byte array of data. Building a file from this is safe
     * @throws IOException
     * @since 1.0
     */
    public static byte[] download(final URL url) throws IOException {
        return readData(url.openStream());
    }

    /**
     * This will read a local file and return data which can be manipulated easily
     *
     * @param file Will load data from a local file
     * @return byte array of data. Building a file or modification of a file from this is safe
     * @throws IOException
     * @since 1.0
     */

    public static byte[] readData(final File file) throws IOException {
        return readData(new FileInputStream(file));
    }

    /**
     * Reads data from an input stream, either a File or URL
     *
     * @param in input stream to read data from. Allocates 1024 bytes per cycle
     * @return a mutable byte array for file storing or manipulation
     * @throws IOException
     *
     * @since 1.0
     */

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

    /**
     * Writes a file and byte data to a specific destination
     *
     * @param file The file to be written to, does not necessarily have to exist.
     * @param data The data to be written. It is best used with url-based files
     * @throws IOException
     * @see {IOUtils.download(URL);}
     * @since 1.0
     */

    public static void write(final File file, final byte[] data) throws IOException {
        final FileOutputStream out = new FileOutputStream(file);
        out.write(data);
        out.close();
    }

}
