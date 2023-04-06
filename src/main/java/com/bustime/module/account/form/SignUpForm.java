package com.bustime.module.account.form;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

@Data
public class SignUpForm {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Length(min = 3, max = 20)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9_-]{3,20}$")
    private String username;

    @NotBlank
    @Length(min = 8, max = 50)
    private String password;
}
