package ee.bitweb.core.retrofit.logging.mappers;

import lombok.RequiredArgsConstructor;
import okhttp3.Connection;
import okhttp3.Request;
import okhttp3.Response;

@RequiredArgsConstructor
public class RetrofitRequestProtocolMapper implements RetrofitLoggingMapper {

    public static final String KEY = "RequestProtocol";

    @Override
    public String getValue(Connection connection, Request request, Response response) {
        return connection != null ? connection.protocol().toString() : "-";
    }

    @Override
    public String getKey() {
        return KEY;
    }
}
