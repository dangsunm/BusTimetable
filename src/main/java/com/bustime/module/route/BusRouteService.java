package com.bustime.module.route;

import com.bustime.module.account.Account;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BusRouteService {

    private final BusRouteRepository busRouteRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    public BusRoute createNewRoute(BusRoute route, Account account){
        BusRoute newRoute = busRouteRepository.save(route);
        newRoute.addManager(account);
        return newRoute;
    }

    public BusRoute getRoute(String path) {
        BusRoute route;
        route = this.busRouteRepository.getById(Long.parseLong(path));
        checkIfExistingRoute(route);
        return route;
    }

    public BusRoute getRouteForManager(Account account, String path) {
        BusRoute route = getRoute(path);
        checkIsManager(account,route);
        return route;
    }

    public void updateRouteInfo(BusRoute route, BusRouteForm busRouteForm) {
        modelMapper.map (route,busRouteForm);
//        eventPublisher.publishEvent(new BusRouteUpdateEvent(route, "스터디 소개를 수정했습니다."));
    }

    private void checkIsManager(Account account, BusRoute route) {
        if (!route.isManagedBy(account)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    private void checkIfExistingRoute(BusRoute route) {
        if (route == null) {
            throw new IllegalArgumentException("버스 노선이 존재하지 않습니다.");
        }
    }
}
