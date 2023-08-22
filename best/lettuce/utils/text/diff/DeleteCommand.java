package best.lettuce.utils.text.diff;

/**
 * Command representing the deletion of one object of the first sequence.
 * <p>
 * When one object of the first sequence has no corresponding object in the
 * second sequence at the right place, the {@link EditScript edit script}
 * transforming the first sequence into the second sequence uses an instance of
 * this class to represent the deletion of this object. The objects embedded in
 * these type of commands always come from the first sequence.
 * </p>
 *
 * @see StringsComparator
 * @see EditScript
 *
 * @param <T> object type
 * @since 1.0
 */
public class DeleteCommand<T> extends EditCommand<T> {

    /**
     * Simple constructor. Creates a new instance of {@link DeleteCommand}.
     *
     * @param object  the object of the first sequence that should be deleted
     */
    public DeleteCommand(final T object) {
        super(object);
    }

    /**
     * Accept a visitor. When a {@code DeleteCommand} accepts a visitor, it calls
     * its {@link CommandVisitor#visitDeleteCommand visitDeleteCommand} method.
     *
     * @param visitor  the visitor to be accepted
     */
    @Override
    public void accept(final CommandVisitor<T> visitor) {
        visitor.visitDeleteCommand(getObject());
    }
}
