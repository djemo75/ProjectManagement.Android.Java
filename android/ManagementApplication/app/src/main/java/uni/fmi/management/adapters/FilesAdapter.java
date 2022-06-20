package uni.fmi.management.adapters;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uni.fmi.management.FilesActivity;
import uni.fmi.management.LoginActivity;
import uni.fmi.management.R;
import uni.fmi.management.http.APIUtils;
import uni.fmi.management.http.ErrorUtils;
import uni.fmi.management.http.responses.APIError;
import uni.fmi.management.http.responses.CommentEntity;
import uni.fmi.management.http.responses.FileEntity;
import uni.fmi.management.http.responses.MessageResponse;
import uni.fmi.management.http.services.FileService;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {
    private ArrayList<FileEntity> files;
    private Context context;
    private FileService fileService;
    private int userId;
    private int projectId;

    public FilesAdapter(ArrayList<FileEntity> files, int projectId) {
        this.files = files; this.projectId = projectId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.files_list_item, parent, false);

        context = parent.getContext();
        fileService = APIUtils.getFileService(context);

        SharedPreferences preferences = context.getSharedPreferences("user", MODE_PRIVATE);
        userId = preferences.getInt("id", -1);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileEntity file = files.get(position);

        holder.fileName.setText(file.getFileName());
        holder.author.setText(file.getUser().getUsername());

        String date = new SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.US).format(file.getCreatedDate());
        holder.date.setText(date);

        holder.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = APIUtils.API_URL + file.getPath();
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);

                intent.setDataAndType(uri, file.getContentType());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Please install application that can open this file!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(file.getUser().getId()!=userId) {
            holder.deleteButton.setVisibility(View.GONE);
        }
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFile(file.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        if (files != null) {
            return files.size();
        } else {
            return 0;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView fileName;
        public final TextView author;
        public final TextView date;
        public final ImageView downloadButton;
        public final Button deleteButton;
        public final LinearLayout item;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            fileName = view.findViewById(R.id.file_list_item_file_name);
            author = view.findViewById(R.id.file_list_item_author);
            date = view.findViewById(R.id.file_list_item_date);
            downloadButton = view.findViewById(R.id.file_download_button);
            deleteButton = view.findViewById(R.id.file_list_item_delete);
            item = view.findViewById(R.id.file_list_item);
        }
    }

    private void deleteFile(int fileId){
        Call<MessageResponse> call = fileService.deleteFile(fileId);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if(response.isSuccessful()){
                    MessageResponse msgResponse = response.body();
                    Toast.makeText(context, msgResponse.getMessage(), Toast.LENGTH_SHORT).show();

                    Optional<FileEntity> optional = files.stream()
                            .filter(x -> fileId == x.getId())
                            .findFirst();

                    if(optional.isPresent()) {
                        FileEntity removed = optional.get();
                        int index = files.indexOf(removed);
                        files.remove(index);
                        notifyItemRemoved(index);
                    }
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(context, LoginActivity.class);
                        context.startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}