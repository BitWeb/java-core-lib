package ee.bitweb.core.retrofit.helpers;

import ee.bitweb.core.retrofit.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ExternalServiceApi {

    @GET("/request")
    Call<Payload> get();

    @GET("/data-request")
    Call<Response<Payload>> getWrappedInResponse();

    @POST("/data-post")
    Call<Payload> postData(@Body Payload payload);

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    class Payload {
        private String message;
        private Integer value;
    }
}
