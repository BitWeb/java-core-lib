package ee.bitweb.core.request_id;

import java.util.List;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestIdUtil {

    private static final String USER_AGENT_MISSING = "USER_AGENT_MISSING";
    private static final List<String> IGNORED_USER_AGENTS = List.of(
            "Prometheus/",
            "ELB-HealthChecker/2.0",
            "ReactorNetty/0.9.6.RELEASE" // Spring Boot Admin
    );
    public static final String FORWARDED_HEADER = "Forwarded";
    public static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";
    public static final Pattern FORWARDED_PATTERN = Pattern.compile(
            "by=([\\.\\d]{7,15});for=([\\.\\d]{7,15});host=(.*);proto=[a-zA-Z]{4,5}"
    );

    public static String getUserAgent(HttpServletRequest httpServletRequest) {
        String header = httpServletRequest.getHeader(HttpHeaders.USER_AGENT);

        return header != null ? header : USER_AGENT_MISSING;
    }

    public static boolean isNotIgnored(String userAgent) {
        return !isIgnored(userAgent);
    }

    public static boolean isIgnored(String userAgent) {
        for (String ignoredUserAgent : RequestIdUtil.IGNORED_USER_AGENTS) {
            if (userAgent.startsWith(ignoredUserAgent)) {
                return true;
            }
        }

        return false;
    }
}
