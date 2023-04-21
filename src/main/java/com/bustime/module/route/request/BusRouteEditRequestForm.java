package com.bustime.module.route.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;

@Data
public class BusRouteEditRequestForm {

    private String description;

    private String response;

    @Enumerated(EnumType.STRING)
    private RouteEditRequestStatus status = RouteEditRequestStatus.RECIEVED;
}
