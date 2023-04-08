package com.bustime.module.account.route;

import com.bustime.module.account.Account;
import com.bustime.module.account.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class RouteController {

    @GetMapping("/route/new")
    private String newRouteForm (@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new BusRouteForm());
        return "route/new";
    }
}
