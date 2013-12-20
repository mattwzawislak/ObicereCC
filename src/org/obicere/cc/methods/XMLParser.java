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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses XML files using only the value tag, for now.
 * Is is required that all
 *
 * @author Obicere
 * @version 1.0
 * @since 1.0
 */

public class XMLParser {

    private static XMLParser instance;
    private final DocumentBuilder builder;
    private Document document;

    /**
     * This is used to get the instance of the <tt>XMLParser</tt>.
     * If there is no instance, it will create a new one properly.
     * This will prevent excessive DOM Objects from being created.
     *
     * @return The instance of the <tt>XMLParser</tt>
     * @since 1.0
     */

    public static XMLParser getInstance() {
        if (instance == null) {
            try {
                instance = new XMLParser();
            } catch (final ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    /**
     * Creates a new instance of the <tt>XMLParser</tt>.
     * Ideally, this will only be done once.
     *
     * @throws ParserConfigurationException
     * @since 1.0
     */

    private XMLParser() throws ParserConfigurationException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        builder = factory.newDocumentBuilder();
    }

    /**
     * Prepares a file for reading. Checks to see if a file exists before
     * If the file doesn't exist, a <tt>FileNotFoundException</tt> will be thrown.
     *
     * @param file The file from which you want to map attributes from.
     * @throws IOException  Typically from missing files.
     * @throws SAXException Typically from invalid XML files that fail to normalize
     * @since 1.0
     */

    public void prepare(final File file) throws IOException, SAXException {
        if (!file.exists()) {
            throw new FileNotFoundException("File: [" + file + "] could not be found.");
        }
        document = builder.parse(file);
        document.getDocumentElement().normalize();
    }

    /**
     * This will return a <tt>Map</tt> of properties from the XML file.
     * A document must be prepared before it can be mapped.
     *
     * @return A <tt>Map</tt> containing attributes loaded from the prepared file.
     * @throws DocumentNotPreparedException
     * @since 1.0
     */

    public Map<String, String> getAttributeMapping() throws DocumentNotPreparedException {
        if (document == null) {
            throw new DocumentNotPreparedException("document is null. A file must be prepared before mapping.");
        }
        final Map<String, String> map = new HashMap<>();
        addAll(map, document.getChildNodes());
        document = null; // Document parsed, dispose of
        return map;
    }

    /**
     * Adds attributes recursively from the XML file.
     *
     * @param map  The map of the loaded attributes.
     *             This gets added to as a pass-by reference.
     * @param list The <tt>NodeList</tt> from the parent list.
     * @since 1.0
     */

    private void addAll(final Map<String, String> map, final NodeList list) {
        for (int i = 0; i < list.getLength(); i++) {
            final Node n = list.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                final Element e = (Element) n;
                addAll(map, e.getChildNodes());
                final String attribute = e.getAttribute("value");
                if (!attribute.isEmpty()) {
                    if (map.containsKey(e.getTagName())) {
                        map.put(e.getTagName(), map.get(e.getTagName()) + ", " + attribute);
                    } else {
                        map.put(e.getTagName(), attribute);
                    }
                }
            }
        }
    }

    /**
     * Used to specify that a document has yet to been prepared.
     * This is used only during the first mapping of the document.
     *
     * @author 1.0
     * @since 1.0
     */

    public static class DocumentNotPreparedException extends IOException {

        /**
         * Creates a new <tt>DocumentNotPreparedException</tt>.
         *
         * @param message The message which should be appended to provide information.
         */

        public DocumentNotPreparedException(final String message) {
            super(message);
        }

    }

}
