package best.lettuce.commands;

import best.lettuce.Lettuce;
import best.lettuce.commands.impl.CommandBind;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class CommandManager {
    public static String CHAT_PREFIX = ".";
    public List<Command> commands = new ArrayList<>();

    public void init() {
        commands.add(new CommandBind());
    }

    public boolean execute(String txt) {
        if (!txt.startsWith(CHAT_PREFIX)) return false;
        String[] arguments = txt.substring(1).split(" ");
        for (Command command : commands) {
            if (command.getName().equalsIgnoreCase(arguments[0])
                    || Arrays.stream(command.getOtherPrefixes()).anyMatch(p -> p.equalsIgnoreCase(arguments[0]))) {
                command.execute(Arrays.copyOfRange(arguments, 1, arguments.length));
                return true;
            }
        }
        Lettuce.text("Invalid command.");
        return false;
    }

    public Command getCommand(Class<? extends Command> command) {
        return getCommands().stream().filter(com -> command == com.getClass()).findFirst().orElse(null);
    }
}