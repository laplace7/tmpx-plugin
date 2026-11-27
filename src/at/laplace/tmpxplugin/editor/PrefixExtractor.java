package at.laplace.tmpxplugin.editor;

public class PrefixExtractor {

    public static Prefix extract(String line, int offset) {
        int i = offset;
        var type = PrefixType.UNKNOWN;
        while (i > 0) {
            char ch = line.charAt(i - 1);

            if (ch <= ' ' || ch == '<' || ch == '>' || ch == '#'
                    || ch == '!' || ch == '(' || ch == '[' || ch == '\t'
                    || ch == '&' || ch == '|' || ch == '+' || ch == '-'
                    || ch == '*' || ch == '/' || ch == '^') {
                break;
            }

            // TODO handle start of macro #word
            if (i > 1 && ch == '.'
                    && Character.isLetterOrDigit(line.charAt(i - 2))) {

                type = PrefixType.LABEL;
                break;
            }
            if (ch == '.') {
                type = PrefixType.DIRECTIVE;
                break;
            }
            i--;
        }

        var prefix = line.substring(i, offset);
        return new Prefix(prefix, type);        
    }
    
    public enum PrefixType {
        DIRECTIVE,
        LABEL,
        UNKNOWN 
    }

    public record Prefix(String prefix, PrefixType type) {
    }

}
