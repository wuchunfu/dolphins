package com.dolphin.saas.inputs;

import lombok.Data;

@Data
public class ChangeUserInfoInputs {
    private Integer type;
    private String newPhone;
    private String code;
    private String email;
    private String uname;
    private String vertry;
}
