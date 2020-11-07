package net.woorisys.lighting.control.user.api;

import net.woorisys.lighting.control.user.domain.Apartment;
import net.woorisys.lighting.control.user.domain.City;
import net.woorisys.lighting.control.user.sjp.server.LoginResult;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiInterface {

    @GET("api/city/list")
    Call<City> getCityList();

    @GET("api/apartment/list")
    Call<Apartment> getApartmentList(@Field("cityId") int cityId);

    @POST("api/login")
    Call<LoginResult> login(@Field("name") String name, @Field("password") String password);


}
