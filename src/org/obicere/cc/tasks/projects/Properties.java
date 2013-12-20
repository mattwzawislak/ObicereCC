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

package org.obicere.cc.tasks.projects;

import java.util.Map;

/**
 * The <tt>Properties</tt> class is used to map properties
 * from the Runner's XML file. This was used in place of a
 * retention policy manifest.
 *
 * @author Obicere
 * @since 1.0
 * @version 1.0
 */

public class Properties {

    private static final String[] PROPERTIES = new String[]{"name", "version", "author", "description", "category", "parameter", "method", "return"};
    private static final String SKELETON = "public class name {\n\t\n\tpublic return method(parameter){\n\t\t\n\t}\n\t\n}";

    private final Map<String, String> map;
    private final String skeleton;

    /**
     * Constructs a new Properties object. This uses a map
     * loaded from the XML file. It is recommended to use the
     * <tt>XMLParser</tt> to get this map.
     *
     * @param map The map containing the properties.
     */

    public Properties(final Map<String, String> map) {
        String skeleton = SKELETON;
        for (final String property : PROPERTIES) {
            if (map.get(property) == null) {
                throw new NoSuchFieldError("Missing property: " + property + " not found in XML file");
            }
            skeleton = skeleton.replace(property, map.get(property));
        }
        this.map = map;
        this.skeleton = skeleton;
    }

    /**
     * This will load the pre-built and formatted skeleton, to be
     * used when the user has contributed no code that can be
     * loaded for this runner.
     *
     * @return The skeleton used to create a base code
     */

    public String getSkeleton() {
        return skeleton;
    }

    /**
     * This returns the version of the runner. This is most
     * commonly used, if not only used, during updates.
     *
     * @return The version of the loaded runner.
     */

    public double getVersion() {
        return Double.parseDouble(map.get("version"));
    }

    /**
     * Each Runner has a name, this name distinguishes the
     * class for the runner, the XML properties file and
     * the name of the main class to be loaded.
     *
     * @return Representative name for this runner.
     */

    public String getName() {
        return map.get("name");
    }

    /**
     * This is used to show who the author of the runner is.
     * This can be used for security issues, but mostly is just
     * used for displaying the Runners in the selector.
     *
     * @return The author of the runner.
     */

    public String getAuthor() {
        return map.get("author");
    }

    /**
     * This method is used to get the description. The basis of
     * this is for displaying what the challenge is. We don't
     * want the users to run around blindly in the dark.
     *
     * @return The description to, well, describe the challenge.
     */

    public String getDescription() {
        return map.get("description");
    }

    /**
     * This is used for setting the color and difficulty name
     * of the Runner. This is also used during sorting to
     * help sort by difficulty.
     *
     * @return The difficulty level, bounded by 1 and 5 inclusive.
     */

    public int getCategory() {
        return Math.min(Math.max(Integer.parseInt(map.get("category")), 1), 5);
    }

    /**
     * The name of the method. This is used during the reflection
     * process to run the code the user wishes to enter. It is
     * also used in the initial construction of the skeleton.
     *
     * @return The method name.
     */

    public String getMethod() {
        return map.get("method");
    }

}
