package best.lettuce.utils.text;

/**
 * <p>
 * Commonly used implementations of {@link CharacterPredicate}. Per the interface
 * requirements, all implementations are thread safe.
 * </p>
 *
 * @since 1.0
 */
public enum CharacterPredicates implements CharacterPredicate {

    /**
     * Tests code points against {@link Character#isLetter(int)}.
     *
     * @since 1.0
     */
    LETTERS {
        @Override
        public boolean test(final int codePoint) {
            return Character.isLetter(codePoint);
        }
    },

    /**
     * Tests code points against {@link Character#isDigit(int)}.
     *
     * @since 1.0
     */
    DIGITS {
        @Override
        public boolean test(final int codePoint) {
            return Character.isDigit(codePoint);
        }
    },

    /**
     * Tests if the code points represents a number between 0 and 9.
     *
     * @since 1.2
     */
    ARABIC_NUMERALS {
        @Override
        public boolean test(final int codePoint) {
            return codePoint >= '0' && codePoint <= '9';
        }
    },

    /**
     * Tests if the code points represents a letter between a and z.
     *
     * @since 1.2
     */
    ASCII_LOWERCASE_LETTERS {
        @Override
        public boolean test(final int codePoint) {
            return codePoint >= 'a' && codePoint <= 'z';
        }
    },

    /**
     * Tests if the code points represents a letter between A and Z.
     *
     * @since 1.2
     */
    ASCII_UPPERCASE_LETTERS {
        @Override
        public boolean test(final int codePoint) {
            return codePoint >= 'A' && codePoint <= 'Z';
        }
    },

    /**
     * Tests if the code points represents a letter between a and Z.
     *
     * @since 1.2
     */
    ASCII_LETTERS {
        @Override
        public boolean test(final int codePoint) {
            return ASCII_LOWERCASE_LETTERS.test(codePoint) || ASCII_UPPERCASE_LETTERS.test(codePoint);
        }
    },

    /**
     * Tests if the code points represents a letter between a and Z or a number between 0 and 9.
     *
     * @since 1.2
     */
    ASCII_ALPHA_NUMERALS {
        @Override
        public boolean test(final int codePoint) {
            return ASCII_LOWERCASE_LETTERS.test(codePoint) || ASCII_UPPERCASE_LETTERS.test(codePoint)
                    || ARABIC_NUMERALS.test(codePoint);
        }
    }
}
