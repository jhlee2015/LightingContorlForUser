package net.woorisys.lighting.control.user.sjp.server;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface NetworkService {

    @FormUrlEncoded
    @POST("/onepass/rest/login")
    Call<LoginResult> Login(@Field("userId") String userId, @Field("password") String password);

}
