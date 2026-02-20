package ee.bitweb.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtil {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Trims whitespace from the given string value.
     *
     * @param value the string to trim, may be null
     * @return trimmed string, or null if input was null
     */
    public static String trim(String value) {
        return value != null ? value.trim() : null;
    }

    public static String random(int length) {
        var sb = new StringBuilder();

        while (sb.length() < length) {
            var index = RANDOM.nextInt(CHARS.length());
            sb.append(CHARS.charAt(index));
        }

        return sb.toString();
    }
}
