package com.bustime.module.route.event;

import com.bustime.module.route.BusRoute;
import com.bustime.module.route.request.BusRouteEditRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RouteEditRequestUpdateEvent {

    private final BusRouteEditRequest editRequest;

    private final String message;
}
