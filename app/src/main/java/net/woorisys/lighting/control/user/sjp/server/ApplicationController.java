package net.woorisys.lighting.control.user.sjp.server;

import android.app.Application;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApplicationController extends Application {

    private NetworkService networkService;

    public NetworkService getNetworkService() {
        return networkService;
    }

    private static ApplicationController instance;

    public static ApplicationController getInstance() {
        if (instance == null)
            instance = new ApplicationController();

        return instance;
    }

    public NetworkService startApplication(String ip, String port) {
        String baseUrl = String.format("http://%s:%s/", ip, port);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        networkService = retrofit.create(NetworkService.class);

        return networkService;
    }
}
