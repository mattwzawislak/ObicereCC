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

import java.io.File;

/**
 * Loads classes from predefined files. Doesn't do any file checks so ensure it
 * exists before loading it's class.
 *
 * @author Obicere
 * @see {@link ClassLoader}
 * @since 1.0
 */

public class CustomClassLoader extends ClassLoader {

    private CustomClassLoader() {
    }

    /**
     * Loads a class from a predefined file. Creates a new file instance, so if
     * you already have a file, use the other one.
     *
     * @param file <tt>String instance for a file to load. File must exist.</tt>
     * @return A class instance from a defined file.
     * @see {@link CustomClassLoader#loadClassFromFile(File)}
     */
    public static Class<?> loadClassFromFile(final String file) {
        return loadClassFromFile(new File(file));
    }

    /**
     * Loads a class from a file instance. Reads the bytes and creates a class
     * instance.
     *
     * @param file <tt>File</tt> instance to load the class from. This file must
     *             exist.
     * @return A class instance from a defined file.
     */

    public static Class<?> loadClassFromFile(final File file) {
        return new CustomClassLoader().loadClass(file);
    }

    /**
     * Loads a class and defines it using methods inherited from
     * {@link ClassLoader}.
     *
     * @param file File to load a class from.
     * @return A class instance from a defined file.
     */

    private Class<?> loadClass(final File file) {
        try {
            final byte[] data = IOUtils.readData(file);
            return super.defineClass(file.getName().substring(0, file.getName().length() - 6), data, 0, data.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
