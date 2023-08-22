package best.lettuce.event.impl.game;

import best.lettuce.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public class EventKeyPressed extends Event {
    private final int key;
}