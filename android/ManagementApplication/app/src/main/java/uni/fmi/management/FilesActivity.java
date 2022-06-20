package uni.fmi.management;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uni.fmi.management.adapters.FilesAdapter;
import uni.fmi.management.adapters.ParticipantsAdapter;
import uni.fmi.management.http.APIUtils;
import uni.fmi.management.http.ErrorUtils;
import uni.fmi.management.http.requests.CreateCommentRequest;
import uni.fmi.management.http.responses.APIError;
import uni.fmi.management.http.responses.FileEntity;
import uni.fmi.management.http.responses.FileUploadResponse;
import uni.fmi.management.http.responses.UserDetails;
import uni.fmi.management.http.services.FileService;
import uni.fmi.management.http.utils.RealPathUtil;

public class FilesActivity extends AppCompatActivity {
    Button addB;
    FileService fileService;
    ArrayList<FileEntity> files = new ArrayList<>();
    RecyclerView fileRV;
    RecyclerView.Adapter fileRVAdapter;
    int projectId;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);

        fileService = APIUtils.getFileService(getApplicationContext());

        projectId = getIntent().getIntExtra("projectId", -1); // -1 is equal for not found

        SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        userId = preferences.getInt("id", -1);

        fileRV = (RecyclerView) findViewById(R.id.filesRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(FilesActivity.this);
        fileRV.setLayoutManager(mLayoutManager);

        getFiles(projectId);

        addB = findViewById(R.id.files_add);
        addB.setOnClickListener(onClickAddFile);
    }

    private void getFiles(int projectId){
        Call<ArrayList<FileEntity>> call = fileService.getFilesByProjectId(projectId);
        call.enqueue(new Callback<ArrayList<FileEntity>>() {
            @Override
            public void onResponse(Call<ArrayList<FileEntity>> call, Response<ArrayList<FileEntity>> response) {
                if(response.isSuccessful()){
                    files = response.body();

                    fileRVAdapter = new FilesAdapter(files, projectId);
                    fileRV.setAdapter(fileRVAdapter);
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(FilesActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(FilesActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<FileEntity>> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(FilesActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static final int PICK_FILE_CODE = 100;

    private View.OnClickListener onClickAddFile = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent();
                intent.setType("*/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_FILE_CODE);
            } else {
                ActivityCompat.requestPermissions(FilesActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == PICK_FILE_CODE) && (resultCode == -1)) {
            Uri uri = data.getData();
            Context context = FilesActivity.this;
            String path = RealPathUtil.getRealPath(context, uri);

            File file = new File(path);
            URLConnection connection = null;
            try {
                connection = file.toURL().openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String mimeType = connection.getContentType();

            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
            RequestBody id = RequestBody.create(MediaType.parse("text/plain"), projectId+"");

            Call<FileEntity> responseBodyCall = fileService.uploadAndSaveFile(body, id);
            responseBodyCall.enqueue(new Callback<FileEntity>() {
                @Override
                public void onResponse(Call<FileEntity> call, Response<FileEntity> response) {
                    if(response.isSuccessful()){
                        getFiles(projectId);
                    } else {
                        APIError error = ErrorUtils.parseError(response);
                        Toast.makeText(FilesActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        if(response.code() == 401){
                            Intent intent = new Intent(FilesActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    }
                }

                @Override
                public void onFailure(Call<FileEntity> call, Throwable t) {
                    Log.e("ERROR: ", t.getMessage());
                    Toast.makeText(FilesActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
}