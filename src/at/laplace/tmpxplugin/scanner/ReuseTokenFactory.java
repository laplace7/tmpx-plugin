package at.laplace.tmpxplugin.scanner;

public class ReuseTokenFactory implements ITokenFactory<Token> {

    private final Token token;
    private StringBuilder builder;
    
    public ReuseTokenFactory() {
        token = new Token();
    }
    
    @Override
    public void setBuilder(StringBuilder builder) {
        this.builder = builder;            
    }

    private Token set(TokenType type) {
        token.type = type;
        token.text = builder.toString();
        return token;
    }
    
    @Override
    public Token eol() {
        return set(TokenType.UNDEFINED);
    }

    @Override
    public Token undefined() {
        return set(TokenType.UNDEFINED);
    }

    @Override
    public Token whitespace() {
        return set(TokenType.WHITESPACE);
    }

    @Override
    public Token comment() {
        return set(TokenType.COMMENT);
    }

    @Override
    public Token string() {
        return set(TokenType.STRING);
    }

    @Override
    public Token constant() {
        return set(TokenType.CONSTANT);
    }

    @Override
    public Token operator() {
        return set(TokenType.OPERATOR);
    }

    @Override
    public Token macroCall() {
        return set(TokenType.MACRO_CALL);
    }

    @Override
    public Token directive(String kind) {
        return set(TokenType.DIRECTIVE);
    }

    @Override
    public Token instruction(String kind) {
        return set(TokenType.INSTRUCTION);
    }

    @Override
    public Token label() {
        return set(TokenType.OTHER);
    }
    
}