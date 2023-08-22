package best.lettuce.utils.text.diff;

/**
 * Command representing the insertion of one object of the second sequence.
 * <p>
 * When one object of the second sequence has no corresponding object in the
 * first sequence at the right place, the {@link EditScript edit script}
 * transforming the first sequence into the second sequence uses an instance of
 * this class to represent the insertion of this object. The objects embedded in
 * these type of commands always come from the second sequence.
 * </p>
 *
 * @see StringsComparator
 * @see EditScript
 *
 * @param <T> object type
 * @since 1.0
 */
public class InsertCommand<T> extends EditCommand<T> {

    /**
     * Simple constructor. Creates a new instance of InsertCommand
     *
     * @param object  the object of the second sequence that should be inserted
     */
    public InsertCommand(final T object) {
        super(object);
    }

    /**
     * Accept a visitor. When an {@code InsertCommand} accepts a visitor,
     * it calls its {@link CommandVisitor#visitInsertCommand visitInsertCommand}
     * method.
     *
     * @param visitor  the visitor to be accepted
     */
    @Override
    public void accept(final CommandVisitor<T> visitor) {
        visitor.visitInsertCommand(getObject());
    }

}
