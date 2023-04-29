package com.bustime.module.route.request;

import com.bustime.module.route.BusRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface RouteEditRequestRepository extends JpaRepository<BusRouteEditRequest, Long> {

    BusRouteEditRequest getById(long path);
}
