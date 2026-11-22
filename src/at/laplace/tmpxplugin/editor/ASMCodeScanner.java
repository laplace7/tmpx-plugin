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

import java.util.ArrayList;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Device;

import at.laplace.tmpxplugin.Activator;
import at.laplace.tmpxplugin.Constants;
import at.laplace.tmpxplugin.TextAttributeConverter;


/**
 * RuleBasedScanner for the ASMEditor.
 * 
 * @author Andy Reek
 * @since 15.11.2005
 */
public class ASMCodeScanner extends RuleBasedScanner implements IPropertyChangeListener {

    private final ASMEditor editor;

    private Token defaultToken;   
	private Token instructionToken;
    private Token instructionBranchToken;
	private Token directiveToken;
	private Token constantToken;
    private Token stringToken;
    private Token macroToken;
    private Token operatorToken;
    private Token directiveBlockToken;
    private Token directiveBranchToken;
    private Token commentToken;

	/**
	 * The constructor.
	 * 
	 * @param editor The underlying ASMEditor for the CodeScanner.
	 */
	public ASMCodeScanner(final ASMEditor editor) {
		this.editor = editor;
		ArrayList<IRule> rules = new ArrayList<IRule>();
		createTokens(editor.getSite().getShell().getDisplay());

		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		
		rules.add(new TmpxScanner());
		setRules(rules.toArray(new IRule[] {}));
	}
		
	private class TmpxScanner implements IRule {
	    
	    private final StringBuilder builder;
	    private int startColumn;
	    private ICharacterScanner scanner;
	    // Two Issue: 
	    // - dot is the or-operator, or can be the start of a directive (.if).
	    // - hash (#) can start a macro expansion (#macro), or are can be the prefix of an argument (lda #10)
	    // 
	    private boolean allowArguments;
	    
	    public TmpxScanner() {
	        builder = new StringBuilder();
	    }
        
        private int read() {
            return scanner.read();
        }
	    
	    private IToken token(IToken token) {
	        // Keep comment for debugging
	        // Activator.getConsole().println("[%d] Token: '%s'".formatted(startColumn,  builder.toString()));
	        return token;
	    }
	    
	    @Override
	    public IToken evaluate(ICharacterScanner scanner) {
	        this.scanner = scanner;
	        reset();
	        startColumn = scanner.getColumn();
	        if (startColumn == 0) {
	            allowArguments = false;
	        }
            // Keep comment for debugging
            // Activator.getConsole().println("[%d] evaluate".formatted(startColumn));
	        
	        int c = read();
	        if (Character.isWhitespace(c)) {
	            return token(Token.WHITESPACE);
	        }
	        
	        if (c == ';') {
	            append(c);
	            return scanComment();
	        }
	        
	        if (!allowArguments) {
	            // first try to detect macro-call, instructions and directives
	            if (c == '#') {
	                append(c);
	                return scanMacroCall();
	            }
	            
	            if (isWordStart(c)) {
	                append(c);
	                return scanWord();
	            }
	        }
	        
	        if (c == '"') {
	            append(c);
	            return scanString();
	        }
	        
	        if (c == '$') {
                append(c);
	            return scanHexNumber();
	        }
	        
	        if (c == '%') {
                append(c);
	            return scanBinaryNumber();
	        }
	        
	        if (c == '\\') {
                append(c);
	            return scanMacroParameter();
	        }
	        
	        if (isDigit(c)) {
                append(c);
	            return scanNumber();
	        }
	        
	        if (isOperator(c)) {
	            append(c);
	            return scanOperator();
	        }
            
            if (isWordStart(c)) {
                append(c);
                return scanWord();
            }
	        	        
	        scanner.unread();
	        return Token.UNDEFINED;
	    }
	    
	    private void reset() {
	        builder.setLength(0);
	    }
	    
	    private void append(int c) {
	        builder.append((char) c);
	    }
	    
	    IToken scanComment() {
	        while (true) {
	            int c = read();
	            if (c == '\r' || c == '\n' || c == ICharacterScanner.EOF) {
	                scanner.unread();
	                break;
	            }
	            append(c);
	        }
	        return token(commentToken);
	    }
        
        IToken scanString() {
            while (true) {
                // tmpx does not have escape sequences that need special handling:
                // Examples: {pound}, {127}, {$20}
                int c = read();
                if (c == '\r' || c == '\n' || c == ICharacterScanner.EOF) {
                    // unterminated string!
                    scanner.unread();
                    break;
                }
                if (c == '\"') {
                    // terminated string
                    append(c);
                    break;
                }
                append(c);
            }
            return token(stringToken);
        }
        
        IToken scanHexNumber() {
            int c = read();
            while (isHexDigit(c)) {
                append(c);
                c = read();
            }
            scanner.unread();
            return token(constantToken);
        }
        
        IToken scanBinaryNumber() {
            int c = read();
            while (isBinaryDigit(c)) {
                append(c);
                c = read();
            }
            scanner.unread();
            return token(constantToken);
        }
        
