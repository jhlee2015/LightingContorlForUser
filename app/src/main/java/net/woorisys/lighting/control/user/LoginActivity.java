package net.woorisys.lighting.control.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.JsonArray;

import net.woorisys.lighting.control.user.adapter.ApartmentAdapter;
import net.woorisys.lighting.control.user.adapter.CityAdapter;
import net.woorisys.lighting.control.user.api.ApiInterface;
import net.woorisys.lighting.control.user.api.HttpClient;
import net.woorisys.lighting.control.user.domain.Apartment;
import net.woorisys.lighting.control.user.domain.City;
import net.woorisys.lighting.control.user.domain.User;
import net.woorisys.lighting.control.user.manager.PreferenceManager;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.idTxt)
    EditText idTxt;
    @BindView(R.id.passwordTxt)
    EditText passwordTxt;
    @BindView(R.id.loginBtn)
    Button loginButton;

    private ApiInterface api;
    private CityAdapter cityAdapter;
    private ApartmentAdapter apartmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        setUISetting();

        api = HttpClient.getRetrofit().create(ApiInterface.class);
    }

    private void setUISetting() {
        String userId = PreferenceManager.getString(getApplicationContext(), "userId");
        if (userId != null) {
            idTxt.setText(userId);
        }

        loginButton.setOnClickListener(v -> login());
    }

    private void login() {
        String id = idTxt.getText().toString();
        if (id.isEmpty()) {
            Toast.makeText(getApplicationContext(), "아이디를 입력하세요!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String password = passwordTxt.getText().toString();
        if (password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "비밀번호를 입력하세요!", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("password", password);

        api.login(params).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User user = response.body();
                Log.d("LoginActivity", user + "");
                if (user != null) {
//                        Toast.makeText(getApplicationContext(), "로그인 성공하였습니다.", Toast.LENGTH_SHORT).show();

                    PreferenceManager.setString(getApplicationContext(), "userId", user.getUserId());
                    PreferenceManager.setString(getApplicationContext(), "cityName", user.getCity().getName());
                    PreferenceManager.setLong(getApplicationContext(), "apartmentId", user.getApartment().getId());
                    PreferenceManager.setString(getApplicationContext(), "apartmentName", user.getApartment().getName());

                    Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "비밀번호를 잘 못 입력하였습니다!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d("LoginActivity", t.getMessage());
                Toast.makeText(getApplicationContext(), "로그인 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}