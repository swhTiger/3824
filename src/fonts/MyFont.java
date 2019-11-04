package fonts;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class MyFont {
    private static Font font;

    static {
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("fonts/汉仪彩蝶体简.ttf")));
        } catch (FontFormatException | IOException e) {
            font = new Font("", Font.PLAIN, 14);
        }
    }

    public static Font getFont(int style, int size) {
        return font.deriveFont(style, size);
    }
}
