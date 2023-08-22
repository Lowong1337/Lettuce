package best.lettuce;

import best.lettuce.commands.CommandManager;
import best.lettuce.config.ConfigManager;
import best.lettuce.config.DragManager;
import best.lettuce.event.Event;
import best.lettuce.event.base.EventManager;
import best.lettuce.gui.altmanager.GuiAltManager;
import best.lettuce.modules.ModuleManager;
import best.lettuce.richpresence.LettuceRichPresence;
import best.lettuce.utils.MinecraftInstance;
import best.lettuce.utils.background.BackgroundProcess;
import de.florianmichael.viamcp.ViaMCP;
import lombok.Getter;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public enum Lecture implements MinecraftInstance {
    INSTANCE;

    public static final File DIRECTORY = new File(mc.mcDataDir, NAME);

    private final EventManager<Event> eventManager = new EventManager<>();
    private final ModuleManager moduleManager = new ModuleManager();
    private final ConfigManager configManager = new ConfigManager();
    private final CommandManager commandManager = new CommandManager();
    private final GuiAltManager altManager = new GuiAltManager();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void onStart() {
        try {
            ViaMCP.create();
            ViaMCP.INSTANCE.initAsyncSlider();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        eventManager.register(new BackgroundProcess());

        moduleManager.init();

        LettuceRichPresence.startrpc();

        ConfigManager.defaultConfig = new File(mc.mcDataDir + "/Lecture/value.json");
        Lecture.INSTANCE.getConfigManager().collectConfigs();
        if (ConfigManager.defaultConfig.exists()) {
            Lecture.INSTANCE.getConfigManager().loadConfig(Lecture.INSTANCE.getConfigManager().readConfigData(ConfigManager.defaultConfig.toPath()), true);
        }

        DragManager.loadDragData();

        commandManager.init();
    }

    public void onStop() {
        Lecture.INSTANCE.getConfigManager().saveDefaultConfig();
        DragManager.saveDragData();
        LettuceRichPresence.shutdownrpc();
    }

    public static void text(String msg, boolean prefix) {
        if (prefix) {
            mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_PURPLE + "Lecture " + EnumChatFormatting.WHITE + EnumChatFormatting.BOLD + ">> " + EnumChatFormatting.RESET + msg));
        } else {
            mc.thePlayer.addChatMessage(new ChatComponentText(msg));
        }
    }

    public static void text(String msg) {
        text(msg, true);
    }
}
