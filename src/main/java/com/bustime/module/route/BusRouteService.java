package com.bustime.module.route;

import com.bustime.module.Tag.Tag;
import com.bustime.module.account.Account;
import com.bustime.module.route.event.NewRouteEditRequestEvent;
import com.bustime.module.route.event.RouteCreationEvent;
import com.bustime.module.route.event.RouteEditRequestUpdateEvent;
import com.bustime.module.route.event.RouteUpdateEvent;
import com.bustime.module.route.request.BusRouteEditRequest;
import com.bustime.module.route.request.BusRouteEditRequestForm;
import com.bustime.module.route.request.RouteEditRequestRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class BusRouteService {

    private final BusRouteRepository busRouteRepository;
    private final RouteEditRequestRepository routeEditRequestRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    public BusRoute createNewRoute(BusRoute route, Account account){
        BusRoute newRoute = busRouteRepository.save(route);
        newRoute.addManager(account);
        newRoute.publish();
        this.eventPublisher.publishEvent(new RouteCreationEvent(route));
        return newRoute;
    }

    public BusRoute getRoute(String path) {
        BusRoute route;
        route = this.busRouteRepository.getById(Long.parseLong(path));
        checkIfExistingRoute(route);
        return route;
    }

    public BusRouteEditRequest getEditRequest(String path) {
        return this.routeEditRequestRepository.getById(Long.parseLong(path));

    }

    public BusRoute getRouteForManager(Account account, String path) {
        BusRoute route = getRoute(path);
        checkIsManager(account,route);
        return route;
    }

    public void updateRouteInfo(BusRoute route, BusRouteForm busRouteForm) {
        modelMapper.map(busRouteForm, route);
        route.publish();
        eventPublisher.publishEvent(new RouteUpdateEvent(route, "노선의 새로운 시간표가 있습니다. 확인해보세요."));
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

    public void removeRoute(BusRoute route) {
        if (route == null) {
            throw new IllegalArgumentException("버스 노선이 존재하지 않습니다.");
        }
        busRouteRepository.delete(route);
    }

    public void addWatcher(BusRoute route, Account account) {
        route.addWatcher(account);
    }

    public void removeWatcher(BusRoute route, Account account) {
        route.removeWatcher(account);
    }

    public void addTag(String path, Tag tag) {
        Optional<BusRoute> byId = busRouteRepository.findById(Long.parseLong(path));
        byId.ifPresent(a -> a.getTags().add(tag));
    }

    public Set<Tag> getTags(String path) {
        Optional<BusRoute> byId = busRouteRepository.findById(Long.parseLong(path));
        return byId.orElseThrow().getTags();
    }

    public void removeTag(String path, Tag tag) {
        busRouteRepository.findById(Long.parseLong(path))
                .map(BusRoute::getTags)
                .ifPresent(tags -> tags.remove(tag));
    }

    public BusRoute getRouteToUpdate(Account account, String path) {
        BusRoute route = busRouteRepository.findBusRouteWithTagsById(Long.parseLong(path));
        checkIfExistingRoute(route);
        checkIfManager(account, route);
        return route;
    }

    private void checkIfManager(Account account, BusRoute route) {
        if (!route.isManagedBy(account)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    public void RequestUpdateRoute(BusRouteEditRequest request, Account account, BusRoute route) {
        request.setAccount(account);
        request.setRoute(route);
        request.publish();
        routeEditRequestRepository.save(request);
        eventPublisher.publishEvent(new NewRouteEditRequestEvent(route, request, "새로운 노선 수정 요청이 등록되었습니다."));
    }

    public void updateRouteProcessUpdate(@Valid BusRouteEditRequestForm form, BusRouteEditRequest request) {
        modelMapper.map(form, request);
        eventPublisher.publishEvent(new RouteEditRequestUpdateEvent(request, "노선 수정 요청에 답변이 등록되었습니다."));
    }

    public void removeEditRequest(BusRouteEditRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("해당 수정 요청이 존재하지 않습니다.");
        }
        routeEditRequestRepository.delete(request);
    }

    public int getCount() {
        return (int) busRouteRepository.count();
    }
}
