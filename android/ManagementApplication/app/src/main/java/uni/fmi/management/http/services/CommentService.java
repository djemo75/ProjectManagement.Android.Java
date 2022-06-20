package uni.fmi.management.http.services;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import uni.fmi.management.http.requests.CreateCommentRequest;
import uni.fmi.management.http.responses.CommentEntity;
import uni.fmi.management.http.responses.MessageResponse;

public interface CommentService {

    @GET("comments/task")
    Call<ArrayList<CommentEntity>> getCommentsByTaskId(@Query("taskId") int taskId);

    @GET("comments/")
    Call<CommentEntity> getCommentById(@Query("id") int id);

    @POST("comments/")
    Call<CommentEntity> createComment(@Body CreateCommentRequest createCommentRequest);

    @DELETE("comments")
    Call<MessageResponse> deleteComment(@Query("id") int id);
}