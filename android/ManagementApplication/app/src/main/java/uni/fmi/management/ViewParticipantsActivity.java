package uni.fmi.management;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uni.fmi.management.adapters.ParticipantsAdapter;
import uni.fmi.management.adapters.ProjectsAdapter;
import uni.fmi.management.http.APIUtils;
import uni.fmi.management.http.ErrorUtils;
import uni.fmi.management.http.requests.CreateProjectRequest;
import uni.fmi.management.http.requests.EditProjectRequest;
import uni.fmi.management.http.responses.APIError;
import uni.fmi.management.http.responses.MessageResponse;
import uni.fmi.management.http.responses.ProjectEntity;
import uni.fmi.management.http.responses.ProjectParticipantEntity;
import uni.fmi.management.http.responses.UserDetails;
import uni.fmi.management.http.services.ProjectService;

public class ViewParticipantsActivity extends AppCompatActivity {
    Button addB;
    Spinner usersDropdown;
    ProjectService projectService;
    ArrayList<UserDetails> users = new ArrayList<>();
    ArrayList<UserDetails> participants = new ArrayList<>();
    RecyclerView participantRV;
    RecyclerView.Adapter participantRVAdapter;
    int projectId;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_participants);

        projectService = APIUtils.getProjectService(getApplicationContext());

        projectId = getIntent().getIntExtra("projectId", -1); // -1 is equal for not found

        SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        userId = preferences.getInt("id", -1);

        participantRV = (RecyclerView) findViewById(R.id.participantsRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(ViewParticipantsActivity.this);
        participantRV.setLayoutManager(mLayoutManager);

        getAllUsers();
        getParticipants(projectId);

        addB = findViewById(R.id.view_participants_add);
        addB.setOnClickListener(onClickAdd);
    }

    private void getParticipants(int projectId){
        Call<ArrayList<UserDetails>> call = projectService.getProjectUsersByProjectId(projectId);
        call.enqueue(new Callback<ArrayList<UserDetails>>() {
            @Override
            public void onResponse(Call<ArrayList<UserDetails>> call, Response<ArrayList<UserDetails>> response) {
                if(response.isSuccessful()){
                    participants = response.body();

                    participantRVAdapter = new ParticipantsAdapter(participants, projectId);
                    participantRV.setAdapter(participantRVAdapter);
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(ViewParticipantsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(ViewParticipantsActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<UserDetails>> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ViewParticipantsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAllUsers(){
        Call<ArrayList<UserDetails>> call = projectService.getAllUsers();
        call.enqueue(new Callback<ArrayList<UserDetails>>() {
            @Override
            public void onResponse(Call<ArrayList<UserDetails>> call, Response<ArrayList<UserDetails>> response) {
                if(response.isSuccessful()){
                    users = response.body();
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(ViewParticipantsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(ViewParticipantsActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<UserDetails>> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ViewParticipantsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private View.OnClickListener onClickAdd = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showAddDialog();
        }
    };

    private void showAddDialog()
    {
        // Create a AlertDialog Builder.
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ViewParticipantsActivity.this);
        // Set title, icon, can not cancel properties.
        alertDialogBuilder.setTitle("Add participant to project");
        alertDialogBuilder.setCancelable(false);
        LayoutInflater layoutInflater = LayoutInflater.from(ViewParticipantsActivity.this);
        final View addParticipantView = layoutInflater.inflate(R.layout.add_participant_dialog, null);
        alertDialogBuilder.setView(addParticipantView);
        final AlertDialog addParticipantDialog = alertDialogBuilder.create();
        addParticipantDialog.show();

        Button addParticipantB = addParticipantView.findViewById(R.id.create_button);
        Button cancelParticipantB = addParticipantView.findViewById(R.id.cancel_button);

        ArrayList<String> options = new ArrayList<>();
        // TO-DO Show only users who are not part of the project
        ArrayList<UserDetails> availableForAdding = new ArrayList<>();
        System.out.println(participants.size());
        System.out.println(users.size());
        for (int i = 0; i < users.size(); i++)
        {
            UserDetails currentUser = users.get(i);
            for (UserDetails participant : participants) {
                if(participant.getId() != currentUser.getId()
                        && !availableForAdding.contains(currentUser)){
                    availableForAdding.add(currentUser);
                }
            }
        }

        availableForAdding.forEach(user -> {
                options.add(user.getUsername());
        });
        ArrayAdapter<String>adapter = new ArrayAdapter<String>(ViewParticipantsActivity.this,
                android.R.layout.simple_spinner_item,options);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        usersDropdown = (Spinner)addParticipantView.findViewById(R.id.add_participant_dialog_user_dropdown);
        usersDropdown.setAdapter(adapter);

        cancelParticipantB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addParticipantDialog.hide();
            }
        });

        addParticipantB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedUsername = usersDropdown.getSelectedItem().toString();

                int selectedUserId = -1;
                for (UserDetails currentUser : users) {
                    if (currentUser.getUsername() == selectedUsername) {
                        selectedUserId = currentUser.getId();
                    }
                }
                if(selectedUserId != -1) {
                    addParticipant(selectedUserId, projectId);
                    addParticipantDialog.hide();
                }
            }
        });
    }

    private void addParticipant(int userId, int projectId){
        Call<ProjectParticipantEntity> call = projectService.addParticipantToProject(userId, projectId);
        call.enqueue(new Callback<ProjectParticipantEntity>() {
            @Override
            public void onResponse(Call<ProjectParticipantEntity> call, Response<ProjectParticipantEntity> response) {
                if(response.isSuccessful()){
                    Toast.makeText(ViewParticipantsActivity.this, "The participant was added successfully!", Toast.LENGTH_SHORT).show();
                    getParticipants(projectId);
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(ViewParticipantsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(ViewParticipantsActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<ProjectParticipantEntity> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ViewParticipantsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteProject(int id){
        Call<MessageResponse> call = projectService.deleteProject(id);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if(response.isSuccessful()){
                    MessageResponse msgResponse = response.body();
                    Toast.makeText(ViewParticipantsActivity.this, msgResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    getParticipants(projectId);
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(ViewParticipantsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(ViewParticipantsActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ViewParticipantsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}