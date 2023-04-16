package com.bustime.module.account.form;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class PasswordResetRequestForm {
    @Email
    @NotBlank
    private String email;
}
