package net.woorisys.lighting.control.user.sjp.classmanagement;

import java.io.File;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("serial")
public class GroupSetting implements Serializable {

    String SectionId;

    String Transmission;

    File file;
}
