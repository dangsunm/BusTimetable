package com.bustime.module.route.event;

import com.bustime.module.route.BusRoute;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Getter
@RequiredArgsConstructor
public class RouteCreationEvent {

    private final BusRoute route;

}
