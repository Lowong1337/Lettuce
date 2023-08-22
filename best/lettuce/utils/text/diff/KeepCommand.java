package best.lettuce.utils.text.diff;

/**
 * Command representing the keeping of one object present in both sequences.
 * <p>
 * When one object of the first sequence {@code equals} another objects in
 * the second sequence at the right place, the {@link EditScript edit script}
 * transforming the first sequence into the second sequence uses an instance of
 * this class to represent the keeping of this object. The objects embedded in
 * these type of commands always come from the first sequence.
 * </p>
 *
 * @see StringsComparator
 * @see EditScript
 *
 * @param <T> object type
 * @since 1.0
 */
public class KeepCommand<T> extends EditCommand<T> {

    /**
     * Simple constructor. Creates a new instance of KeepCommand
     *
     * @param object  the object belonging to both sequences (the object is a
     *   reference to the instance in the first sequence which is known
     *   to be equal to an instance in the second sequence)
     */
    public KeepCommand(final T object) {
        super(object);
    }

    /**
     * Accept a visitor. When a {@code KeepCommand} accepts a visitor, it
     * calls its {@link CommandVisitor#visitKeepCommand visitKeepCommand} method.
     *
     * @param visitor  the visitor to be accepted
     */
    @Override
    public void accept(final CommandVisitor<T> visitor) {
        visitor.visitKeepCommand(getObject());
    }
}
