package net.woorisys.lighting.control.user.sjp;

import java.io.File;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RememberData {

    private File savefilepath = new File("NULL");

    private static final RememberData ourInstance = new RememberData();

    public static RememberData getInstance() {
        return ourInstance;
    }

    private RememberData() {
    }
}
