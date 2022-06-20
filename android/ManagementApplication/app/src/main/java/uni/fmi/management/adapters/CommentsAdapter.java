package uni.fmi.management.adapters;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uni.fmi.management.LoginActivity;
import uni.fmi.management.R;
import uni.fmi.management.http.APIUtils;
import uni.fmi.management.http.ErrorUtils;
import uni.fmi.management.http.responses.APIError;
import uni.fmi.management.http.responses.CommentEntity;
import uni.fmi.management.http.responses.MessageResponse;
import uni.fmi.management.http.services.CommentService;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
    private ArrayList<CommentEntity> comments;
    private Context context;
    private int userId;
    private CommentService commentService;
    private int taskId;

    public CommentsAdapter(ArrayList<CommentEntity> comments, int taskId) {
        this.comments = comments;
        this.taskId = taskId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_list_item, parent, false);

        context = parent.getContext();
        commentService = APIUtils.getCommentService(context);

        SharedPreferences preferences = context.getSharedPreferences("user", MODE_PRIVATE);
        userId = preferences.getInt("id", -1);
        return new CommentsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommentEntity comment = comments.get(position);

        holder.content.setText(comment.getContent());
        holder.user.setText(comment.getAuthor().getUsername());

        String date = new SimpleDateFormat("MMMM dd yyyy", Locale.US).format(comment.getCreatedDate());
        String time = new SimpleDateFormat("HH:mm").format(comment.getCreatedDate());
        holder.createdDate.setText(date + ", "+time);

        if(comment.getAuthor().getId()!=userId) {
            holder.remove.setVisibility(View.GONE);
        }
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteComment(comment.getId());
            }
        });

        switch (comment.getType()){
            case "text":
                holder.image.setVisibility(View.GONE);
                holder.content.setVisibility(View.VISIBLE);
                break;
            case "image":
                holder.content.setVisibility(View.GONE);
                holder.image.setVisibility(View.VISIBLE);
                new DownloadImageFromInternet((ImageView) holder.image)
                    .execute(APIUtils.API_URL + comment.getResourcePath());
                break;
        }

    }

    @Override
    public int getItemCount() {
        if (comments != null) {
            return comments.size();
        } else {
            return 0;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView content;
        public final TextView createdDate;
        public final TextView user;
        public final Button remove;
        public final ImageView image;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            content = view.findViewById(R.id.comment_list_item_content);
            createdDate = view.findViewById(R.id.comment_list_item_date);
            user = view.findViewById(R.id.comment_list_item_user);
            remove = view.findViewById(R.id.comment_list_item_delete);
            image = view.findViewById(R.id.comment_list_item_image);
        }
    }

    private void deleteComment(int commentId){
        Call<MessageResponse> call = commentService.deleteComment(commentId);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if(response.isSuccessful()){
                    MessageResponse msgResponse = response.body();
                    Toast.makeText(context, msgResponse.getMessage(), Toast.LENGTH_SHORT).show();

                    Optional<CommentEntity> optional = comments.stream()
                            .filter(x -> commentId == x.getId())
                            .findFirst();

                    if(optional.isPresent()) {
                        CommentEntity removed = optional.get();
                        int index = comments.indexOf(removed);
                        comments.remove(index);
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

    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;
        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView=imageView;
        }
        protected Bitmap doInBackground(String... urls) {
            String imageURL=urls[0];
            Bitmap bimage=null;
            try {
                InputStream in=new java.net.URL(imageURL).openStream();
                bimage= BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }
            return bimage;
        }
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }
}