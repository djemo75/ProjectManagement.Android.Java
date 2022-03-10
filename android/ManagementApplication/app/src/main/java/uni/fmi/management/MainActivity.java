package uni.fmi.management;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uni.fmi.management.http.APIUtils;
import uni.fmi.management.http.requests.LoginRequest;
import uni.fmi.management.http.responses.UserDetails;
import uni.fmi.management.http.services.AuthService;

public class MainActivity extends AppCompatActivity {
    AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authService = APIUtils.getAuthService(getApplicationContext());

        SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        String username = preferences.getString("username", "");
        String password = preferences.getString("password", "");
        if(username != "" && password != "") {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername(preferences.getString("username", ""));
            loginRequest.setPassword(preferences.getString("password", ""));
            loginAutomatically(loginRequest);
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void loginAutomatically(LoginRequest loginRequest){
        Call<UserDetails> call = authService.login(loginRequest);
        call.enqueue(new Callback<UserDetails>() {
            @Override
            public void onResponse(Call<UserDetails> call, Response<UserDetails> response) {
                if(response.isSuccessful()){
                    Intent intent = new Intent(MainActivity.this, ProjectsActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<UserDetails> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}