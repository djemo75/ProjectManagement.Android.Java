package uni.fmi.management.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import uni.fmi.management.R;
import uni.fmi.management.ViewProjectActivity;
import uni.fmi.management.http.responses.ProjectEntity;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ViewHolder> {
    private ArrayList<ProjectEntity> projects;
    private Context context;

    public ProjectsAdapter(ArrayList<ProjectEntity> projects) {
        this.projects = projects;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.projects_list_item, parent, false);

        context = parent.getContext();
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProjectEntity project = projects.get(position);

        holder.title.setText(project.getTitle());

        String date = new SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(project.getCreatedDate());
        String time = new SimpleDateFormat("HH:mm").format(project.getCreatedDate());
        holder.date.setText(date);
        holder.time.setText(time);


        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ViewProjectActivity.class);
                intent.putExtra("projectId", project.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (projects != null) {
            return projects.size();
        } else {
            return 0;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView title;
        public final TextView date;
        public final TextView time;
        public final CardView card;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            title = view.findViewById(R.id.projects_list_item_title);
            date = view.findViewById(R.id.projects_list_item_date);
            time = view.findViewById(R.id.projects_list_item_time);
            card = view.findViewById(R.id.projects_list_item_card);
        }
    }
}