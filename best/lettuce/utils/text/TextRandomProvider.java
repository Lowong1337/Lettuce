package best.lettuce.utils.text;

/**
 * <p>
 * TextRandomProvider implementations are used by {@link RandomStringGenerator}
 * as a source of randomness.  It is highly recommended that the
 * <a href="https://commons.apache.org/proper/commons-rng/">Apache Commons RNG</a>
 * library be used to provide the random number generation.
 * </p>
 *
 * <p>
 * When using Java 8 or later, TextRandomProvider is a functional interface and
 * need not be explicitly implemented.  For example:
 * </p>
 * <pre>
 * {@code
 * UniformRandomProvider rng = RandomSource.create(...);
 * RandomStringGenerator gen = new RandomStringGenerator.Builder()
 *     .usingRandom(rng::nextInt)
 *     // additional builder calls as needed
 *     .build();
 * }
 * </pre>
 * @since 1.1
 */
public interface TextRandomProvider {

    /**
     * Generates an int value between 0 (inclusive) and the specified value
     * (exclusive).
     * @param max  Bound on the random number to be returned. Must be positive.
     * @return a random int value between 0 (inclusive) and n (exclusive).
     */
    int nextInt(int max);
}
