/*
 Kick Assembler plugin - An Eclipse plugin for convenient Kick Assembling
 Copyright (c) 2012 - P-a Backstrom <pa.backstrom@gmail.com>
 
 Based on ASMPlugin - http://sourceforge.net/projects/asmplugin/
 Copyright (c) 2006 - Andy Reek, D. Mitte
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package at.laplace.tmpxplugin.editor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.Status;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import at.laplace.tmpxplugin.Activator;
import at.laplace.tmpxplugin.Constants;
import at.laplace.tmpxplugin.Messages;

/**
 * Loads an manages the instructions, which are uses in the editor.
 * 
 * @author Daniel Mitte
 * @since 24.02.2006
 */
public final class ASMInstructionSet {

    private static final Map<String, String> instructionMap = new TreeMap<>();
    private static final Map<String, String> directiveMap = new TreeMap<>();
    private static final Map<String, String> blockMap = new TreeMap<>();
    // shared by all entries
    private static final Map<String, String> kindMap = new HashMap<>();

    static {
        loadXMLData();
    }
    
    /**
     * Must not be instantiated.
     */
    private ASMInstructionSet() {
        // Must not be instantiated.
    }

    public static Map<String, String> getInstructions() {
        return instructionMap;
    }

    public static Map<String, String> getDirectives() {
        return directiveMap;
    }

    public static Map<String, String> getBlocks() {
        return blockMap;
    }
    
    public static Map<String, String> getKind() {
        return kindMap;
    }

    /**
     * Loads the instructions.
     */
    private static void loadXMLData() {
        String xmlfile = Activator
                .getFilePathFromPlugin("asm_instruction_set.xml");

        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(new File(xmlfile), new DefaultHandler() {
                public void startElement(String uri, String localName,
                        String qName, Attributes attributes)
                        throws SAXException {
                    String key = null;
                    var kind = attributes.getValue("kind");
                    if (qName.equals("instruction")) {
                        key = attributes.getValue("command");
                        instructionMap.put(key,
                                attributes.getValue("description"));
                    } else if (qName.equals("directive")) {
                        key = attributes.getValue("field");
                        directiveMap.put(key,
                                attributes.getValue("description"));
                    } else if (qName.equals("block")) {
                        key = attributes.getValue("field");
                        blockMap.put(key,
                                attributes.getValue("description"));
                    }
                    if (key != null && kind != null) {
                        kindMap.put(key, kind);
                    }
                }
            });
        } catch (Exception e) {
            Activator.getDefault().getLog()
                    .log(new Status(Status.ERROR, Constants.PLUGIN_ID,
                            Status.OK, Messages.LOAD_ASMISET_ERROR, e));
        }

    }
}
