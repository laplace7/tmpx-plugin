package at.laplace.tmpxplugin;

import java.util.regex.Pattern;

public class Patterns {

    public final static Pattern EXTENSION_PATTERN_ALL = Pattern.compile("(asm|inc|sym|s)",Pattern.CASE_INSENSITIVE);
    public final static Pattern EXTENSION_PATTERN_INCLUDES = Pattern.compile("(inc|sym)",Pattern.CASE_INSENSITIVE);
    public final static Pattern EXTENSION_PATTERN_MAINFILES = Pattern.compile("(asm|s)",Pattern.CASE_INSENSITIVE);
    public final static Pattern EXTENSION_PATTERN_PROGRAM = Pattern.compile("prg",Pattern.CASE_INSENSITIVE);

    public final static Pattern LABEL_PATTERN = Pattern.compile("^(\\w+).*(\\r|\\n)*$", Pattern.CASE_INSENSITIVE);
    // TODO remove    
    public final static Pattern LABEL_PATTERN_ALT = Pattern.compile("\\A\\s*(\\w+):.*$", Pattern.CASE_INSENSITIVE);
    
    public final static Pattern MACRO_PATTERN = Pattern.compile("^(\\w+)\\s*\\.(macro|segment).*$", Pattern.CASE_INSENSITIVE);

    public final static Pattern PLUGIN_PATTERN = Pattern.compile("^\\s*\\.plugin\\s*\"(([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*)\".*$");

    // TODO remove
    public final static Pattern PSEUDOCOMMAND_PATTERN = Pattern.compile("^\\s*\\.pseudocommand\\s+(.*)\\{?.*$", Pattern.CASE_INSENSITIVE);

    public static final Pattern FUNCTION_PATTERN = Pattern.compile("^(\\w+).*(\\n|\\r)*$", Pattern.CASE_INSENSITIVE);

    public static final Pattern FILENAMESPACE_PATTERN = Pattern.compile("\\A\\s*.filenamespace\\s+(\\w+).*$", Pattern.CASE_INSENSITIVE);
    public static final Pattern NAMESPACE_PATTERN = Pattern.compile("\\A\\s*.namespace\\s+(\\w+).*$", Pattern.CASE_INSENSITIVE);

    public final static Pattern SPACES_EQUALS_SIGN_SPACES = Pattern.compile("\\s*=\\s*\\S+");
    public final static Pattern PSEUDOCOMMAND_PATTERN_LINE = Pattern.compile("^\\s*\\.pseudocommand\\s+(.*)$", Pattern.CASE_INSENSITIVE);
    public final static Pattern BEGIN_BLOCK_PATTERN = Pattern.compile("\\{.*$");

    public static final Pattern IMPORT_SOURCE_PATTERN = Pattern.compile("^\\s*\\.import\\s+source\\s+\"(.*)\".*\\s?$", Pattern.CASE_INSENSITIVE);
    public static final Pattern IMPORT_PATTERN = Pattern.compile("^\\s*\\#import\\s+\"(.*)\"\\s*$", Pattern.CASE_INSENSITIVE);
    public static final Pattern IMPORTIF_PATTERN = Pattern.compile("^\\s*\\#importif\\s+([a-z_\\&\\|!]+\\s+)+\"(.*)\"\\s*$", Pattern.CASE_INSENSITIVE);


    public static final Pattern CONSTVAR_PATTERN = Pattern.compile("^(\\w+)\\s+(\\=|\\.var)\\s+([^;\\n\\r]*)(;.*)?(\\r|\\n)*$",Pattern.CASE_INSENSITIVE);
    public static final Pattern EQUALS_PATTERN = Pattern.compile("\\s*=.*");
    
    // main.asm(10) : error 32: undefined label; 'label1'
    // include/test.asm(3) : error 5: missing operand at col 9
    // groups: file, line number, error number, message
    public static final Pattern ERROR_PATTERN = Pattern.compile("^([^(]+)\\((\\d+)\\) : error (\\d+): (.+)$");
    // group: column number
    public static final Pattern AT_COL_PATTERN = Pattern.compile(".*at col (\\d+).*");

}
