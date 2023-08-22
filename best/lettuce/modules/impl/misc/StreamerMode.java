package best.lettuce.modules.impl.misc;

import best.lettuce.modules.Category;
import best.lettuce.modules.Module;
import best.lettuce.modules.property.impl.BooleanProperty;
import best.lettuce.modules.property.impl.StringProperty;

public class StreamerMode extends Module {

    public StreamerMode(){
        super("Streamer Mode", Category.MISC, "Hides your username or others username when you are streaming.");
        addProperties(nameprotect, name, hideothers);
    }

    public final BooleanProperty nameprotect = new BooleanProperty("Name Protect", true);
    public final StringProperty name = new StringProperty("Name", "You", nameprotect::isEnabled);
    public final BooleanProperty hideothers = new BooleanProperty("Hide Other Usernames", false);
}
