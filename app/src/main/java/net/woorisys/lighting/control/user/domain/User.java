package net.woorisys.lighting.control.user.domain;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

/**
 * 유저 데이터
 */
@Data
public class User {

    @SerializedName("id")
    @Expose
    private long id;

    @SerializedName("userId")
    @Expose
    private String userId;

    @SerializedName("password")
    @Expose
    private int password;

    @SerializedName("city")
    @Expose
    private City city;

    @SerializedName("apartment")
    @Expose
    private Apartment apartment;
}
