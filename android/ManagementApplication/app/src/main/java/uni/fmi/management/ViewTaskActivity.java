package uni.fmi.management;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.stream.IntStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uni.fmi.management.adapters.CommentsAdapter;
import uni.fmi.management.http.APIUtils;
import uni.fmi.management.http.ErrorUtils;
import uni.fmi.management.http.requests.CreateCommentRequest;
import uni.fmi.management.http.requests.EditTaskRequest;
import uni.fmi.management.http.responses.APIError;
import uni.fmi.management.http.responses.CommentEntity;
import uni.fmi.management.http.responses.FileUploadResponse;
import uni.fmi.management.http.responses.MessageResponse;
import uni.fmi.management.http.responses.TaskEntity;
import uni.fmi.management.http.responses.UserDetails;
import uni.fmi.management.http.services.CommentService;
import uni.fmi.management.http.services.FileService;
import uni.fmi.management.http.services.ProjectService;
import uni.fmi.management.http.services.TaskService;
import uni.fmi.management.http.utils.RealPathUtil;

public class ViewTaskActivity extends AppCompatActivity {
    TextView title;
    TextView content;
    TextView status;
    ImageView statusImage;
    TextView date;
    TextView time;
    TextView username;
    TextView project;
    TextView deadline;
    Button deleteB;
    Button editB;
    Button markAsCompleteB;
    Button sendCommentB;
    Button uploadFileB;
    EditText commentET;
    ArrayList<UserDetails> users = new ArrayList<>();
    TaskService taskService;
    ProjectService projectService;
    CommentService commentService;
    FileService fileService;
    ArrayList<CommentEntity> comments = new ArrayList<>();
    RecyclerView commentRV;
    RecyclerView.Adapter commentRVAdapter;
    TaskEntity task;
    int taskId;
    int userId;
    boolean isTaskOwner = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_task);

        title = findViewById(R.id.view_task_title);
        content = findViewById(R.id.view_task_content);
        date = findViewById(R.id.view_task_date);
        time = findViewById(R.id.view_task_time);
        username = findViewById(R.id.view_task_username);
        project = findViewById(R.id.view_task_project);
        deadline = findViewById(R.id.view_task_deadline);
        status = findViewById(R.id.view_task_status);
        statusImage = findViewById(R.id.view_task_status_image);
        deleteB = findViewById(R.id.view_task_delete);
        deleteB.setOnClickListener(onClickDelete);
        editB = findViewById(R.id.view_task_edit);
        editB.setOnClickListener(onClickEdit);
        markAsCompleteB = findViewById(R.id.view_task_mark_as_complete);
        markAsCompleteB.setOnClickListener(onClickMarkAsComplete);
        sendCommentB = findViewById(R.id.send_comment_button);
        sendCommentB.setOnClickListener(onClickSendComment);
        commentET = findViewById(R.id.comment_input);
        uploadFileB = findViewById(R.id.upload_file_button);
        uploadFileB.setOnClickListener(onUploadFile);

        taskService = APIUtils.getTaskService(getApplicationContext());
        projectService = APIUtils.getProjectService(getApplicationContext());
        commentService = APIUtils.getCommentService(getApplicationContext());
        fileService = APIUtils.getFileService(getApplicationContext());

        commentRV = (RecyclerView)findViewById(R.id.commentsRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        commentRV.setLayoutManager(mLayoutManager);

        Intent intent = getIntent();
        taskId = intent.getIntExtra("taskId", -1); // -1 is equal for not found

        SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        userId = preferences.getInt("id", -1);

        new ViewTaskActivity.NewsAsyncTask().execute();
    }

    private class NewsAsyncTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog = new ProgressDialog(ViewTaskActivity.this);

        public NewsAsyncTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setTitle("Loading the task...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getTask(taskId);
            getAllUsers();
            getComments();
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            dialog.hide();
        }
    }

    private void getTask(int taskId){
        Call<TaskEntity> call = taskService.getTaskById(taskId);
        call.enqueue(new Callback<TaskEntity>() {
            @Override
            public void onResponse(Call<TaskEntity> call, Response<TaskEntity> response) {
                if(response.isSuccessful()){
                    task = response.body();
                    String titleValue = task.getTitle().toString();
                    String contentValue = task.getContent().toString();
                    String statusValue = task.getStatus().toString();
                    String dateValue = new SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(task.getCreatedDate());
                    String timeValue = new SimpleDateFormat("HH:mm").format(task.getCreatedDate());
                    String usernameValue = task.getAssignedUser().getUsername().toString()
                            +" ("+task.getAssignedUser().getName().toString()+")";
                    String projectValue = task.getProject().getTitle().toString();
                    String deadlineValue = new SimpleDateFormat("MMMM dd, yyyy HH:mm").format(task.getDeadline());
                    isTaskOwner = task.getAssignedUser().getId()==userId;

                    title.setText(titleValue);
                    content.setText(contentValue);
                    date.setText(dateValue);
                    time.setText(timeValue);
                    username.setText(usernameValue);
                    project.setText(projectValue);
                    deadline.setText(deadlineValue);

                    switch (statusValue){
                        case "to-do": statusImage.setImageResource(R.drawable.icon_task_status_todo);
                            status.setText("Not started");
                            markAsCompleteB.setVisibility(View.VISIBLE);
                            break;
                        case "in-progress": statusImage.setImageResource(R.drawable.icon_task_status_in_progress);
                            status.setText("In progress");
                            markAsCompleteB.setVisibility(View.VISIBLE);
                            break;
                        case "complete": statusImage.setImageResource(R.drawable.icon_task_status_complete);
                            status.setText("Complete");
                            markAsCompleteB.setVisibility(View.GONE);
                            break;
                        case "completed-after-deadline": statusImage.setImageResource(R.drawable.icon_task_status_completed_after_deadline);
                            status.setText("Completed after deadline");
                            markAsCompleteB.setVisibility(View.GONE);
                            break;
                    }

                    if(!isTaskOwner) {
                        deleteB.setVisibility(View.GONE);
                        editB.setVisibility(View.GONE);
                        markAsCompleteB.setVisibility(View.GONE);
                    }
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(ViewTaskActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(ViewTaskActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<TaskEntity> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ViewTaskActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private View.OnClickListener onClickDelete = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showDeleteDialog();
        }
    };

    private void showDeleteDialog()
    {
        // Create a AlertDialog Builder.
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ViewTaskActivity.this);
        // Set title, icon, can not cancel properties.
        alertDialogBuilder.setTitle("Are you sure you want to delete the task?");
        alertDialogBuilder.setCancelable(false);
        LayoutInflater layoutInflater = LayoutInflater.from(ViewTaskActivity.this);
        final View editProjectView = layoutInflater.inflate(R.layout.delete_task_dialog, null);
        alertDialogBuilder.setView(editProjectView);
        final AlertDialog deleteTaskDialog = alertDialogBuilder.create();
        deleteTaskDialog.show();

        Button editB = editProjectView.findViewById(R.id.delete_button);
        Button cancelB = editProjectView.findViewById(R.id.cancel_button);

        cancelB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTaskDialog.hide();
            }
        });

        editB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTask(taskId);
                deleteTaskDialog.hide();
            }
        });
    }

    private void deleteTask(int id){
        Call<MessageResponse> call = taskService.deleteTask(id);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if(response.isSuccessful()){
                    MessageResponse msgResponse = response.body();
                    Toast.makeText(ViewTaskActivity.this, msgResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(ViewTaskActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(ViewTaskActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ViewTaskActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ViewTaskActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(ViewTaskActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<UserDetails>> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ViewTaskActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private View.OnClickListener onClickEdit = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showEditDialog();
        }
    };

    private void showEditDialog()
    {
        // Create a AlertDialog Builder.
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ViewTaskActivity.this);
        // Set title, icon, can not cancel properties.
        alertDialogBuilder.setTitle("Edit task");
        alertDialogBuilder.setCancelable(false);
        LayoutInflater layoutInflater = LayoutInflater.from(ViewTaskActivity.this);
        final View editProjectView = layoutInflater.inflate(R.layout.edit_task_dialog, null);
        alertDialogBuilder.setView(editProjectView);
        final AlertDialog editTaskDialog = alertDialogBuilder.create();
        editTaskDialog.show();

        Button editB = editProjectView.findViewById(R.id.save_button);
        Button cancelB = editProjectView.findViewById(R.id.cancel_button);
        EditText titleET = editProjectView.findViewById(R.id.title_input);
        EditText contentET = editProjectView.findViewById(R.id.content_input);

        // Users dropdown
        Spinner usersDropdown = editProjectView.findViewById(R.id.edit_task_dialog_user_dropdown);
        ArrayList<String> usersDropdownOptions = new ArrayList<>();
        for (UserDetails user : users) {
            usersDropdownOptions.add(user.getUsername());
        }
        ArrayAdapter<String> usersDropdownAdapter = new ArrayAdapter<String>(ViewTaskActivity.this,
                android.R.layout.simple_spinner_item,usersDropdownOptions);
        usersDropdownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        usersDropdown.setAdapter(usersDropdownAdapter);
        int index = IntStream.range(0, users.size())
                .filter(i -> users.get(i).getId() == task.getAssignedUser().getId())
                .findFirst()
                .orElse(-1);
        if(index != -1) {
            usersDropdown.setSelection(index);
        }

        // Status dropdown
        Spinner statusDropdown = editProjectView.findViewById(R.id.edit_task_dialog_status_dropdown);
        ArrayList<String> statusDropdownOptions = new ArrayList<>();
        statusDropdownOptions.add("Not completed");
        statusDropdownOptions.add("In progress");
        statusDropdownOptions.add("Complete");
        statusDropdownOptions.add("Completed after deadline");
        ArrayAdapter<String> statusDropdownAdapter = new ArrayAdapter<String>(ViewTaskActivity.this,
                android.R.layout.simple_spinner_item,statusDropdownOptions);
        statusDropdownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusDropdown.setAdapter(statusDropdownAdapter);
        switch (task.getStatus()){
            case "to-do":
                statusDropdown.setSelection(0);
                break;
            case "in-progress":
                statusDropdown.setSelection(1);
                break;
            case "complete":
                statusDropdown.setSelection(2);
                break;
            case "completed-after-deadline":
                statusDropdown.setSelection(3);
                break;
        }


        titleET.setText(task.getTitle());
        contentET.setText(task.getContent());

        cancelB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTaskDialog.hide();
            }
        });

        editB.setOnClickListener(new View.OnClickListener() {
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

                String selectedStatusText = statusDropdown.getSelectedItem().toString();
                String selectedStatusValue = "to-do";
                switch (selectedStatusText){
                    case "Not started": selectedStatusValue = "to-do";
                        break;
                    case "In progress": selectedStatusValue = "in-progress";
                        break;
                    case "Complete": selectedStatusValue = "complete";
                        break;
                    case "Completed after deadline": selectedStatusValue = "completed-after-deadline";
                        break;
                }

                EditTaskRequest editTaskRequest = new EditTaskRequest();
                editTaskRequest.setTitle(title);
                editTaskRequest.setContent(content);
                editTaskRequest.setAssignedUserId(selectedUserId);
                editTaskRequest.setStatus(selectedStatusValue);
                editTask(taskId, editTaskRequest);
                editTaskDialog.hide();
            }
        });
    }

    private View.OnClickListener onClickMarkAsComplete = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            EditTaskRequest editTaskRequest = new EditTaskRequest();
            editTaskRequest.setTitle(task.getTitle());
            editTaskRequest.setContent(task.getContent());
            editTaskRequest.setAssignedUserId(task.getAssignedUser().getId());

            if(task.getDeadline().after(task.getDeadline())) {
                editTaskRequest.setStatus("completed-after-deadline");
            } else {
                editTaskRequest.setStatus("complete");
            }
            editTask(taskId, editTaskRequest);
        }
    };


    private void editTask(int id, EditTaskRequest editTaskRequest){
        Call<MessageResponse> call = taskService.editTask(id, editTaskRequest);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if(response.isSuccessful()){
                    MessageResponse msgResponse = response.body();
                    Toast.makeText(ViewTaskActivity.this, msgResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    getTask(taskId);
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(ViewTaskActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(ViewTaskActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ViewTaskActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getComments(){
        Call<ArrayList<CommentEntity>> call = commentService.getCommentsByTaskId(taskId);
        call.enqueue(new Callback<ArrayList<CommentEntity>>() {
            @Override
            public void onResponse(Call<ArrayList<CommentEntity>> call, Response<ArrayList<CommentEntity>> response) {
                if(response.isSuccessful()){
                    comments.clear();
                    comments.addAll(response.body());

                    commentRVAdapter = new CommentsAdapter(response.body(), taskId);
                    commentRV.setAdapter(commentRVAdapter);
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(ViewTaskActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(ViewTaskActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<CommentEntity>> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ViewTaskActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private View.OnClickListener onClickSendComment = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String comment = commentET.getText().toString();

            if(comment.length() == 0){
                Toast.makeText(getApplicationContext(), "Comment is required", Toast.LENGTH_LONG).show();
                return;
            }

            CreateCommentRequest createCommentRequest = new CreateCommentRequest();
            createCommentRequest.setType("text");
            createCommentRequest.setContent(comment);
            createCommentRequest.setResourcePath("");
            createCommentRequest.setTaskId(taskId);
            addComment(createCommentRequest);
        }
    };

    private void addComment(CreateCommentRequest createCommentRequest){
        Call<CommentEntity> call = commentService.createComment(createCommentRequest);
        call.enqueue(new Callback<CommentEntity>() {
            @Override
            public void onResponse(Call<CommentEntity> call, Response<CommentEntity> response) {
                if(response.isSuccessful()){
                    commentET.setText("");
                    Toast.makeText(ViewTaskActivity.this, "The comment was added successfully!", Toast.LENGTH_SHORT).show();
                    getComments();
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(ViewTaskActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(ViewTaskActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<CommentEntity> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ViewTaskActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static final int PICK_IMAGE_CODE = 100;

    private View.OnClickListener onUploadFile = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_IMAGE_CODE);
            } else {
                ActivityCompat.requestPermissions(ViewTaskActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == PICK_IMAGE_CODE) && (resultCode == -1)) {
            Uri  uri = data.getData();
            Context context = ViewTaskActivity.this;
            String path = RealPathUtil.getRealPath(context, uri);

            File file = new File(path);
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/formdata"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            Call<FileUploadResponse> responseBodyCall = fileService.uploadFile(body);
            responseBodyCall.enqueue(new Callback<FileUploadResponse>() {
                @Override
                public void onResponse(Call<FileUploadResponse> call, Response<FileUploadResponse> response) {
                    if(response.isSuccessful()){
                        FileUploadResponse fileUploadResponse = response.body();

                        CreateCommentRequest createCommentRequest = new CreateCommentRequest();
                        createCommentRequest.setType("image");
                        createCommentRequest.setContent("");
                        createCommentRequest.setResourcePath(fileUploadResponse.getPath());
                        createCommentRequest.setTaskId(taskId);

                        addComment(createCommentRequest);
                    } else {
                        APIError error = ErrorUtils.parseError(response);
                        Toast.makeText(ViewTaskActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        if(response.code() == 401){
                            Intent intent = new Intent(ViewTaskActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    }
                }

                @Override
                public void onFailure(Call<FileUploadResponse> call, Throwable t) {
                    Log.e("ERROR: ", t.getMessage());
                    Toast.makeText(ViewTaskActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
}