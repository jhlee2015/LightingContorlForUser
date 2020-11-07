package net.woorisys.lighting.control.user.domain;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

/**
 * 아파트 층 데이터
 *
 */
@Data
public class Floor {

    @SerializedName("id")
    @Expose
    int id;

    /** 지하 층수 명 */
    @SerializedName("name")
    @Expose
    String name;

    /** 채널 번호 */
    @SerializedName("channel")
    @Expose
    int channel;
}
