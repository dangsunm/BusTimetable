package com.bustime.module.route.event;

import com.bustime.module.route.BusRoute;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RouteUpdateEvent {

    private final BusRoute route;

    private final String message;

}
