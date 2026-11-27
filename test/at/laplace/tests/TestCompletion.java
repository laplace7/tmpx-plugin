package at.laplace.tests;

import static at.laplace.tmpxplugin.ui.editor.PrefixExtractor.extract;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import at.laplace.tmpxplugin.ui.editor.PrefixExtractor.Prefix;
import at.laplace.tmpxplugin.ui.editor.PrefixExtractor.PrefixType;

class TestCompletion {
    
    private static void assertPrefix(Prefix prefix, PrefixType type, String text) {
        assertEquals(text, prefix.prefix(), "prefix");
        assertEquals(type, prefix.type(), "type");        
    }

    @Test
    void emptyLine() {
        var prefix = extract("", 0);
        assertPrefix(prefix, PrefixType.UNKNOWN, "");
    }

    @Test
    void label() {
        var prefix = extract("label1 .macro ; comment", 0);
        assertPrefix(prefix, PrefixType.UNKNOWN, "");
    }

    @Test
    void directive1() {
        var prefix = extract("label1 .macro ; comment", 7);
        assertPrefix(prefix, PrefixType.UNKNOWN, "");
    }

    @Test
    void directive2() {
        var prefix = extract("label1 .macro ; comment", 8);
        assertPrefix(prefix, PrefixType.DIRECTIVE, "");
    }

    @Test
    void directive3() {
        var prefix = extract("label1 .macro ; comment", 9);
        assertPrefix(prefix, PrefixType.DIRECTIVE, "m");
    }

    @Test
    void parameter1() {
        var prefix = extract("*=", 2);
        assertPrefix(prefix, PrefixType.UNKNOWN, "=");
    }

    @Test
    void parameter2() {
        var prefix = extract("  lda ", 6);
        assertPrefix(prefix, PrefixType.UNKNOWN, "");
    }
    
}
