package com.bustime.module.notification;

import com.bustime.module.account.Account;
import com.bustime.module.account.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository repository;

    private final NotificationService service;

    @GetMapping("/notifications")
    public String getNotifications(@CurrentUser Account account, Model model) {
        List<Notification> notifications = repository.findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, false);
        long numberOfChecked = repository.countByAccountAndChecked(account, true);
        putCategorizedNotifications(model, notifications, numberOfChecked, notifications.size());
        model.addAttribute("isNew", true);
        service.markAsRead(notifications);
        return "notification/list";
    }

    @GetMapping("/notifications/old")
    public String getOldNotifications(@CurrentUser Account account, Model model) {
        List<Notification> notifications = repository.findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, true);
        long numberOfNotChecked = repository.countByAccountAndChecked(account, false);
        putCategorizedNotifications(model, notifications, notifications.size(), numberOfNotChecked);
        model.addAttribute("isNew", false);
        return "notification/list";
    }

    @DeleteMapping("/notifications")
    public String deleteNotifications(@CurrentUser Account account) {
        repository.deleteByAccountAndChecked(account, true);
        return "redirect:/notifications";
    }

    private void putCategorizedNotifications(Model model, List<Notification> notifications,
                                             long numberOfChecked, long numberOfNotChecked) {

        List<Notification> newRouteNotifications = new ArrayList<>();
        List<Notification> watchingRouteNotifications = new ArrayList<>();
        List<Notification> newEditRequestNotifications = new ArrayList<>();
        List<Notification> editRequestUpdateNotifications = new ArrayList<>();

        for (var notification : notifications) {
            switch (notification.getNotificationType()) {
                case NEW_ROUTE: newRouteNotifications.add(notification); break;
                case NEW_REQUEST: newEditRequestNotifications.add(notification); break;
                case ROUTE_UPDATED: watchingRouteNotifications.add(notification); break;
                case REQUEST_UPDATED: editRequestUpdateNotifications.add(notification); break;
            }
        }

        model.addAttribute("numberOfNotChecked", numberOfNotChecked);
        model.addAttribute("numberOfChecked", numberOfChecked);
        model.addAttribute("notifications", notifications);
        model.addAttribute("newRouteNotifications", newRouteNotifications);
        model.addAttribute("newEditRequestNotifications", newEditRequestNotifications);
        model.addAttribute("watchingRouteNotifications", watchingRouteNotifications);
        model.addAttribute("editRequestUpdateNotifications", editRequestUpdateNotifications);
    }

}
