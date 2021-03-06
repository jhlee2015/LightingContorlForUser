package net.woorisys.lighting.control.user.sjp.server;

import net.woorisys.lighting.control.user.sjp.server.returnvalue.resLogin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResult {

    public static final int RETURN_CODE_SUCCESS = 0;
    public static final int RETURN_CODE_FAIL = -1;

    private int returnCode;

    private String message;

    private resLogin result = null;

    public LoginResult() {
        this.returnCode = RETURN_CODE_SUCCESS;
    }

    public LoginResult(int returnCode) {
        this.returnCode = returnCode;
    }

    public LoginResult(resLogin object) {
        this.result = object;
    }

    public LoginResult(int returnCode, resLogin object) {
        this(returnCode);
        this.result = object;
    }
}

