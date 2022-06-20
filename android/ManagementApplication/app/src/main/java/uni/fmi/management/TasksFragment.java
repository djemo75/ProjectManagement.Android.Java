package uni.fmi.management;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uni.fmi.management.adapters.AssignedTasksAdapter;
import uni.fmi.management.adapters.ProjectsAdapter;
import uni.fmi.management.http.APIUtils;
import uni.fmi.management.http.ErrorUtils;
import uni.fmi.management.http.responses.APIError;
import uni.fmi.management.http.responses.ProjectEntity;
import uni.fmi.management.http.responses.TaskEntity;
import uni.fmi.management.http.services.ProjectService;
import uni.fmi.management.http.services.TaskService;

public class TasksFragment extends Fragment {
    ArrayList<TaskEntity> assignedTasks = new ArrayList<>();
    RecyclerView taskRV;
    RecyclerView.Adapter taskRVAdapter;
    TaskService taskService;

    public TasksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        taskService = APIUtils.getTaskService(getContext());

        taskRV = (RecyclerView) getView().findViewById(R.id.assignedTasksRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        taskRV.setLayoutManager(mLayoutManager);

        new TasksFragment.NewsAsyncTask().execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        new TasksFragment.NewsAsyncTask().execute();
    }

    private class NewsAsyncTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog = new ProgressDialog(getContext());

        public NewsAsyncTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setTitle("Loading all tasks...");
            dialog.show();
            assignedTasks.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getAssignedTasks();
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            dialog.cancel();
        }
    }

    private void getAssignedTasks(){
        Call<ArrayList<TaskEntity>> call = taskService.getAssignedTasks();
        call.enqueue(new Callback<ArrayList<TaskEntity>>() {
            @Override
            public void onResponse(Call<ArrayList<TaskEntity>> call, Response<ArrayList<TaskEntity>> response) {
                if(response.isSuccessful()){
                    assignedTasks.clear();
                    assignedTasks.addAll(response.body());

                    taskRVAdapter = new AssignedTasksAdapter(response.body());
                    taskRV.setAdapter(taskRVAdapter);
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<TaskEntity>> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}