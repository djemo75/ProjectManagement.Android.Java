package uni.fmi.management.http.services;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import uni.fmi.management.http.requests.EditProfileRequest;
import uni.fmi.management.http.requests.LoginRequest;
import uni.fmi.management.http.requests.RegisterRequest;
import uni.fmi.management.http.responses.MessageResponse;
import uni.fmi.management.http.responses.UserDetails;

public interface AuthService {

    @GET("auth/profile")
    Call<UserDetails> getProfile();

    @POST("auth/login")
    Call<UserDetails> login(@Body LoginRequest loginRequest);

    @POST("auth/register")
    Call<MessageResponse> register(@Body RegisterRequest registerRequest);

    @PUT("auth/edit-profile")
    Call<MessageResponse> editProfile(@Body EditProfileRequest editProfileRequest);

    @POST("auth/logout")
    Call<MessageResponse> logout();
}