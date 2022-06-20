package uni.fmi.management.http.services;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import uni.fmi.management.http.responses.FileEntity;
import uni.fmi.management.http.responses.FileUploadResponse;
import uni.fmi.management.http.responses.MessageResponse;

public interface FileService {

    @GET("files/downloadFile/{fileCode}")
    Call<?> getFile(@Path("id") String fileCode);

    @GET("files/")
    Call<ArrayList<FileEntity>> getFilesByProjectId(@Query("projectId") int projectId);

    @Multipart
    @POST("files/uploadFile")
    Call<FileUploadResponse> uploadFile(@Part MultipartBody.Part file);

    @Multipart
    @POST("files/uploadAndSaveFile")
    Call<FileEntity> uploadAndSaveFile(@Part MultipartBody.Part file, @Part("projectId") RequestBody projectId);

    @DELETE("files")
    Call<MessageResponse> deleteFile(@Query("id") int id);
}