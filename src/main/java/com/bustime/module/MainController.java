package com.bustime.module;

import com.bustime.module.account.Account;
import com.bustime.module.account.CurrentUser;
import com.bustime.module.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final NotificationRepository notificationRepository;

    @GetMapping("/")
    public String indexPage (@CurrentUser Account account, Model model) {
        if (account != null) {
            model.addAttribute(account);
            model.addAttribute("nCount", notificationRepository.countByAccountAndChecked(account, false));
        }
        return "index";
    }


    @GetMapping("/login")
    public String loginForm(
            HttpServletRequest request,
            Model model) {

        /**
         * 이전 페이지로 되돌아가기 위한 Referer 헤더값을 세션의 prevPage attribute로 저장
         */
        String uri = request.getHeader("Referer");
        if (uri != null && !uri.contains("/login")) {
            request.getSession().setAttribute("prevPage", uri);
        }
        return "login";
    }
}
