package net.woorisys.lighting.control.user.sjp.classmanagement;

import java.io.Serializable;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@SuppressWarnings("serial")
public class MaintenanceSetting implements Serializable {

    private String MacID;

    private String Area;

    private int Max;

    private int Min;

    private int On;

    private int Off;

    private int Maintain;

    private int Sensitivity;
}
