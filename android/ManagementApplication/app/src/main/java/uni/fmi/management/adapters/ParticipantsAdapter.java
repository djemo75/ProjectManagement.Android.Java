package uni.fmi.management.adapters;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uni.fmi.management.LoginActivity;
import uni.fmi.management.R;
import uni.fmi.management.ViewParticipantsActivity;
import uni.fmi.management.ViewProjectActivity;
import uni.fmi.management.http.APIUtils;
import uni.fmi.management.http.ErrorUtils;
import uni.fmi.management.http.responses.APIError;
import uni.fmi.management.http.responses.MessageResponse;
import uni.fmi.management.http.responses.ProjectEntity;
import uni.fmi.management.http.responses.UserDetails;
import uni.fmi.management.http.services.ProjectService;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.ViewHolder> {
    private ArrayList<UserDetails> users;
    private int userId;
    private Context context;
    private ProjectService projectService;
    private int projectId;

    public ParticipantsAdapter(ArrayList<UserDetails> users, int projectId) {
        this.users = users; this.projectId = projectId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.participants_list_item, parent, false);

        context = parent.getContext();
        projectService = APIUtils.getProjectService(context);

        SharedPreferences preferences = context.getSharedPreferences("user", MODE_PRIVATE);
        userId = preferences.getInt("id", -1);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserDetails user = users.get(position);

        holder.title.setText(user.getUsername()+" ("+user.getName()+")");

        if(user.getId()==userId) {
            holder.remove.setVisibility(View.GONE);
        }
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeParticipant(user.getId(), projectId);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (users != null) {
            return users.size();
        } else {
            return 0;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView title;
        public final Button remove;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            title = view.findViewById(R.id.participants_list_item_title);
            remove = view.findViewById(R.id.participants_list_item_remove);
        }
    }

    private void removeParticipant(int userId, int projectId){
        Call<MessageResponse> call = projectService.removeParticipantFromProject(userId, projectId);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if(response.isSuccessful()){
                    MessageResponse msgResponse = response.body();
                    Toast.makeText(context, msgResponse.getMessage(), Toast.LENGTH_SHORT).show();

                    Optional<UserDetails> optional = users.stream()
                            .filter(x -> userId == x.getId())
                            .findFirst();

                    if(optional.isPresent()) {
                        UserDetails removed = optional.get();
                        int index = users.indexOf(removed);
                        users.remove(index);
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