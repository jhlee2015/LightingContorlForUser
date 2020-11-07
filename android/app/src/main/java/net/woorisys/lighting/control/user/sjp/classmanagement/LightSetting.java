package net.woorisys.lighting.control.user.sjp.classmanagement;

import java.io.Serializable;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@SuppressWarnings("serial")
public class LightSetting implements Serializable {

    private int MaxLight;        //  최대 밝기

    private int MinLight;        //  최소 밝기

    private int MaintainLight;   //  디밍 유지

    private int OffDimmingLight; //  OFF 디밍

    private int OnDimmingLight;  //  ON 디밍

    private int SensitivityLight;//  감도수

    public LightSetting(){}
}
