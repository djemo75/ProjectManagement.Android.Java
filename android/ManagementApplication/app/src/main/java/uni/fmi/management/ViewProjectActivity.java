package uni.fmi.management;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uni.fmi.management.http.APIUtils;
import uni.fmi.management.http.ErrorUtils;
import uni.fmi.management.http.requests.EditProjectRequest;
import uni.fmi.management.http.responses.APIError;
import uni.fmi.management.http.responses.MessageResponse;
import uni.fmi.management.http.responses.ProjectEntity;
import uni.fmi.management.http.services.ProjectService;

public class ViewProjectActivity extends AppCompatActivity {
    TextView title;
    TextView content;
    TextView date;
    TextView time;
    TextView username;
    Button deleteB;
    Button editB;
    Button viewParticipants;
    ProjectService projectService;
    ProjectEntity project;
    int projectId;
    int userId;
    boolean isProjectOwner = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_project);

        title = findViewById(R.id.view_project_title);
        content = findViewById(R.id.view_project_content);
        date = findViewById(R.id.view_project_date);
        time = findViewById(R.id.view_project_time);
        username = findViewById(R.id.view_project_username);
        deleteB = findViewById(R.id.view_project_delete);
        deleteB.setOnClickListener(onClickDelete);
        editB = findViewById(R.id.view_project_edit);
        editB.setOnClickListener(onClickEdit);
        viewParticipants = findViewById(R.id.view_project_participants);

        projectService = APIUtils.getProjectService(getApplicationContext());

        Intent intent = getIntent();
        projectId = intent.getIntExtra("projectId", -1); // -1 is equal for not found

        SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        userId = preferences.getInt("id", -1);

        new NewsAsyncTask().execute();
    }

    private class NewsAsyncTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog = new ProgressDialog(ViewProjectActivity.this);

        public NewsAsyncTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setTitle("Loading the project...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getProject(projectId);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            dialog.hide();
        }
    }

    private void getProject(int projectId){
        Call<ProjectEntity> call = projectService.getProjectById(projectId);
        call.enqueue(new Callback<ProjectEntity>() {
            @Override
            public void onResponse(Call<ProjectEntity> call, Response<ProjectEntity> response) {
                if(response.isSuccessful()){
                    project = response.body();
                    String titleValue = project.getTitle().toString();
                    String contentValue = project.getContent().toString();
                    String dateValue = new SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(project.getCreatedDate());
                    String timeValue = new SimpleDateFormat("HH:mm").format(project.getCreatedDate());
                    String usernameValue = project.getAuthor().getUsername().toString()
                            +" ("+project.getAuthor().getName().toString()+")";
                    isProjectOwner = project.getAuthor().getId()==userId;
                    if(isProjectOwner) {
                        usernameValue += " - Project Owner";
                    } else {
                        usernameValue += " - Participant";
                    }
                    title.setText(titleValue);
                    content.setText(contentValue);
                    date.setText(dateValue);
                    time.setText(timeValue);
                    username.setText(usernameValue);

                    if(!isProjectOwner) {
                        deleteB.setVisibility(View.GONE);
                        editB.setVisibility(View.GONE);
                        viewParticipants.setVisibility(View.GONE);
                    }

                    viewParticipants.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(ViewProjectActivity.this, ViewParticipantsActivity.class);
                            intent.putExtra("projectId", getIntent().getIntExtra("projectId", -1));
                            startActivity(intent);
                        }
                    });

                    invalidateOptionsMenu(); // onCreateOptionsMenu(...) called again to hide the menu
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(ViewProjectActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(ViewProjectActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<ProjectEntity> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ViewProjectActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private View.OnClickListener onClickDelete = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            deleteProject(projectId);
        }
    };

    private View.OnClickListener onClickEdit = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showEditDialog();
        }
    };

    private void deleteProject(int id){
        Call<MessageResponse> call = projectService.deleteProject(id);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if(response.isSuccessful()){
                    MessageResponse msgResponse = response.body();
                    Toast.makeText(ViewProjectActivity.this, msgResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ViewProjectActivity.this, ProjectsActivity.class);
                    startActivity(intent);
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(ViewProjectActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(ViewProjectActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ViewProjectActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog()
    {
        // Create a AlertDialog Builder.
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ViewProjectActivity.this);
        // Set title, icon, can not cancel properties.
        alertDialogBuilder.setTitle("Edit project");
        alertDialogBuilder.setCancelable(false);
        LayoutInflater layoutInflater = LayoutInflater.from(ViewProjectActivity.this);
        final View editProjectView = layoutInflater.inflate(R.layout.add_project_dialog, null);
        alertDialogBuilder.setView(editProjectView);
        final AlertDialog editNewsDialog = alertDialogBuilder.create();
        editNewsDialog.show();

        Button editNewsB = editProjectView.findViewById(R.id.create_button);
        Button cancelNewsB = editProjectView.findViewById(R.id.cancel_button);
        EditText titleET = editProjectView.findViewById(R.id.title_input);
        EditText contentET = editProjectView.findViewById(R.id.content_input);

        titleET.setText(project.getTitle());
        contentET.setText(project.getContent());

        cancelNewsB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editNewsDialog.hide();
            }
        });

        editNewsB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleET.getText().toString();
                String content = contentET.getText().toString();

                if(title.length() == 0){
                    Toast.makeText(getApplicationContext(), "Title is required", Toast.LENGTH_LONG).show();
                    return;
                }

                if(content.length() == 0){
                    Toast.makeText(getApplicationContext(), "Content is required", Toast.LENGTH_LONG).show();
                    return;
                }

                EditProjectRequest editProjectRequest = new EditProjectRequest();
                editProjectRequest.setTitle(title);
                editProjectRequest.setContent(content);
                editProject(projectId, editProjectRequest);
                editNewsDialog.hide();
            }
        });
    }

    private void editProject(int id, EditProjectRequest editProjectRequest){
        Call<MessageResponse> call = projectService.editProject(id, editProjectRequest);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if(response.isSuccessful()){
                    MessageResponse msgResponse = response.body();
                    Toast.makeText(ViewProjectActivity.this, msgResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    getProject(projectId);
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(ViewProjectActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(ViewProjectActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ViewProjectActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}