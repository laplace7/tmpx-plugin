package at.laplace.tmpxplugin.scanner;

import org.eclipse.jface.text.rules.ICharacterScanner;

import at.laplace.tmpxplugin.Activator;
import at.laplace.tmpxplugin.ui.editor.ASMInstructionSet;

public class TmpScanner<T> {
    
    private final ITokenFactory<T> factory;
    private final StringBuilder builder;
    private boolean verbose;
    private int startColumn;
    private ICharacterScanner scanner;
    // Two Issue: 
    // - dot is the or-operator, or can be the start of a directive (.if).
    // - hash (#) can start a macro expansion (#macro), or are can be the prefix of an argument (lda #10)
    // 
    private boolean allowArguments;
    
    public TmpScanner(ITokenFactory<T> factory) {
        this.factory = factory;
        builder = new StringBuilder();
        factory.setBuilder(builder);
    }
    
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    private int read() {
        return scanner.read();
    }
    
    private T token(T token) {
        if (verbose) {
            Activator.getConsole().println("[%d] Token: '%s'".formatted(startColumn,  builder.toString()));
        }
        return token;
    }
    
    public T evaluate(ICharacterScanner scanner) {
        this.scanner = scanner;
        reset();
        startColumn = scanner.getColumn();
        if (startColumn == 0) {
            allowArguments = false;
        }
        if (verbose) {
            Activator.getConsole().println("[%d] evaluate".formatted(startColumn));
        }
        
        int c = read();
        if (c == -1) {
            return token(factory.eol());
        }
        
        append(c);
        if (Character.isWhitespace(c)) {
            return scanWhitespaces(c);
        }
        
        if (c == ';') {
            return scanComment();
        }
        
        if (!allowArguments) {
            // first try to detect macro-call, instructions and directives
            if (c == '#') {
                return scanMacroCall();
            }
            
            if (isWordStart(c)) {
                return scanWord();
            }
        }
        
        if (c == '"') {
            return scanString();
        }
        
        if (c == '$') {
            return scanHexNumber();
        }
        
        if (c == '%') {
            return scanBinaryNumber();
        }
        
        if (c == '\\') {
            return scanMacroParameter();
        }
        
        if (isDigit(c)) {
            return scanNumber();
        }
        
        if (isOperator(c)) {
            return scanOperator();
        }
        
        if (isWordStart(c)) {
            return scanWord();
        }
                    
        return token(factory.undefined());
    }
    
    private void reset() {
        builder.setLength(0);
    }
    
    private void append(int c) {
        builder.append((char) c);
    }
    
    T scanWhitespaces(int start) {
        // stop at line break
        // we skip over \r though!
        if (start == '\n') {
            return token(factory.whitespace());
        }
        int c = read();
        while (Character.isWhitespace(c)) {
            append(c);
            if (c == '\n') {
                return token(factory.whitespace());                
            }
            c = read();
        }
        scanner.unread();
        return token(factory.whitespace());
    }
    
    T scanComment() {
        while (true) {
            int c = read();
            if (c == '\r' || c == '\n' || c == ICharacterScanner.EOF) {
                scanner.unread();
                break;
            }
            append(c);
        }
        return token(factory.comment());
    }
    
    T scanString() {
        while (true) {
            // tmpx does not have escape sequences that need special handling:
            // Examples: {pound}, {127}, {$20}
            int c = read();
            if (c == '\r' || c == '\n' || c == ICharacterScanner.EOF) {
                // unterminated string!
                scanner.unread();
                break;
            }
            append(c);
            if (c == '\"') {
                // terminated string
                break;
            }
        }
        return token(factory.string());
    }
    
    T scanHexNumber() {
        int c = read();
        while (isHexDigit(c)) {
            append(c);
            c = read();
        }
        scanner.unread();
        return token(factory.constant());
    }
    
    T scanBinaryNumber() {
        int c = read();
        while (isBinaryDigit(c)) {
            append(c);
            c = read();
        }
        scanner.unread();
        return token(factory.constant());
    }
    
    T scanMacroParameter() {
        int c = read();
        while (isDigit(c)) {
            append(c);
            c = read();
        }
        scanner.unread();
        return token(factory.constant());
    }
    
    T scanNumber() {
        int c = read();
        while (isDigit(c)) {
            append(c);
            c = read();
        }
        scanner.unread();
        return token(factory.constant());
    }
    
    T scanOperator() {
        return token(factory.operator());
    }
    
    T scanMacroCall() {
        allowArguments = true;
        int c = read();
        while (isWordPart(c)) {
            append(c);
            c = read();
        }
        scanner.unread();
        return token(factory.macroCall());            
    }
    
    T scanWord() {
        int c = read();
        while (isWordPart(c)) {
            append(c);
            c = read();
        }
        scanner.unread();
        
        var word = builder.toString();
        if (isInstruction(word) || isDirective(word)) {
            allowArguments = true;
        }
        return token(getTokenForWord(word));
    }
        
    public static boolean isDigit(int c) {
        return '0' <= c && c <= '9';
    }
    
    public static boolean isBinaryDigit(int c) {
        return '0' == c || '1' == c;
    }
    
    public static boolean isHexDigit(int c) {
        if (isDigit(c)) {
            return true;
        }
        return 'a' <= c && c <= 'f' || 'A' <= c && c >= 'F';
    }
    
    public static boolean isOperator(int c) {
        return switch (c) {
        case '#','<','>','+','-','*','/','&','.',',',':','(',')','=','!','{','}' -> true;
        default -> false;
        };
    }
    
    public static boolean isWordStart(int c) {
        return c == '.' || c == '_' || 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z';
    }
    
    public static boolean isWordPart(int c) {
        return isWordStart(c) || isDigit(c) || c == '@';            
    }
    
    T getTokenForWord(String word) {
        var kind = ASMInstructionSet.getKind().get(word);        
        if (isInstruction(word)) {
            return factory.instruction(kind);
        }
        if (isDirective(word)) {
            return factory.directive(kind);
        }
        return factory.label();
    }
    
    private boolean isInstruction(String word) {
        return ASMInstructionSet.getInstructions().containsKey(word);
    }
    
    private boolean isDirective(String word) {
        return ASMInstructionSet.getDirectives().containsKey(word);
    }
}
