package uni.fmi.management.http;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uni.fmi.management.http.utils.AddCookiesInterceptor;
import uni.fmi.management.http.utils.ReceivedCookiesInterceptor;

public class RetrofitClient {

    private static Retrofit retrofit = null;
    Context context;

    public static Retrofit getClient(String url, Context context){
        if(retrofit == null){
            // Configuration for sending the cookies for authentication
            OkHttpClient client = new OkHttpClient(); // Configuration
            OkHttpClient.Builder builder = new OkHttpClient.Builder();// Configuration
            builder.addInterceptor(new AddCookiesInterceptor(context)); // Configuration
            builder.addInterceptor(new ReceivedCookiesInterceptor(context)); // Configuration
            client = builder.build(); // Configuration

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .create();

            retrofit = new Retrofit.Builder().baseUrl(url)
                    .client(client) // Configuration
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        return retrofit;
    }

}
