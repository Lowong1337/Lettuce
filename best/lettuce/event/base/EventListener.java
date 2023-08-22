package best.lettuce.event.base;

public interface EventListener<T> {
    void call(T event);
}