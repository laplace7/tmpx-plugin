package at.laplace.tests;

import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import at.laplace.tmpxplugin.Patterns;

public class TestPatterns {

    @Test
    public void testNewImportRegex() throws Exception {
       assertTrue(Patterns.IMPORT_PATTERN.matcher("#import \"tjo.inc\"").matches());
    }
    
    @Test
    public void testNewImportRegexBad() throws Exception {
       assertFalse(Patterns.IMPORT_PATTERN.matcher("#import bla \"tjo.inc\"").matches());
       assertFalse(Patterns.IMPORT_PATTERN.matcher("#import tjo.inc\"").matches());
    }
    
    
    @Test
    public void testImportifRegex() throws Exception {
       Matcher m = Patterns.IMPORTIF_PATTERN.matcher("#importif BLA_BLA \"tjo.inc\"");
       assertTrue(m.matches());
       assertEquals(m.group(m.groupCount()), "tjo.inc");
    }
    

    @Test
    public void testImportifRegexBad() throws Exception {
       assertFalse(Patterns.IMPORTIF_PATTERN.matcher("#importif BLA_BLA \"tjo.inc").matches());
    }


    @Test
    public void testImportifRegexMulti() throws Exception {
       Matcher m = Patterns.IMPORTIF_PATTERN.matcher("#importif BLA_BLA && tjotjo || !bla \"tjo.inc\"");
       assertTrue(m.matches());
       assertEquals(m.group(m.groupCount()), "tjo.inc");
    }

}
