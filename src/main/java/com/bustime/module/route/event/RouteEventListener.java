package com.bustime.module.route.event;

import com.bustime.config.AppProperties;
import com.bustime.config.mail.EmailMessage;
import com.bustime.config.mail.EmailService;
import com.bustime.module.account.Account;
import com.bustime.module.account.AccountPredicates;
import com.bustime.module.account.AccountRepository;
import com.bustime.module.notification.Notification;
import com.bustime.module.notification.NotificationRepository;
import com.bustime.module.notification.NotificationType;
import com.bustime.module.route.BusRoute;
import com.bustime.module.route.BusRouteRepository;
import com.bustime.module.route.request.BusRouteEditRequest;
import com.bustime.module.route.request.RouteEditRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class RouteEventListener {

    private final BusRouteRepository routeRepository;
    private final AccountRepository accountRepository;
    private final NotificationRepository notificationRepository;
    private final RouteEditRequestRepository routeEditRequestRepository;

    @EventListener
    public void handleRouteCreatedEvent(RouteCreationEvent event){
        BusRoute route = routeRepository.findBusRouteWithTagsById(event.getRoute().getId());

        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates.findByTags(route.getTags()));
        accounts.forEach(account ->
        {
            createNotification(route, account, route.getTitle() + " 노선의 시간표가 새로 등록되었습니다.",
                        NotificationType.NEW_ROUTE);
        });
    }

    @EventListener
    public void handleRouteUpdateEvent(RouteUpdateEvent event) {
        BusRoute route = routeRepository.findBusRouteWithManagersAndWatchersById(event.getRoute().getId());
        Set<Account> accounts = new HashSet<>();
        accounts.addAll(route.getManagers());
        accounts.addAll(route.getWatchers());

        accounts.forEach(account -> {
                createNotification(route, account, event.getMessage(), NotificationType.ROUTE_UPDATED);
        });
    }

    @EventListener
    public void handleNewRouteEditRequestEvent(NewRouteEditRequestEvent event) {
        BusRoute route =  routeRepository.findBusRouteWithManagersById(event.getRoute().getId());
        BusRouteEditRequest request = routeEditRequestRepository.getById(event.getEditRequest().getId());
        Set<Account> accounts = new HashSet<>();
        accounts.addAll(route.getManagers());
        accounts.forEach(account -> {
            createNotificationForEditReq(request, route, account, event.getMessage(), NotificationType.ROUTE_UPDATED);
        });

    }

    @EventListener
    public void handleRouteEditRequestUpdateEvent(RouteEditRequestUpdateEvent event) {
        BusRouteEditRequest request = routeEditRequestRepository.getById(event.getEditRequest().getId());
        BusRoute route = routeRepository.getById(request.getRoute().getId());
        Account account = request.getAccount();
        createNotificationForEditReq(request, route, account, event.getMessage(), NotificationType.ROUTE_UPDATED);
    }

    private void createNotificationForEditReq(BusRouteEditRequest request, BusRoute route,
                                              Account account, String message, NotificationType notificationType){
        Notification notification = new Notification();
        notification.setTitle(route.getTitle());
        notification.setLink("/edit-request/" + request.getEncodedPath());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(message);
        notification.setAccount(account);
        notification.setNotificationType(notificationType);
        notificationRepository.save(notification);
    }

    private void createNotification(BusRoute route, Account account, String message, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.setTitle(route.getTitle());
        notification.setLink("/route/" + route.getEncodedPath());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(message);
        notification.setAccount(account);
        notification.setNotificationType(notificationType);
        notificationRepository.save(notification);
    }

}
