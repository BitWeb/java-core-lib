package ee.bitweb.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtil {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String random(int length) {
        StringBuilder sb = new StringBuilder();

        while (sb.length() < length) {
            int index = RANDOM.nextInt(CHARS.length());
            sb.append(CHARS.charAt(index));
        }

        return sb.toString();
    }
}
