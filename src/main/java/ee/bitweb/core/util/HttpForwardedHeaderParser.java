package ee.bitweb.core.util;

import ee.bitweb.core.exception.InvalidArgumentException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpForwardedHeaderParser {

    public static ForwardedHeader parse(Enumeration<String> headers) {
        List<HttpForwardedHeaderParser.ForwardedHeader> results = new ArrayList<>();

        while (headers.hasMoreElements()) {
            results.add(parse(headers.nextElement()));
        }

        return merge(results);
    }

    public static ForwardedHeader parse(String header) {
        if (header == null || header.isBlank()) {
            throw new InvalidArgumentException("header is required");
        }

        String[] elements = header.split(",");

        List<String> by = new ArrayList<>();
        List<String> aFor = new ArrayList<>();
        List<String> host = new ArrayList<>();
        List<String> proto = new ArrayList<>();
        List<ForwardedExtension> extensions = new ArrayList<>();

        for (String element : elements) {
            String[] parameters = element.trim().split(";");

            for (String parameter : parameters) {
                String[] pair = parameter.split("=");
                if (pair.length != 2) {
                    log.debug("'{}' is not recognisable", parameter);
                    continue;
                }

                String key = pair[0].toLowerCase();
                String value = pair[1].replaceAll("^\"|\"$", "");

                switch (key) {
                    case "by":
                        by.add(value);
                        break;
                    case "for":
                        aFor.add(value);
                        break;
                    case "host":
                        host.add(value);
                        break;
                    case "proto":
                        proto.add(value);
                        break;
                    default:
                        extensions.add(new ForwardedExtension(key, value));
                        break;
                }
            }
        }

        var result = new ForwardedHeader();
        result.setBy(by);
        result.setFor(aFor);
        result.setHost(host);
        result.setProto(proto);
        result.setExtensions(extensions);

        return result;
    }

    public static ForwardedHeader merge(List<ForwardedHeader> headers) {
        Set<String> by = new LinkedHashSet<>();
        Set<String> aFor = new LinkedHashSet<>();
        Set<String> host = new LinkedHashSet<>();
        Set<String> proto = new LinkedHashSet<>();
        Set<ForwardedExtension> extensions = new LinkedHashSet<>();

        for (ForwardedHeader header : headers) {
            by.addAll(header.getBy());
            aFor.addAll(header.getAFor());
            host.addAll(header.getHost());
            proto.addAll(header.getProto());
            extensions.addAll(header.getExtensions());
        }

        var result = new ForwardedHeader();
        result.setBy(List.copyOf(by));
        result.setFor(List.copyOf(aFor));
        result.setHost(List.copyOf(host));
        result.setProto(List.copyOf(proto));
        result.setExtensions(List.copyOf(extensions));

        return result;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ForwardedHeader {

        private List<String> by;
        private List<String> aFor;
        private List<String> host;
        private List<String> proto;
        private List<ForwardedExtension> extensions;

        public void setBy(List<String> by) {
            this.by = Collections.unmodifiableList(by);
        }

        public void setFor(List<String> aFor) {
            this.aFor = Collections.unmodifiableList(aFor);
        }

        public void setHost(List<String> host) {
            this.host = Collections.unmodifiableList(host);
        }

        public void setProto(List<String> proto) {
            this.proto = Collections.unmodifiableList(proto);
        }

        public void setExtensions(List<ForwardedExtension> extensions) {
            this.extensions = Collections.unmodifiableList(extensions);
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ForwardedExtension {
        private final String key;
        private final String value;

        @Override
        public String toString() {
            return key +  "=" + value;
        }
    }
}
