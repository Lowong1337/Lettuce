package best.lettuce.commands.impl;

import best.lettuce.Lettuce;
import best.lettuce.commands.Command;
import best.lettuce.modules.Module;
import org.lwjgl.input.Keyboard;

public class CommandBind extends Command {

    public CommandBind() {
        super("bind", "Binds a module to a certain key", ".bind [module] [key]" + "\n" +
                "Binding through the ClickGUI is highly recommended.");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 2) {
            usage();
        } else {
            String stringModule = args[0];
            try {
                Module module = Lettuce.INSTANCE.getModuleManager().getModuleByName(stringModule);
                module.getKeybind().setCode(Keyboard.getKeyIndex(args[1].toUpperCase()));
                sendChatWithPrefix("Keybind for " + module.getName() + " was set to " + args[1].toUpperCase());
            } catch (Exception e) {
                usage();
            }
        }
    }
}