        IToken scanMacroParameter() {
            int c = read();
            while (isDigit(c)) {
                append(c);
                c = read();
            }
            scanner.unread();
            return token(constantToken);
        }
        
        IToken scanNumber() {
            int c = read();
            while (isDigit(c)) {
                append(c);
                c = read();
            }
            scanner.unread();
            return token(constantToken);
        }
        
        IToken scanOperator() {
            return token(operatorToken);
        }
        
        IToken scanMacroCall() {
            allowArguments = true;
            
            int c = read();
            while (isWordPart(c)) {
                append(c);
                c = read();
            }
            scanner.unread();
            return token(macroToken);            
        }
        
        IToken scanWord() {
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
        
        static boolean isDigit(int c) {
            return '0' <= c && c <= '9';
        }
        
        static boolean isBinaryDigit(int c) {
            return '0' == c || '1' == c;
        }
        
        static boolean isHexDigit(int c) {
            if (isDigit(c)) {
                return true;
            }
            return 'a' <= c && c <= 'f' || 'A' <= c && c >= 'F';
        }
        
        static boolean isOperator(int c) {
            return switch (c) {
            case '#','<','>','+','-','*','/','&','.',':','(',')','=','!','{','}' -> true;
            default -> false;
            };
        }
        
        static boolean isWordStart(int c) {
            return c == '.' || c == '_' || 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z';
        }
        
        static boolean isWordPart(int c) {
            return isWordStart(c) || isDigit(c) || c == '@';            
        }
	    
        Token getTokenForWord(String word) {
            boolean instruction = isInstruction(word);
            var kind = ASMInstructionSet.getKind().get(word);
            switch (kind) {
            case null: break;
            case "block": return directiveBlockToken;
            case "branch": return instruction ? instructionBranchToken : directiveBranchToken;
            default: break;
            }
            
            if (isInstruction(word)) {
                return instructionToken;
            }
            if (isDirective(word)) {
                return directiveToken;
            }
            return defaultToken;
        }
        
        private boolean isInstruction(String word) {
            return ASMInstructionSet.getInstructions().containsKey(word);
        }
        
        private boolean isDirective(String word) {
            return ASMInstructionSet.getDirectives().containsKey(word);
        }
	}
	
	/**
	 * Disposes the PropertyChangeListener from the PreferenceStore.
	 */
	public void dispose() {
		Activator.getDefault().getPreferenceStore().removePropertyChangeListener(this);
	}

	/**
	 * Create all Tokens.
	 * 
	 * @param device The device is needed for the color of the Tokens.
	 */
	private void createTokens(Device device) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		defaultToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_DEFAULT); 
		instructionToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_INSTRUCTION);
        instructionBranchToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_INSTRUCTION_BRANCH);
		directiveToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_DIRECTIVE);
		constantToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_CONSTANT);
		stringToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_STRING);
        macroToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_MACRO);
        operatorToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_OPERATOR);
        directiveBlockToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_DIRECTIVE_BLOCK);
        directiveBranchToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_DIRECTIVE_BRANCH);
        commentToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_COMMENT);

	}

	private static Token getToken(IPreferenceStore store, String key) {

		return new Token(TextAttributeConverter
				.preferenceDataToTextAttribute(store.getString(key)));
	}

	/**
	 * {@inheritDoc}
	 */
	public void propertyChange(PropertyChangeEvent event) {
	    var token = getTokenForProperty(event.getProperty());
	    if (token != null) {
            token.setData(TextAttributeConverter.preferenceDataToTextAttribute((String) event.getNewValue()));	        
            editor.refreshSourceViewer();
	    }
	}
	
	private Token getTokenForProperty(String property) {
	    return switch (property) {
        case Constants.PREFERENCES_TEXTCOLOR_DEFAULT  -> defaultToken;
	    case Constants.PREFERENCES_TEXTCOLOR_INSTRUCTION  -> instructionToken;
        case Constants.PREFERENCES_TEXTCOLOR_INSTRUCTION_BRANCH  -> instructionBranchToken;
        case Constants.PREFERENCES_TEXTCOLOR_DIRECTIVE  -> directiveToken;
        case Constants.PREFERENCES_TEXTCOLOR_CONSTANT  -> constantToken;
        case Constants.PREFERENCES_TEXTCOLOR_STRING  -> stringToken;
        case Constants.PREFERENCES_TEXTCOLOR_MACRO  -> macroToken;
        case Constants.PREFERENCES_TEXTCOLOR_OPERATOR  -> operatorToken;
        case Constants.PREFERENCES_TEXTCOLOR_DIRECTIVE_BLOCK  -> directiveBlockToken;
        case Constants.PREFERENCES_TEXTCOLOR_DIRECTIVE_BRANCH  -> directiveBranchToken;
        case Constants.PREFERENCES_TEXTCOLOR_COMMENT  -> commentToken;
	    default -> null;
	    };
	}

}
