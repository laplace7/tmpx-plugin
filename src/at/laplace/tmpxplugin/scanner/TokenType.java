package at.laplace.tmpxplugin.scanner;

public enum TokenType {
    EOL,
    UNDEFINED,
    WHITESPACE,
    COMMENT,
    STRING,
    CONSTANT,
    OPERATOR,
    MACRO_CALL,
    DIRECTIVE,
    INSTRUCTION,
    OTHER
}