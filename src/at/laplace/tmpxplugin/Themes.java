package at.laplace.tmpxplugin;

public class Themes {
    
    public static String[] THEMES = {
            "Light Theme", "Dark Theme"
    };
    
    public static Attributes[] LIGHT_THEME = {
            attr(Constants.PREFERENCES_TEXTCOLOR_DEFAULT, 36, 41, 46, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_INSTRUCTION, 0, 92, 197, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_INSTRUCTION_BRANCH, 175, 0, 219, true, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_DIRECTIVE, 111, 66, 193, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_DIRECTIVE_BLOCK, 0, 112, 112, true, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_DIRECTIVE_BRANCH, 179, 89, 0, true, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_CONSTANT, 9, 134, 88, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_STRING, 163, 21, 21, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_MACRO_CALL, 0, 92, 197, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_OPERATOR, 90, 98, 106, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_COMMENT, 106, 115, 125, false, true),
    };

    public static Attributes[] DARK_THEME = {
            attr(Constants.PREFERENCES_TEXTCOLOR_DEFAULT, 212, 212, 212, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_INSTRUCTION, 86, 156, 214, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_INSTRUCTION_BRANCH, 218, 112, 214, true, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_DIRECTIVE, 197, 134, 192, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_DIRECTIVE_BRANCH, 229, 192, 123, true, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_DIRECTIVE_BLOCK, 78, 201, 176, true, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_CONSTANT, 181, 206, 168, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_STRING, 206, 145, 120, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_MACRO_CALL, 86, 156, 214, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_OPERATOR, 160, 168, 176, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_COMMENT, 106, 153, 85, false, true),
    };
    
    public static Attributes[] getTheme(int index) {
        if (index == 1) {
            return DARK_THEME;
        }
        return LIGHT_THEME;
    }
    
    public record Attributes(String key, int r, int g, int b, boolean bold, boolean italic) {
    }
    
    static Attributes attr(String key, int r, int g, int b, boolean bold, boolean italic) {
        return new Attributes(key, r, g, b, bold, italic);
    }

}
