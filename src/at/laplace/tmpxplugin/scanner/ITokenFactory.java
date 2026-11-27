package at.laplace.tmpxplugin.scanner;

public interface ITokenFactory<T> {
    void setBuilder(StringBuilder builder);
    T eol();
    T undefined();
    T whitespace();
    T comment();
    T string();
    T constant();
    T operator();
    T macroCall();
    T directive(String kind);
    T instruction(String kind);
    T label();
}
