package uni.fmi.management;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uni.fmi.management.http.APIUtils;
import uni.fmi.management.http.ErrorUtils;
import uni.fmi.management.http.requests.LoginRequest;
import uni.fmi.management.http.responses.APIError;
import uni.fmi.management.http.responses.UserDetails;
import uni.fmi.management.http.services.AuthService;

public class LoginActivity extends AppCompatActivity {
    EditText usernameET;
    EditText passwordET;
    Button loginB;
    AuthService authService;
    TextView registerB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = APIUtils.getAuthService(getApplicationContext());

        usernameET = findViewById(R.id.login_page_username);
        passwordET = findViewById(R.id.login_page_password);

        loginB = findViewById(R.id.login_page_login);
        loginB.setOnClickListener(onLoginClick);

        registerB = (TextView) findViewById(R.id.login_page_register);
        registerB.setOnClickListener(onClickRegister);
    }

    View.OnClickListener onLoginClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String username = usernameET.getText().toString();
            String password = passwordET.getText().toString();

            // Validations
            if(username.length() == 0){
                Toast.makeText(getApplicationContext(), "Username is required", Toast.LENGTH_LONG).show();
                return;
            }

            if(password.length() == 0){
                Toast.makeText(getApplicationContext(), "Password is required", Toast.LENGTH_LONG).show();
                return;
            }

            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername(username);
            loginRequest.setPassword(password);
            login(loginRequest);
        }
    };

    private void login(LoginRequest loginRequest){
        Call<UserDetails> call = authService.login(loginRequest);
        call.enqueue(new Callback<UserDetails>() {
            @Override
            public void onResponse(Call<UserDetails> call, Response<UserDetails> response) {
                if(response.isSuccessful()){
                    SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();

                    UserDetails userDetails = response.body();
                    String password = passwordET.getText().toString();
                    editor.putInt("id", userDetails.getId());
                    editor.putString("username", userDetails.getUsername());
                    editor.putString("password", password);
                    editor.putString("name", userDetails.getName());
                    editor.commit();

                    Toast.makeText(LoginActivity.this, "Login successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, ProjectsActivity.class);
                    startActivity(intent);
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserDetails> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private View.OnClickListener onClickRegister = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        }
    };
}