package uni.fmi.management.http;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

import retrofit2.Response;
import uni.fmi.management.http.responses.APIError;

public class ErrorUtils {

    public static APIError parseError(Response<?> response) {
        APIError error;

        try {
            Moshi moshi = new Moshi.Builder().build();
            JsonAdapter<APIError> jsonAdapter = moshi.adapter(APIError.class);

            String json = response.errorBody().string();
            error = jsonAdapter.fromJson(json);
        } catch (IOException e) {
            return new APIError();
        }

        return error;
    }
}