package uni.fmi.management;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uni.fmi.management.adapters.ProjectTasksAdapter;
import uni.fmi.management.http.APIUtils;
import uni.fmi.management.http.ErrorUtils;
import uni.fmi.management.http.requests.CreateTaskRequest;
import uni.fmi.management.http.requests.EditProjectRequest;
import uni.fmi.management.http.responses.APIError;
import uni.fmi.management.http.responses.MessageResponse;
import uni.fmi.management.http.responses.ProjectEntity;
import uni.fmi.management.http.responses.TaskEntity;
import uni.fmi.management.http.responses.UserDetails;
import uni.fmi.management.http.services.ProjectService;
import uni.fmi.management.http.services.TaskService;

public class ViewProjectActivity extends AppCompatActivity {
    TextView title;
    TextView content;
    TextView date;
    TextView time;
    TextView username;
    Button deleteB;
    Button editB;
    Button viewParticipants;
    Button viewFiles;
    Button addTaskB;
    ProjectService projectService;
    TaskService taskService;
    ProjectEntity project;
    ArrayList<TaskEntity> tasks = new ArrayList<>();
    RecyclerView taskRV;
    RecyclerView.Adapter taskRVAdapter;
    ArrayList<UserDetails> users = new ArrayList<>();
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
        viewFiles = findViewById(R.id.view_project_files);
        addTaskB = findViewById(R.id.view_project_add_task);
        addTaskB.setOnClickListener(onClickAddTask);

        projectService = APIUtils.getProjectService(getApplicationContext());
        taskService = APIUtils.getTaskService(getApplicationContext());

        taskRV = (RecyclerView)findViewById(R.id.projectTasksRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        taskRV.setLayoutManager(mLayoutManager);

        Intent intent = getIntent();
        projectId = intent.getIntExtra("projectId", -1); // -1 is equal for not found

        SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        userId = preferences.getInt("id", -1);

        new NewsAsyncTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            getParticipants();
            getTasks();
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
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 10, 0, 0);

