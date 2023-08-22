import net.minecraft.client.main.Main;

import java.io.File;

public class Start {
    public static void main(String[] args) {
        final String appdata = System.getenv("APPDATA");
        final String folder = (appdata != null) ? appdata : System.getProperty("user.home", ".");
        File file = new File(folder, ".minecraft/");

        Main.main(new String[]{
                "--version", "1.8.9",
                "--accessToken", "0",
                "--assetIndex", "1.8",
                "--userProperties", "{}",
                "--gameDir", new File(file, ".").getAbsolutePath(),
                "--assetsDir", new File(file, "assets/").getAbsolutePath()});
    }
}