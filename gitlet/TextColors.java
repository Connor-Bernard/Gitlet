package gitlet;
/**
 * Enumeration for use of changing the color of text when printing.
 * Replace definitions with commented section to enable.
 * @author Connor Bernard
 */
public enum TextColors {
    /*RESET_COLOR("\u001B[0m"),
    ERROR_COLOR("\u001B[1;31m"),
    WARNING_COLOR("\u001B[1;33m"),
    COMPLETION_COLOR("\u001B[1;32m");*/
    /**
     * Different colors for use when printing text.
     */
    RESET_COLOR(""),
    ERROR_COLOR(""),
    WARNING_COLOR(""),
    COMPLETION_COLOR("");
    /**
     * Color code value for each color.
     */
    private final String colorCode;
    /**
     * Standard color code constructor.
     * @param colorString String the enum is to represent.
     */
    TextColors(String colorString) {
        colorCode = colorString;
    }
    @Override
    public String toString() {
        return colorCode;
    }
}
