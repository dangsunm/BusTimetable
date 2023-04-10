package com.bustime.module.route;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class BusRouteForm {

//    public static final String VALID_PATH_PATTERN = "^[ㄱ-ㅎ가-힣a-z0-9_-]{2,20}$";
//
//    @NotBlank
//    @Length(min = 2, max = 20)
//    @Pattern(regexp = VALID_PATH_PATTERN)
//    private String path;

    @NotBlank
    @Length(max = 50)
    private String title;

    @NotBlank
    @Length(max = 30)
    private String city;

    @NotBlank
    @Length(max=10)
    private String routeNumber;

    @NotBlank
    @Length(max=10)
    private String routeType;

    @NotBlank
    @Length(max=10)
    private String operator;

    @NotBlank
    private String description;

}
