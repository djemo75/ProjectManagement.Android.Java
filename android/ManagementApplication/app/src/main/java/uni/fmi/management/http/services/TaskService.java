package uni.fmi.management.http.services;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import uni.fmi.management.http.requests.CreateTaskRequest;
import uni.fmi.management.http.requests.EditTaskRequest;
import uni.fmi.management.http.responses.MessageResponse;
import uni.fmi.management.http.responses.TaskEntity;

public interface TaskService {

    @GET("tasks/assigned")
    Call<ArrayList<TaskEntity>> getAssignedTasks();

    @GET("tasks/project")
    Call<ArrayList<TaskEntity>> getTasksByProjectId(@Query("projectId") int projectId);

    @GET("tasks/")
    Call<TaskEntity> getTaskById(@Query("id") int id);

    @POST("tasks/")
    Call<TaskEntity> createTask(@Body CreateTaskRequest createTaskRequest);

    @PUT("tasks")
    Call<MessageResponse> editTask(@Query("id") int id, @Body EditTaskRequest editTaskRequest);

    @DELETE("tasks")
    Call<MessageResponse> deleteTask(@Query("id") int id);
}