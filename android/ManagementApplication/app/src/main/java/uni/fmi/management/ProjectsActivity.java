package uni.fmi.management;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uni.fmi.management.http.APIUtils;
import uni.fmi.management.http.ErrorUtils;
import uni.fmi.management.http.requests.CreateProjectRequest;
import uni.fmi.management.http.requests.EditProfileRequest;
import uni.fmi.management.http.responses.APIError;
import uni.fmi.management.http.responses.MessageResponse;
import uni.fmi.management.http.responses.UserDetails;
import uni.fmi.management.http.services.AuthService;

public class ProjectsActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    BottomNavigationView bottomNavigationView;
    AuthService authService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        authService = APIUtils.getAuthService(getApplicationContext());

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, projectsFragment).commit();
    }

    ProjectsFragment projectsFragment = new ProjectsFragment();
    TasksFragment tasksFragment = new TasksFragment();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.projects_page: {
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, projectsFragment).commit();
                return true;
            }
            case R.id.tasks_page: {
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, tasksFragment).commit();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_app_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle presses on the action bar items by id
        switch (item.getItemId()) {
            case R.id.main_app_bar_menu_edit_profile:
                showEditProfileDialog();
                return true;
            case R.id.main_app_bar_menu_logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void logout(){
        Call<MessageResponse> call = authService.logout();
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if(response.isSuccessful()){
                    SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.commit();
                    Toast.makeText(ProjectsActivity.this, "Logout successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProjectsActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(ProjectsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ProjectsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditProfileDialog()
    {
        // Create a AlertDialog Builder.
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // Set title, icon, can not cancel properties.
        alertDialogBuilder.setTitle("Edit profile");
        alertDialogBuilder.setCancelable(false);
        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        final View editProfileView = layoutInflater.inflate(R.layout.edit_profile_dialog, null);
        alertDialogBuilder.setView(editProfileView);
        final AlertDialog addProjectDialog = alertDialogBuilder.create();
        addProjectDialog.show();

        EditText nameET = editProfileView.findViewById(R.id.name_input);
        SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        String nameValue = preferences.getString("name", "");

        nameET.setText(nameValue);

        Button saveB = editProfileView.findViewById(R.id.save_button);
        Button cancelB = editProfileView.findViewById(R.id.cancel_button);

        cancelB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProjectDialog.hide();
            }
        });

        saveB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText nameET = editProfileView.findViewById(R.id.name_input);
                String name = nameET.getText().toString();

                if(name.length() == 0){
                    Toast.makeText(getApplicationContext(), "Name is required", Toast.LENGTH_LONG).show();
                    return;
                }

                EditProfileRequest editProfileRequest = new EditProfileRequest();
                editProfileRequest.setName(name);
                editProfile(editProfileRequest);
                addProjectDialog.hide();
            }
        });
    }

    private void editProfile(EditProfileRequest editProfileRequest){
        Call<MessageResponse> call = authService.editProfile(editProfileRequest);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if(response.isSuccessful()){
                    MessageResponse msgResponse = response.body();
                    Toast.makeText(ProjectsActivity.this, msgResponse.getMessage(), Toast.LENGTH_SHORT).show();

                    // Update the name in preferences
                    SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();

                    editor.putString("name", editProfileRequest.getName());
                    editor.commit();
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(ProjectsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(ProjectsActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ProjectsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}