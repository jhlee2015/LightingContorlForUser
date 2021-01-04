package net.woorisys.lighting.control.user.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class Temp implements Serializable {

    private String text;

    public Temp(String text) {
        this.text = text;
    }
}
