package net.woorisys.lighting.control.user.dongle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import net.woorisys.lighting.control.user.R;
import net.woorisys.lighting.control.user.search.SearchActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import androidx.appcompat.app.AppCompatActivity;

public class ConnectDongleActivity extends AppCompatActivity {

    @BindView(R.id.connect_dongle_btn)
    TextView connect_dongle_btn;

    // @BindView(R.id.progressBar)
    // CircleProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_dongle);
        ButterKnife.bind(ConnectDongleActivity.this);

        //progressBar.setVisibility(View.GONE);
    }

    @OnClick({R.id.connect_dongle_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.connect_dongle_btn:
                //API 주소
                //https://github.com/emre1512/CircleProgressBar
                //현재 프로세스에 맞게 setProgress 및 setText 를 변경한다.
                //progressBar.setProgress(100);
                //progressBar.setText("100");
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
}
