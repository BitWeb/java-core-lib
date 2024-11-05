package ee.bitweb.core.retrofit.builder;

import ee.bitweb.core.retrofit.logging.mappers.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum LoggingLevel {

    NONE(List.of()),
    BASIC(basicMappers()),
    HEADERS(headerMappers()),
    BODY(bodyMappers()),
    CUSTOM(List.of());

    private final List<String> mappers;

    private static List<String> basicMappers() {
        return new ArrayList<>(List.of(
                RetrofitRequestMethodMapper.KEY,
                RetrofitRequestUrlMapper.KEY,
                RetrofitRequestBodySizeMapper.KEY,
                RetrofitResponseStatusCodeMapper.KEY,
                RetrofitResponseBodySizeMapper.KEY
        ));
    }

    private static List<String> headerMappers() {
        List<String> mappers = basicMappers();
        mappers.addAll(List.of(
                RetrofitRequestHeadersMapper.KEY,
                RetrofitResponseHeadersMapper.KEY
        ));

        return mappers;
    }

    private static List<String> bodyMappers() {
        List<String> mappers = headerMappers();
        mappers.addAll(List.of(
                RetrofitRequestBodyMapper.KEY,
                RetrofitResponseBodyMapper.KEY
        ));

        return mappers;
    }
}
