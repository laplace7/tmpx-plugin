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
package at.laplace.tmpxplugin.ui.editor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import at.laplace.tmpxplugin.Activator;
import at.laplace.tmpxplugin.Constants;
import at.laplace.tmpxplugin.TextAttributeConverter;
import at.laplace.tmpxplugin.scanner.ITokenFactory;
import at.laplace.tmpxplugin.scanner.TmpScanner;


/**
 * RuleBasedScanner for the ASMEditor.
 * 
 * @author Andy Reek
 * @since 15.11.2005
 */
public class ASMCodeScanner extends RuleBasedScanner implements IPropertyChangeListener, ITokenFactory<IToken> {

    private final ASMEditor editor;

    private Token defaultToken;   
	private Token instructionToken;
    private Token instructionBranchToken;
	private Token directiveToken;
	private Token constantToken;
    private Token stringToken;
    private Token macroCallToken;
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
		
		createTokens();

		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		
		var rule = new TmpScannerRule(this);
		setRules(new IRule[] { rule });
	}
		
	public static class TmpScannerRule extends TmpScanner<IToken> implements IRule {
	    
        public TmpScannerRule(ITokenFactory<IToken> factory) {
            super(factory);
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
	 */
	private void createTokens() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		defaultToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_DEFAULT); 
		instructionToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_INSTRUCTION);
        instructionBranchToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_INSTRUCTION_BRANCH);
		directiveToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_DIRECTIVE);
		constantToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_CONSTANT);
		stringToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_STRING);
        macroCallToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_MACRO_CALL);
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
        case Constants.PREFERENCES_TEXTCOLOR_MACRO_CALL  -> macroCallToken;
        case Constants.PREFERENCES_TEXTCOLOR_OPERATOR  -> operatorToken;
        case Constants.PREFERENCES_TEXTCOLOR_DIRECTIVE_BLOCK  -> directiveBlockToken;
        case Constants.PREFERENCES_TEXTCOLOR_DIRECTIVE_BRANCH  -> directiveBranchToken;
        case Constants.PREFERENCES_TEXTCOLOR_COMMENT  -> commentToken;
	    default -> null;
	    };
	}

	// ITokenFactory
	
	@Override
	public void setBuilder(StringBuilder builder) {
	    // not needed here	    
	}
	
	@Override
	public IToken eol() {
        return Token.UNDEFINED;
	}
	
    @Override
    public IToken undefined() {
        return Token.UNDEFINED;
    }

    @Override
    public IToken whitespace() {
        return Token.WHITESPACE;
    }

    @Override
    public IToken comment() {
        return commentToken;
    }

    @Override
    public IToken string() {
        return stringToken;
    }

    @Override
    public IToken constant() {
        return constantToken;
    }

    @Override
    public IToken operator() {
        return operatorToken;
    }

    @Override
    public IToken macroCall() {
        return macroCallToken;
    }

    @Override
    public IToken directive(String kind) {
        return switch (kind) {
        case null -> directiveToken;
        case "block" -> directiveBlockToken;
        case "branch" -> directiveBranchToken;  
        default -> directiveToken;
        };
    }

    @Override
    public IToken instruction(String kind) {
        return switch (kind) {
        case null -> instructionToken;
        case "branch" -> instructionBranchToken;
        default -> instructionToken;
        };
    }

    @Override
    public IToken label() {
        return defaultToken;
    }

}
