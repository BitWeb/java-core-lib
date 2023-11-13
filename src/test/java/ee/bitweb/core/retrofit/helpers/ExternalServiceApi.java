package ee.bitweb.core.retrofit.helpers;

import ee.bitweb.core.retrofit.Response;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ExternalServiceApi {

    @GET("/request")
    Call<Payload> get();

    @GET("/data-request")
    Call<Response<Payload>> getWrappedInResponse();

    @Setter
    @Getter
    @NoArgsConstructor
    class Payload {
        private String message;
        private Integer value;
    }
}
