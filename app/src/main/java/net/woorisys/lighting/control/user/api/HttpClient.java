package net.woorisys.lighting.control.user.api;

import android.util.Log;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpClient {

    private static Retrofit retrofit;

//    private final static String IP = "192.168.219.100";
//    private final static String IP = "182.162.104.154";
    private final static String IP = "woori-test.postown.net";
    private final static String PORT = "8080";

    // Http 통신을 위한 Retrofit 객체반환
    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            String baseUrl = String.format("https://%s:%s/lighting/", IP, PORT);
//            String baseUrl = String.format("http://%s:%s/LightingControl/", IP, PORT);

            Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create());  // 받아오는 Json 구조의 데이터를 객체 형태로 변환
            retrofit = builder.build();
        }

        return retrofit;
    }
}
