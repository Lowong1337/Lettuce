package best.lettuce.utils.text.diff;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles sequences of replacements resulting from a comparison.
 * <p>
 * The comparison of two objects sequences leads to the identification of common
 * parts and parts which only belong to the first or to the second sequence. The
 * common parts appear in the edit script in the form of <em>keep</em> commands,
 * they can be considered as synchronization objects between the two sequences.
 * These synchronization objects split the two sequences in synchronized
 * sub-sequences. The first sequence can be transformed into the second one by
 * replacing each synchronized sub-sequence of the first sequence by the
 * corresponding sub-sequence of the second sequence. This is a synthetic way to
 * see an {@link EditScript edit script}, replacing individual
 * {@link DeleteCommand delete}, {@link KeepCommand keep} and
 * {@link InsertCommand insert} commands by fewer replacements acting on
 * complete sub-sequences.
 * </p>
 * <p>
 * This class is devoted to perform this interpretation. It visits an
 * {@link EditScript edit script} (because it implements the
 * {@link CommandVisitor CommandVisitor} interface) and calls a user-supplied
 * handler implementing the {@link ReplacementsHandler ReplacementsHandler}
 * interface to process the sub-sequences.
 * </p>
 *
 * @see ReplacementsHandler
 * @see EditScript
 * @see StringsComparator
 *
 * @param <T> object type
 * @since 1.0
 */
public class ReplacementsFinder<T> implements CommandVisitor<T> {

    /**
     * List of pending insertions.
     */
    private final List<T> pendingInsertions;

    /**
     * List of pending deletions.
     */
    private final List<T> pendingDeletions;

    /**
     * Count of elements skipped.
     */
    private int skipped;

    /** Handler to call when synchronized sequences are found. */
    private final ReplacementsHandler<T> handler;

    /**
     * Simple constructor. Creates a new instance of {@link ReplacementsFinder}.
     *
     * @param handler  handler to call when synchronized sequences are found
     */
    public ReplacementsFinder(final ReplacementsHandler<T> handler) {
        pendingInsertions = new ArrayList<>();
        pendingDeletions  = new ArrayList<>();
        skipped           = 0;
        this.handler      = handler;
    }

    /**
     * Add an object to the pending insertions set.
     *
     * @param object  object to insert
     */
    @Override
    public void visitInsertCommand(final T object) {
        pendingInsertions.add(object);
    }

    /**
     * Handle a synchronization object.
     * <p>
     * When a synchronization object is identified, the pending insertions and
     * pending deletions sets are provided to the user handler as subsequences.
     * </p>
     *
     * @param object  synchronization object detected
     */
    @Override
    public void visitKeepCommand(final T object) {
        if (pendingDeletions.isEmpty() && pendingInsertions.isEmpty()) {
            ++skipped;
        } else {
            handler.handleReplacement(skipped, pendingDeletions, pendingInsertions);
            pendingDeletions.clear();
            pendingInsertions.clear();
            skipped = 1;
        }
    }

    /**
     * Add an object to the pending deletions set.
     *
     * @param object  object to delete
     */
    @Override
    public void visitDeleteCommand(final T object) {
        pendingDeletions.add(object);
    }

}
