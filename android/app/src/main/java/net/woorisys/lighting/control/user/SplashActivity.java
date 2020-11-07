package net.woorisys.lighting.control.user;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import net.woorisys.lighting.control.user.fragment.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity {

    @BindView(R.id.light)
    ImageView lightImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(SplashActivity.this);

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
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                finish();
            }
        }, 3000);// 0.5초 정도 딜레이를 준 후 시작
    }
}