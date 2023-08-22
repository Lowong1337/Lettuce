package best.lettuce.commands;

import best.lettuce.Lecture;
import best.lettuce.utils.MinecraftInstance;
import lombok.Getter;
import net.minecraft.util.EnumChatFormatting;

@Getter
public abstract class Command implements MinecraftInstance {
    private final String name, description, usage;
    private final String[] otherPrefixes;
    public static boolean sendSuccess = true;

    public Command(String name, String description, String usage, String... otherPrefixes) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.otherPrefixes = otherPrefixes;
    }

    public abstract void execute(String[] args);

    public void sendChatWithPrefix(String message) {
        if (sendSuccess) Lecture.text(message);
    }

    public void sendChatError(String message) {
        Lecture.text(EnumChatFormatting.RED + "ERROR: " + message);
    }

    public void sendChatWithInfo(String message) {
        if (sendSuccess) Lecture.text(EnumChatFormatting.WHITE + "INFO: " + message);
    }

    public void usage() {
        Lecture.text("Usage: " + usage);
    }
}