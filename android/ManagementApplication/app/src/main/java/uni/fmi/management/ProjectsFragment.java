package uni.fmi.management;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uni.fmi.management.adapters.ProjectsAdapter;
import uni.fmi.management.http.APIUtils;
import uni.fmi.management.http.ErrorUtils;
import uni.fmi.management.http.requests.CreateProjectRequest;
import uni.fmi.management.http.responses.APIError;
import uni.fmi.management.http.responses.ProjectEntity;
import uni.fmi.management.http.services.ProjectService;

public class ProjectsFragment extends Fragment {
    ArrayList<ProjectEntity> projects = new ArrayList<>();
    EditText searchET;
    RecyclerView projectRV;
    RecyclerView.Adapter projectRVAdapter;
    ProjectService projectService;

    FloatingActionButton addProjectB;
    int userId;

    public ProjectsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_projects, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        projectService = APIUtils.getProjectService(getContext());
        SharedPreferences preferences = this.getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        userId = preferences.getInt("id", -1);

        addProjectB = getView().findViewById(R.id.projects_page_add_project);
        addProjectB.setOnClickListener(onClickAddProjectB);

        projectRV = (RecyclerView) getView().findViewById(R.id.projectsRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        projectRV.setLayoutManager(mLayoutManager);

        searchET = getView().findViewById(R.id.projects_page_search);
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String phrase = charSequence.toString().toLowerCase();
                ArrayList<ProjectEntity> filteredProjects = new ArrayList<>();
                if (phrase == "") {
                    filteredProjects.addAll(projects);
                } else {
                    projects.forEach(currentProject -> {
                        if (currentProject.getTitle().toLowerCase().contains(phrase)) {
                            filteredProjects.add(currentProject);
                        }
                    });
                }
                renderProjects(filteredProjects);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        new NewsAsyncTask().execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        searchET.setText(""); // Clear search when switch to other fragment
    }

    private class NewsAsyncTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog = new ProgressDialog(getContext());

        public NewsAsyncTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setTitle("Loading all projects...");
            dialog.show();
            projects.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getProjects(userId);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            dialog.cancel();
        }
    }

    private void getProjects(int userId){
        Call<ArrayList<ProjectEntity>> call = projectService.getParticipatedProjects(userId);
        call.enqueue(new Callback<ArrayList<ProjectEntity>>() {
            @Override
            public void onResponse(Call<ArrayList<ProjectEntity>> call, Response<ArrayList<ProjectEntity>> response) {
                if(response.isSuccessful()){
                    projects.clear();
                    projects.addAll(response.body());

                    renderProjects(projects);
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
            public void onFailure(Call<ArrayList<ProjectEntity>> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    View.OnClickListener onClickAddProjectB = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showAddDialog();
        }
    };

    private void showAddDialog()
    {
        // Create a AlertDialog Builder.
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        // Set title, icon, can not cancel properties.
        alertDialogBuilder.setTitle("Create project");
        alertDialogBuilder.setCancelable(false);
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        final View addProjectView = layoutInflater.inflate(R.layout.add_project_dialog, null);
        alertDialogBuilder.setView(addProjectView);
        final AlertDialog addProjectDialog = alertDialogBuilder.create();
        addProjectDialog.show();

        Button createProjectB = addProjectView.findViewById(R.id.create_button);
        Button cancelProjectB = addProjectView.findViewById(R.id.cancel_button);

        cancelProjectB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProjectDialog.hide();
            }
        });

        createProjectB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText titleET = addProjectView.findViewById(R.id.title_input);
                EditText contentET = addProjectView.findViewById(R.id.content_input);
                String title = titleET.getText().toString();
                String content = contentET.getText().toString();

                if(title.length() == 0){
                    Toast.makeText(getContext(), "Title is required", Toast.LENGTH_LONG).show();
                    return;
                }

                if(content.length() == 0){
                    Toast.makeText(getContext(), "Content is required", Toast.LENGTH_LONG).show();
                    return;
                }

                CreateProjectRequest createProjectRequest = new CreateProjectRequest();
                createProjectRequest.setTitle(title);
                createProjectRequest.setContent(content);
                addProject(createProjectRequest);
                addProjectDialog.hide();
            }
        });
    }

    private void addProject(CreateProjectRequest createProjectRequest){
        Call<ProjectEntity> call = projectService.createProject(createProjectRequest);
        call.enqueue(new Callback<ProjectEntity>() {
            @Override
            public void onResponse(Call<ProjectEntity> call, Response<ProjectEntity> response) {
                if(response.isSuccessful()){
                    Toast.makeText(getContext(), "The project was created successfully!", Toast.LENGTH_SHORT).show();
                    getProjects(userId);
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
            public void onFailure(Call<ProjectEntity> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderProjects(ArrayList<ProjectEntity> listOfProjects) {
        projectRVAdapter = new ProjectsAdapter(listOfProjects);
        projectRV.setAdapter(projectRVAdapter);
    }
}