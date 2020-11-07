package net.woorisys.lighting.control.user.domain;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

/**
 * 시, 구 도시 데이터
 *
 */
@Data
public class City {

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("name")
    @Expose
    private String name;
}
