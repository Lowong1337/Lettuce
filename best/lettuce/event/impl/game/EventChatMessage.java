package best.lettuce.event.impl.game;

import best.lettuce.event.Event;
import net.minecraft.util.IChatComponent;

public class EventChatMessage extends Event {
    public final byte type;
    public IChatComponent message;
    private final String rawMessage;

    public EventChatMessage(byte type, IChatComponent message) {
        this.type = type;
        this.message = message;
        this.rawMessage = message.getUnformattedText();
    }


    public String getRawMessage() {
        return rawMessage;
    }
}
