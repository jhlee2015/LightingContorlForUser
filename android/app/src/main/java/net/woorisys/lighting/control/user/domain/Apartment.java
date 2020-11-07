package net.woorisys.lighting.control.user.domain;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

/**
 * 아파트 단지 데이터
 * 
 */
@Data
public class Apartment {

    @SerializedName("id")
    @Expose
    int id;

    /** 단지 명 */
    @SerializedName("name")
    @Expose
    String name;

    @SerializedName("cityId")
    @Expose
    int cityId;
}
