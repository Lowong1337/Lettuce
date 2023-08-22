package best.lettuce.utils;

import best.lettuce.gui.clickguis.dropdown.utils.SearchBar;
import best.lettuce.utils.fonts.CustomFont;
import best.lettuce.utils.fonts.FontUtils;
import net.minecraft.client.Minecraft;

public interface MinecraftInstance {
    String NAME = "Lettuce", VERSION = "0.1", AUTHOR = "Lettuce Organization", WEBSITE = "lettuce.gay";
    Minecraft mc = Minecraft.getMinecraft();

    SearchBar searchBar = new SearchBar();

    FontUtils.FontType lettuceFont = FontUtils.FontType.GEOLOGICA,
            iconFont = FontUtils.FontType.ICON,
            icontestFont = FontUtils.FontType.ICONTEST,
            neverloseFont = FontUtils.FontType.NEVERLOSE,
            tahomaFont = FontUtils.FontType.TAHOMA;
            //rubikFont = FontUtils.FontType.RUBIK;

    //Lettuce fonts
    CustomFont lettuceFont14 = lettuceFont.size(14),
            lettuceFont16 = lettuceFont.size(16),
            lettuceFont18 = lettuceFont.size(18),
            lettuceFont20 = lettuceFont.size(20),
            lettuceFont22 = lettuceFont.size(22),
            lettuceFont24 = lettuceFont.size(24),
            lettuceFont26 = lettuceFont.size(26),
            lettuceFont32 = lettuceFont.size(32),
            lettuceFont40 = lettuceFont.size(40),
            lettuceFont60 = lettuceFont.size(60);

    //Bold Lettuce fonts
    CustomFont lettuceBoldFont14 = lettuceFont14.getBoldFont(),
            lettuceBoldFont16 = lettuceFont16.getBoldFont(),
            lettuceBoldFont18 = lettuceFont18.getBoldFont(),
            lettuceBoldFont20 = lettuceFont20.getBoldFont(),
            lettuceBoldFont22 = lettuceFont22.getBoldFont(),
            lettuceBoldFont26 = lettuceFont26.getBoldFont(),
            lettuceBoldFont32 = lettuceFont32.getBoldFont(),
            lettuceBoldFont60 = lettuceFont60.getBoldFont();

    //Tahoma fonts
    CustomFont tahoma20 = tahomaFont.size(20),
    tahoma18 = tahomaFont.size(18);

    //Tahoma Bold fonts
    CustomFont tahomaBold20 = tahoma20.getBoldFont();

    //Neverlose fonts
    CustomFont neverlose20 = neverloseFont.size(20);

    //Neverlose Bold fonts
    CustomFont neverloseBold20 = neverlose20.getBoldFont();

    //Icon Fonts
    CustomFont iconFont16 = iconFont.size(16),
            iconFont20 = iconFont.size(20),
            iconFont35 = iconFont.size(35),
            iconFont40 = iconFont.size(40);

    CustomFont icontestFont16 = icontestFont.size(16),
            icontestFont20 = icontestFont.size(20),
            icontestFont26 = icontestFont.size(26),
            icontestFont40 = icontestFont.size(40),
            icontestFont70 = icontestFont.size(70),
            icontestFont80 = icontestFont.size(80),
            icontestFont90 = icontestFont.size(90),
            icontestFont100 = icontestFont.size(100);
}