                        viewFiles.setLayoutParams(params);
                    }

                    viewParticipants.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(ViewProjectActivity.this, ViewParticipantsActivity.class);
                            intent.putExtra("projectId", getIntent().getIntExtra("projectId", -1));
                            startActivity(intent);
                        }
                    });

                    viewFiles.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(ViewProjectActivity.this, FilesActivity.class);
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

    private void getParticipants(){
        Call<ArrayList<UserDetails>> call = projectService.getProjectUsersByProjectId(projectId);
        call.enqueue(new Callback<ArrayList<UserDetails>>() {
            @Override
            public void onResponse(Call<ArrayList<UserDetails>> call, Response<ArrayList<UserDetails>> response) {
                if(response.isSuccessful()){
                    users = response.body();
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
            public void onFailure(Call<ArrayList<UserDetails>> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ViewProjectActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTasks(){
        Call<ArrayList<TaskEntity>> call = taskService.getTasksByProjectId(projectId);
        call.enqueue(new Callback<ArrayList<TaskEntity>>() {
            @Override
            public void onResponse(Call<ArrayList<TaskEntity>> call, Response<ArrayList<TaskEntity>> response) {
                if(response.isSuccessful()){
                    tasks.clear();
                    tasks.addAll(response.body());

                    taskRVAdapter = new ProjectTasksAdapter(response.body());
                    taskRV.setAdapter(taskRVAdapter);
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
            public void onFailure(Call<ArrayList<TaskEntity>> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ViewProjectActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    View.OnClickListener onClickAddTask = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showAddTaskDialog();
        }
    };

    int mYear, mMonth, mDay, mHour, mMinute;
    Date deadlineValue = null;

    private void showAddTaskDialog()
    {
        // Create a AlertDialog Builder.
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ViewProjectActivity.this);
        // Set title, icon, can not cancel properties.
        alertDialogBuilder.setTitle("Create task");
        alertDialogBuilder.setCancelable(false);
        LayoutInflater layoutInflater = LayoutInflater.from(ViewProjectActivity.this);
        final View editProjectView = layoutInflater.inflate(R.layout.add_task_dialog, null);
        alertDialogBuilder.setView(editProjectView);
        final AlertDialog createTaskDialog = alertDialogBuilder.create();
        createTaskDialog.show();

        Button createTaskB = editProjectView.findViewById(R.id.create_button);
        Button cancelTaskB = editProjectView.findViewById(R.id.cancel_button);
        EditText titleET = editProjectView.findViewById(R.id.title_input);
        EditText contentET = editProjectView.findViewById(R.id.content_input);

        Button deadlineB = editProjectView.findViewById(R.id.deadline_button);
        TextView deadlineText = editProjectView.findViewById(R.id.deadline_text);

        deadlineB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get Current Date
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(ViewProjectActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                // Get Current Time
                                final Calendar c = Calendar.getInstance();
                                mHour = c.get(Calendar.HOUR_OF_DAY);
                                mMinute = c.get(Calendar.MINUTE);

                                // Launch Time Picker Dialog
                                TimePickerDialog timePickerDialog = new TimePickerDialog(ViewProjectActivity.this,
                                        new TimePickerDialog.OnTimeSetListener() {

                                            @Override
                                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                                  int minute) {

                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                                String deadlineAsString = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth+ " " +hourOfDay + ":" + minute;

                                                try {
                                                    deadlineValue = sdf.parse(deadlineAsString);
                                                    deadlineText.setText(deadlineAsString);
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }, mHour, mMinute, false);
                                timePickerDialog.show();
                            }
                        }, mYear, mMonth, mDay);

                DatePicker dp = datePickerDialog.getDatePicker();
                dp.setMinDate(c.getTimeInMillis());
                datePickerDialog.show();
            }
        });

        Spinner usersDropdown = editProjectView.findViewById(R.id.add_task_dialog_user_dropdown);
        ArrayList<String> options = new ArrayList<>();
        for (UserDetails user : users) {
            options.add(user.getUsername());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ViewProjectActivity.this,
                android.R.layout.simple_spinner_item,options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        usersDropdown.setAdapter(adapter);

        cancelTaskB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTaskDialog.hide();
            }
        });

        createTaskB.setOnClickListener(new View.OnClickListener() {
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

                if(deadlineValue == null){
                    Toast.makeText(getApplicationContext(), "Deadline is required", Toast.LENGTH_LONG).show();
                    return;
                }

                CreateTaskRequest createTaskRequest = new CreateTaskRequest();
                createTaskRequest.setTitle(title);
                createTaskRequest.setContent(content);
                createTaskRequest.setStatus("to-do");
                createTaskRequest.setDeadline(deadlineValue);
                String selectedUsername = usersDropdown.getSelectedItem().toString();

                int selectedUserId = -1;
                for (UserDetails currentUser : users) {
                    if (currentUser.getUsername() == selectedUsername) {
                        selectedUserId = currentUser.getId();
                    }
                }
                if(selectedUserId == -1) {
                    Toast.makeText(getApplicationContext(), "User is required", Toast.LENGTH_LONG).show();
                    return;
                }
                createTaskRequest.setAssignedUserId(selectedUserId);
                createTaskRequest.setProjectId(projectId);

                addTask(createTaskRequest);
                createTaskDialog.hide();
            }
        });
    }

    private void addTask(CreateTaskRequest createTaskRequest){
        Call<TaskEntity> call = taskService.createTask(createTaskRequest);
        call.enqueue(new Callback<TaskEntity>() {
            @Override
            public void onResponse(Call<TaskEntity> call, Response<TaskEntity> response) {
                if(response.isSuccessful()){
                    Toast.makeText(ViewProjectActivity.this, "The task was created successfully!", Toast.LENGTH_SHORT).show();
                    getTasks();
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
            public void onFailure(Call<TaskEntity> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ViewProjectActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}