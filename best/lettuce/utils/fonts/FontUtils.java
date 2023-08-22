package best.lettuce.utils.fonts;

import best.lettuce.utils.MinecraftInstance;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FontUtils implements MinecraftInstance {
    //These are for the icon font for ease of access
    public final static String
            /*BUG = "a",
            LIST = "b",
            BOMB = "c",
            EYE = "d",
            PERSON = "e",
            WHEELCHAIR = "f",
            SCRIPT = "g",
            SKIP_LEFT = "h",
            PAUSE = "i",
            PLAY = "j",
            SKIP_RIGHT = "k",
            SHUFFLE = "l",
            INFO = "m",
            SETTINGS = "n",
            CHECKMARK = "o",
            XMARK = "p",
            TRASH = "q",
            WARNING = "r",
            FOLDER = "s",
            LOAD = "t",
            SAVE = "u",

            DROPDOWN_ARROW = "z",
            PIN = "s",
            EDIT = "A",
            SEARCH = "B",
            UPLOAD = "C",
            REFRESH = "D",
            ADD_FILE = "E",
            STAR_OUTLINE = "F",
            STAR = "G";*/
            CHECKMARK = "A",
            XMARK = "B",
            INFO = "C",
            DROPDOWN_ARROW = "z",
            REFRESH = "D",
            SEARCH = "B",
            WARNING = "D",
            STAR_OUTLINE = "F",
            COMBAT = "E",
            MOVEMENT = "F",
            RENDER = "G",
            PLAYER = "H",
            EXPLOIT = "I",
            MISC = "J",
            CONFIG = "R",
            SAVE = "S",
            SINGLEPLAYER = "L",
            MULTIPLAYER = "M",
            SETTINGS = "N",
            ALTS = "O",
            EXIT = "P",
            HEART = "Q",
            LETTUCE = "T",
            TRASH_BODY = "u",
            TRASH_TOP = "V",
            DISCORD = "W",
            UPDATE = "X",
            GAME_LEFT = "Y",
            GAME_RIGHT = "Z",
            GAMEPAD = "a",
            PLAY = "b",
            PAUSE = "c",
            SHUFFLE = "d",
            SKIP_RIGHT = "e",
            SKIP_LEFT = "f",
            ADD_ALT = "g";

    private static final HashMap<FontType, Map<Integer, CustomFont>> customFontMap = new HashMap<>();

    public static void setupFonts() {
        for (FontType type : FontType.values()) {
            type.setup();
            HashMap<Integer, CustomFont> fontSizes = new HashMap<>();

            if (type.hasBold()) {
                for (int size : type.getSizes()) {
                    CustomFont font = new CustomFont(type.fromSize(size));
                    font.setBoldFont(new CustomFont(type.fromBoldSize(size)));

                    fontSizes.put(size, font);
                }
            } else {
                for (int size : type.getSizes()) {
                    fontSizes.put(size, new CustomFont(type.fromSize(size)));
                }
            }

            customFontMap.put(type, fontSizes);
        }
    }

    @Getter
    public enum FontType {
        TAHOMA("tahoma", "tahoma-bold", 14, 16, 18, 20, 22, 24, 26, 32, 40),
        //RUBIK("rubik", "rubik-bold", 20),
        NEVERLOSE("neverlose", "neverlose-bold", 20),
        GEOLOGICA("geologica", "geologica-bold", 14, 16, 18, 20, 22, 24, 26, 32, 40, 60),
        ICON("icon", 16, 20, 35, 40),
        ICONTEST("icontest", 16, 20,26, 40, 70, 80, 90, 100);

        private final ResourceLocation location, boldLocation;
        private Font font, boldFont;
        private final int[] sizes;

        FontType(String fontName, String boldName, int... sizes) {
            this.location = new ResourceLocation("Lettuce/Fonts/" + fontName + ".ttf");
            this.boldLocation = new ResourceLocation("Lettuce/Fonts/" + boldName + ".ttf");
            this.sizes = sizes;
        }

        FontType(String fontName, int... sizes) {
            this.location = new ResourceLocation("Lettuce/Fonts/" + fontName + ".ttf");
            this.boldLocation = null;
            this.sizes = sizes;
        }

        public boolean hasBold() {
            return boldLocation != null;
        }

        public Font fromSize(int size) {
            return font.deriveFont(Font.PLAIN, size);
        }

        private Font fromBoldSize(int size) {
            return boldFont.deriveFont(Font.PLAIN, size);
        }

        public void setup() {
            font = getFontData(location);
            if (boldLocation != null) {
                boldFont = getFontData(boldLocation);
            }
        }

        public CustomFont size(int size) {
            return customFontMap.get(this).computeIfAbsent(size, k -> null);
        }

        public CustomFont boldSize(int size) {
            return customFontMap.get(this).get(size).getBoldFont();
        }
    }

    private static Font getFontData(ResourceLocation location) {
        try {
            InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
            return Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading font");
            return new Font("default", Font.PLAIN, +10);
        }
    }
}