package best.lettuce.utils.text;

import java.text.Format;
import java.util.Locale;

/**
 * Format factory.
 *
 * @since 1.0
 */
public interface FormatFactory {

    /**
     * Create or retrieve a format instance.
     *
     * @param name The format type name
     * @param arguments Arguments used to create the format instance. This allows the
     *                  {@code FormatFactory} to implement the "format style"
     *                  concept from {@code java.text.MessageFormat}.
     * @param locale The locale, may be null
     * @return The format instance
     */
    Format getFormat(String name, String arguments, Locale locale);

}
