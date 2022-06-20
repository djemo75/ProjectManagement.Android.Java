package uni.fmi.management.http;

import android.content.Context;

import uni.fmi.management.http.services.AuthService;
import uni.fmi.management.http.services.CommentService;
import uni.fmi.management.http.services.FileService;
import uni.fmi.management.http.services.ProjectService;
import uni.fmi.management.http.services.TaskService;

public class APIUtils {

    private APIUtils(){
    };

    public static final String API_URL = "http://192.168.0.103:8080";

    public static AuthService getAuthService(Context context){
        return RetrofitClient.getClient(API_URL, context).create(AuthService.class);
    }

    public static ProjectService getProjectService(Context context){
        return RetrofitClient.getClient(API_URL, context).create(ProjectService.class);
    }

    public static TaskService getTaskService(Context context){
        return RetrofitClient.getClient(API_URL, context).create(TaskService.class);
    }

    public static CommentService getCommentService(Context context){
        return RetrofitClient.getClient(API_URL, context).create(CommentService.class);
    }

    public static FileService getFileService(Context context){
        return RetrofitClient.getClient(API_URL, context).create(FileService.class);
    }
}