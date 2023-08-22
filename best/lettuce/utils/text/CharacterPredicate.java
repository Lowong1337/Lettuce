package best.lettuce.utils.text;

/**
 * A predicate for selecting code points. Implementations of this interface must
 * be thread safe.
 *
 * @since 1.0
 */
public interface CharacterPredicate {

    /**
     * Tests the code point with this predicate.
     *
     * @param codePoint
     *            the code point to test
     * @return {@code true} if the code point matches the predicate,
     *         {@code false} otherwise
     * @since 1.0
     */
    boolean test(int codePoint);
}
