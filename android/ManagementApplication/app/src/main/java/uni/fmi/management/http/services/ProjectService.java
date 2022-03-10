package uni.fmi.management.http.services;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import uni.fmi.management.http.requests.CreateProjectRequest;
import uni.fmi.management.http.requests.EditProjectRequest;
import uni.fmi.management.http.responses.MessageResponse;
import uni.fmi.management.http.responses.ProjectEntity;
import uni.fmi.management.http.responses.ProjectParticipantEntity;
import uni.fmi.management.http.responses.UserDetails;

public interface ProjectService {

    @GET("projects")
    Call<ArrayList<ProjectEntity>> getProjects(@Query("phrase") String phrase);

    @GET("projects/participated")
    Call<ArrayList<ProjectEntity>> getParticipatedProjects(@Query("userId") int userId);

    @GET("projects/")
    Call<ProjectEntity> getProjectById(@Query("id") int id);

    @GET("projects/all-users")
    Call<ArrayList<UserDetails>> getAllUsers();

    @GET("projects/participated-users")
    Call<ArrayList<UserDetails>> getProjectUsersByProjectId(@Query("projectId") int projectId);

    @POST("projects/")
    Call<ProjectEntity> createProject(@Body CreateProjectRequest createProjectRequest);

    @POST("projects/add-user")
    Call<ProjectParticipantEntity> addParticipantToProject(@Query("userId") int userId, @Query("projectId") int projectId);

    @PUT("projects")
    Call<MessageResponse> editProject(@Query("id") int id, @Body EditProjectRequest editProjectRequest);

    @DELETE("projects/remove-user")
    Call<MessageResponse> removeParticipantFromProject(@Query("userId") int userId, @Query("projectId") int projectId);

    @DELETE("projects")
    Call<MessageResponse> deleteProject(@Query("id") int id);
}