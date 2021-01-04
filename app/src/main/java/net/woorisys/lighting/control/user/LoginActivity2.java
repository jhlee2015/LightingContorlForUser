package net.woorisys.lighting.control.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import net.woorisys.lighting.control.user.fragment.BaseActivity;
import net.woorisys.lighting.control.user.manager.PreferenceManager;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity2 extends AppCompatActivity {

    @BindView(R.id.citySpinner)
    Spinner citySpinner;
    @BindView(R.id.apartmentSpinner)
    Spinner apartmentSpinner;
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
        setContentView(R.layout.activity_login2);
        ButterKnife.bind(this);

        setUISetting();

        api = HttpClient.getRetrofit().create(ApiInterface.class);
        getCityList();
    }

    private void setUISetting() {
        loginButton.setOnClickListener(v -> login());

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                City city = (City) citySpinner.getItemAtPosition(position);
//                Toast.makeText(getApplicationContext(), city.getName() + "이 선택되었습니다.", Toast.LENGTH_SHORT).show();
                getApartmentList(city.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

//        apartmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(getApplicationContext(),
//                        ((Apartment) apartmentSpinner.getItemAtPosition(position)).getName() + "이 선택되었습니다.",
//                        Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) { }
//        });
    }

    private void getCityList() {
        // 비동기로 백그라운드 쓰레드로 동작
        api.getCityList().enqueue(new Callback<List<City>>() {
            @Override
            public void onResponse(Call<List<City>> call, Response<List<City>> response) {
//                Toast.makeText(getApplicationContext(), "도시 데이터 조회하였습니다.", Toast.LENGTH_SHORT).show();

                List<City> cities = response.body();
                cityAdapter = new CityAdapter(getApplicationContext(), cities);
                citySpinner.setAdapter(cityAdapter);
            }

            @Override
            public void onFailure(Call<List<City>> call, Throwable t) {
                Log.d("LoginActivity", t.getMessage());
                Toast.makeText(getApplicationContext(), "도시 데이터 조회를 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getApartmentList(int cityId) {
        api.getApartmentList(cityId).enqueue(new Callback<List<Apartment>>() {
            @Override
            public void onResponse(Call<List<Apartment>> call, Response<List<Apartment>> response) {
//                Toast.makeText(getApplicationContext(), "단지 데이터 조회하였습니다.", Toast.LENGTH_SHORT).show();

                apartmentAdapter = new ApartmentAdapter(getApplicationContext(), response.body());
                apartmentSpinner.setAdapter(apartmentAdapter);
            }

            @Override
            public void onFailure(Call<List<Apartment>> call, Throwable t) {
                Log.d("LoginActivity", t.getMessage());
                Toast.makeText(getApplicationContext(), "단지 데이터 조회를 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void login() {
        Apartment apartment = (Apartment)apartmentSpinner.getSelectedItem();
        if (apartment != null) {
            String password = passwordTxt.getText().toString();
            if (password.isEmpty()) {
                Toast.makeText(getApplicationContext(), "비밀번호를 입력하세요!", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, Object> params = new HashMap<>();
            params.put("id", apartment.getId());
            params.put("password", password);

//            api.login(params).enqueue(new Callback<Boolean>() {
//                @Override
//                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
//                    if (response.body()) {
////                        Toast.makeText(getApplicationContext(), "로그인 성공하였습니다.", Toast.LENGTH_SHORT).show();
//
//                        PreferenceManager.setLong(getApplicationContext(), "apartmentId", apartment.getId());
//                        PreferenceManager.setString(getApplicationContext(), "apartmentName", apartment.getName());
//
//                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                        startActivity(intent);
//                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
//                        finish();
//                    } else {
//                        Toast.makeText(getApplicationContext(), "비밀번호를 잘 못 입력하였습니다!", Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<Boolean> call, Throwable t) {
//                    Log.d("LoginActivity", t.getMessage());
//                    Toast.makeText(getApplicationContext(), "로그인 실패하였습니다.", Toast.LENGTH_SHORT).show();
//                }
//            });
        } else {
            Toast.makeText(getApplicationContext(), "단지를 선택하세요!", Toast.LENGTH_SHORT).show();
        }
    }
}