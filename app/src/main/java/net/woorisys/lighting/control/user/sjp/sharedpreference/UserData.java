package net.woorisys.lighting.control.user.sjp.sharedpreference;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class UserData {

    @Getter
    @Setter
    String Id;
    String Password;
}
