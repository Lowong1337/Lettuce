package best.lettuce.utils.text.diff;

import java.util.List;

/**
 * This interface is devoted to handle synchronized replacement sequences.
 *
 * @param <T> object type
 * @see ReplacementsFinder
 * @since 1.0
 */
public interface ReplacementsHandler<T> {

    /**
     * Handle two synchronized sequences.
     * <p>
     * This method is called by a {@link ReplacementsFinder ReplacementsFinder}
     * instance when it has synchronized two sub-sequences of object arrays
     * being compared, and at least one of the sequences is non-empty. Since the
     * sequences are synchronized, the objects before the two sub-sequences are
     * equals (if they exist). This property also holds for the objects after
     * the two sub-sequences.
     * <p>
     * The replacement is defined as replacing the {@code from}
     * sub-sequence into the {@code to} sub-sequence.
     *
     * @param skipped  number of tokens skipped since the last call (i.e. number of
     *   tokens that were in both sequences), this number should be strictly positive
     *   except on the very first call where it can be zero (if the first object of
     *   the two sequences are different)
     * @param from  sub-sequence of objects coming from the first sequence
     * @param to  sub-sequence of objects coming from the second sequence
     */
    void handleReplacement(int skipped, List<T> from, List<T> to);

}
