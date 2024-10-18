package ee.bitweb.core.retrofit.logging.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import okhttp3.Headers;
import okio.Buffer;

import java.io.IOException;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RetrofitBodyMapperHelper {

    public static boolean isProbablyUtf8(Buffer buffer) throws IOException {
        var prefix = new Buffer();
        var byteCount = buffer.size() > 64 ? 64 : buffer.size();

        buffer.copyTo(prefix, 0, byteCount);

        for (int i = 0; i < 16; i++) {
            if (prefix.exhausted()) {
                break;
            }

            var codePoint = prefix.readUtf8CodePoint();

            if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                return false;
            }
        }

        return true;
    }

    public static boolean bodyHasUnknownEncoding(Headers headers) {
        var contentEncoding = headers.get("Content-Encoding");

        if (contentEncoding == null) {
            return false;
        }

        return !contentEncoding.equalsIgnoreCase("identity") &&
                !contentEncoding.equalsIgnoreCase("gzip");
    }

    public static boolean isRedactBodyUrl(Set<String> redactBodyUrls, String url) {
        return redactBodyUrls.contains(url);
    }
}
