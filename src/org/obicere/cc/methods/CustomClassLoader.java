

package org.obicere.cc.methods;

import java.io.File;



public class CustomClassLoader extends ClassLoader {

    private CustomClassLoader() {
    }


    public static Class<?> loadClassFromFile(final String file) {
        return loadClassFromFile(new File(file));
    }



    public static Class<?> loadClassFromFile(final File file) {
        return new CustomClassLoader().loadClass(file);
    }



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
