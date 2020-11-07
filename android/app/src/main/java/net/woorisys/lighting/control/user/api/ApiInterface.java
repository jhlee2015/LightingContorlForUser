package net.woorisys.lighting.control.user.api;

import net.woorisys.lighting.control.user.domain.Apartment;
import net.woorisys.lighting.control.user.domain.City;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("api/city/list")
    Call<List<City>> getCityList();

    @GET("api/apartment/list")
    Call<List<Apartment>> getApartmentList(@Query(value = "cityId", encoded = true) int cityId);

    @FormUrlEncoded
    @POST("api/login")
    Call<Boolean> login(@FieldMap HashMap<String, Object> param);
}
