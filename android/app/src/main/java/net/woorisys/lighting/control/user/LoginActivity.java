package net.woorisys.lighting.control.user;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import net.woorisys.lighting.control.user.api.ApiInterface;
import net.woorisys.lighting.control.user.api.HttpClient;
import net.woorisys.lighting.control.user.domain.City;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

//    private ApiInterface api;

    @BindView(R.id.loginBtn)
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d("LoginActivity", "test");

//        api = HttpClient.getRetrofit().create(ApiInterface.class);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login() {
//        Call<City> call = api.getCityList();
//        // 비동기로 백그라운드 쓰레드로 동작
//        call.enqueue(new Callback<City>() {
//            // 통신성공 후 텍스트뷰에 결과값 출력
//            @Override
//            public void onResponse(Call<City> call, Response<City> response) {
//                System.out.println(response.body());
//            }
//
//            @Override
//            public void onFailure(Call<City> call, Throwable t) {
//                System.out.println("onFailure" );
//            }
//        });
    }
}