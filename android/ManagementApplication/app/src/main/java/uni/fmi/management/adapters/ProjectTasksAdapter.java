package uni.fmi.management.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import uni.fmi.management.R;
import uni.fmi.management.ViewProjectActivity;
import uni.fmi.management.ViewTaskActivity;
import uni.fmi.management.http.responses.TaskEntity;

public class ProjectTasksAdapter extends RecyclerView.Adapter<ProjectTasksAdapter.ViewHolder> {
    private ArrayList<TaskEntity> projectTasks;
    private Context context;

    public ProjectTasksAdapter(ArrayList<TaskEntity> projectTasks) {
        this.projectTasks = projectTasks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.tasks_list_item, parent, false);

        context = parent.getContext();
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskEntity task = projectTasks.get(position);

        holder.title.setText(task.getTitle());
        holder.user.setText(task.getAssignedUser().getUsername());
        holder.projectRow.setVisibility(View.GONE);
        switch (task.getStatus()){
            case "to-do": holder.status.setImageResource(R.drawable.icon_task_status_todo);
                break;
            case "in-progress": holder.status.setImageResource(R.drawable.icon_task_status_in_progress);
                break;
            case "complete": holder.status.setImageResource(R.drawable.icon_task_status_complete);
                break;
            case "completed-after-deadline": holder.status.setImageResource(R.drawable.icon_task_status_completed_after_deadline);
                break;
        }

        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ViewTaskActivity.class);
                intent.putExtra("taskId", task.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (projectTasks != null) {
            return projectTasks.size();
        } else {
            return 0;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView title;
        public final LinearLayout projectRow;
        public final TextView project;
        public final LinearLayout userRow;
        public final TextView user;
        public final ImageView status;
        public final LinearLayout item;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            title = view.findViewById(R.id.task_list_item_title);
            projectRow = view.findViewById(R.id.task_list_item_project_row);
            project = view.findViewById(R.id.task_list_item_project);
            userRow = view.findViewById(R.id.task_list_item_user_row);
            user = view.findViewById(R.id.task_list_item_user);
            status = view.findViewById(R.id.task_list_item_status);
            item = view.findViewById(R.id.task_list_item);
        }
    }
}