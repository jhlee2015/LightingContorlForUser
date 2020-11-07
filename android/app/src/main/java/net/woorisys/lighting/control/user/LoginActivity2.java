package net.woorisys.lighting.control.user;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.woorisys.lighting.control.user.fragment.BaseActivity;
import net.woorisys.lighting.control.user.sjp.server.ResultType;
import net.woorisys.lighting.control.user.sjp.server.ServerRequest;
import net.woorisys.lighting.control.user.sjp.server.ServerRequestResult;
import net.woorisys.lighting.control.user.sjp.sharedpreference.SharedPreferenceManage;
import net.woorisys.lighting.control.user.sjp.sharedpreference.UserData;

import butterknife.BindView;
import butterknife.ButterKnife;

// BroadCastReceiver 로 연결 여부 확인
// true 일 경우 연결 - 로그인 가능
// false 일 경우 연결 X - 로그인 불가능
public class LoginActivity2 extends AppCompatActivity implements ServerRequestResult {

    private String TAG = "SJP_LoginActivity_Tag";

    InputMethodManager controlManager;

    @BindView(R.id.loginForm)
    LinearLayout loginForm;
    @BindView(R.id.loginEdits)
    LinearLayout loginEdits;
    @BindView(R.id.light_image)
    ImageView lightImage;
    @BindView(R.id.user_id)
    EditText userId;
    @BindView(R.id.user_password)
    EditText userPassword;
    @BindView(R.id.error_message)
    TextView errorMessageView;
    @BindView(R.id.submit)
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        ButterKnife.bind(LoginActivity2.this);

        controlManager = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);

        SharedPreferenceManage sharedPreferenceManage = new SharedPreferenceManage(getApplicationContext());
        UserData userData = sharedPreferenceManage.ReadSharedPreference();

        userId.setText(userData.getId());
        userPassword.setText(userData.getPassword());

        if (userData.getId().isEmpty() || userData.getPassword().isEmpty()) {
            keyBoardUp();
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loginEdits.setGravity(Gravity.BOTTOM);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(broadcastReceiver);
    }

    public void keyBoardUp() {
        userId.requestFocus();
        controlManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        loginEdits.setGravity(Gravity.NO_GRAVITY);

        Log.d(TAG, "MainActivity onViewClicked keyBoardUp");
    }

    // 로그인 버튼 클릭시
    private void Login() {
        ServerRequest serverRequest = new ServerRequest(getApplicationContext());
        serverRequest.setListener(this);
        serverRequest.Login(userId.getText().toString(), userPassword.getText().toString());
    }

    @Override
    public boolean Result(ResultType resultType, boolean result, String reason) {
        Log.d(TAG, "RESULT TYPE : " + resultType + " , RESULT : " + result + " , RESON : " + reason);

        if (!result) {
            errorMessageView.setVisibility(View.VISIBLE);
            errorMessageView.setText(reason);
        } else {
            Intent SearchActivity = new Intent(this, BaseActivity.class);
            startActivity(SearchActivity);

            UserData userData = new UserData();
            userData.setId(userId.getText().toString());
            userData.setPassword(userPassword.getText().toString());

            SharedPreferenceManage sharedPreferenceManage = new SharedPreferenceManage(getApplicationContext());
            sharedPreferenceManage.WriteSharedPreference(userData);

            finish();
            return true;
        }

        return false;
    }
}
