package net.woorisys.lighting.control.user;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import net.woorisys.lighting.control.user.manager.PreferenceManager;


public class SplashActivity extends AppCompatActivity {

    TextView nameText;
    ImageView lightImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
//        ButterKnife.bind(SplashActivity.this);

        /* 레이아웃 지정 */
        nameText = findViewById(R.id.nameText);
        lightImage = findViewById(R.id.light);

        String cityName = PreferenceManager.getString(getApplicationContext(), "cityName");
        String apartmentName = PreferenceManager.getString(getApplicationContext(), "apartmentName");

        nameText.setText(cityName + " " + apartmentName);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    lightImage.setImageDrawable(getResources().getDrawable(R.drawable.bulb_on_set_with_app_name, getApplicationContext().getTheme()));
                } else {
                    lightImage.setImageDrawable(getResources().getDrawable(R.drawable.bulb_on_set_with_app_name));
                }
            }
        }, 3000);// 0.5초 정도 딜레이를 준 후 시작

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                finish();
            }
        }, 3000);// 0.5초 정도 딜레이를 준 후 시작
    }
}