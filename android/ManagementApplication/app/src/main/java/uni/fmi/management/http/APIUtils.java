package uni.fmi.management.http;

import android.content.Context;

import uni.fmi.management.http.services.AuthService;
import uni.fmi.management.http.services.ProjectService;

public class APIUtils {

    private APIUtils(){
    };

    public static final String API_URL = "http://192.168.1.130:8080/";

    public static AuthService getAuthService(Context context){
        return RetrofitClient.getClient(API_URL, context).create(AuthService.class);
    }

    public static ProjectService getProjectService(Context context){
        return RetrofitClient.getClient(API_URL, context).create(ProjectService.class);
    }
}