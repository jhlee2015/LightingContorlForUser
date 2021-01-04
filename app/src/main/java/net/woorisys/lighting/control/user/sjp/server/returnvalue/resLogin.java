package net.woorisys.lighting.control.user.sjp.server.returnvalue;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class resLogin {

    @SerializedName("id")
    @Expose
    int id;

    @SerializedName("userId")
    @Expose
    String userId;

    @SerializedName("name")
    @Expose
    String name;

    @SerializedName("role")
    @Expose
    String role;

    @SerializedName("password")
    @Expose
    String password;

    @SerializedName("reTypePassword")
    @Expose
    String reTypePassword;

    @SerializedName("underFloorsCount")
    @Expose
    int underFloorsCount;

    @SerializedName("startFloor")
    @Expose
    int startFloor;
}
