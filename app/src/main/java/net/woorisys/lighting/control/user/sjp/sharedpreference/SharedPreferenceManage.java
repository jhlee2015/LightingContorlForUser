package net.woorisys.lighting.control.user.sjp.sharedpreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPreferenceManage {

    private String TAG = "SJP_SharedPreference_Tag";

    private String SharedPreferenceName = "UserData"; //  SharedPreference Login 정보 기억하는 Object 이름
    private String UserId = "UserId";
    private String UserPassword = "UserPassword";

    private Context context;

    private String id;              //  사용자가 가장 마지막에 입력 하였던 Id
    private String password;        //  사용자가 가장 마지막에 입력 하였던 Password

    private SharedPreferences sharedPreferences;

    public SharedPreferenceManage(Context context) {
        this.context = context;
    }

    public void WriteSharedPreference(UserData userData) {
        id = userData.getId();
        password = userData.getPassword();

        sharedPreferences = context.getSharedPreferences(SharedPreferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(UserId, userData.getId());
        editor.putString(UserPassword, userData.getPassword());
        editor.commit();

        Log.d(TAG, "Write SharedPreference Login : " + id + " / " + password);
    }

    public UserData ReadSharedPreference() {
        UserData userData = new UserData();

        sharedPreferences = context.getSharedPreferences(SharedPreferenceName, Context.MODE_PRIVATE);
        id = sharedPreferences.getString(UserId, "");
        password = sharedPreferences.getString(UserPassword, "");

        userData.setId(id);
        userData.setPassword(password);

        Log.d(TAG, "Write SharedPreference Login : " + id + " / " + password);

        return userData;
    }
}
