package com.bustime.module.route;

import com.bustime.module.account.Account;
import com.bustime.module.account.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class RouteController {

    private final BusRouteService busRouteService;
    private final ModelMapper modelMapper;

    @GetMapping("/new-route")
    public String newRouteForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new BusRouteForm());
        return "route/new";
    }
    @PostMapping("/new-route")
    public String RegisterNewRoute (@CurrentUser Account account, @Valid BusRouteForm busRouteForm, Model model, Errors errors){
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "route/new";
        }

        BusRoute route = busRouteService.createNewRoute(modelMapper.map(busRouteForm, BusRoute.class), account);
        String routeId = route.getId().toString();
        return "redirect:/route/" + URLEncoder.encode(routeId, StandardCharsets.UTF_8);
    }

    @GetMapping("route/{path}/edit")
    public String modifyRouteForm(@CurrentUser Account account, Model model, @PathVariable String path){
        BusRoute route = busRouteService.getRouteForManager(account, path);
        model.addAttribute(account);
        model.addAttribute(path);
        model.addAttribute(route);
        model.addAttribute(modelMapper.map(route, BusRouteForm.class));
        return "route/modify";
    }

    @PostMapping("route/{path}/edit")
    public String updateRouteInfo(@CurrentUser Account account, @PathVariable String path,
                                  @Valid BusRouteForm busRouteForm, Errors errors,
                                  Model model, RedirectAttributes attributes) {
        BusRoute route = busRouteService.getRouteForManager(account, path);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(route);
            return "route/modify";
        }

        busRouteService.updateRouteInfo(route, busRouteForm);
        attributes.addFlashAttribute("message", "노선 정보를 수정했습니다.");
        String routeId = route.getId().toString();
        return "redirect:/route/" + URLEncoder.encode(routeId, StandardCharsets.UTF_8);
    }

    @GetMapping("route/{path}")
    private String viewRoute
        //(@CurrentUser Account account, @PathVariable String path, Model model) {
        (@PathVariable String path, Model model)
        {
        BusRoute busroute = busRouteService.getRoute(path);
        //model.addAttribute(account);
        model.addAttribute(busroute);
        model.addAttribute(path);
        return "route/view";
    }

    @PostMapping("route/{path}/delete")
    private String deleteRoute (@PathVariable String path){
        BusRoute route = busRouteService.getRoute(path);
        busRouteService.removeRoute(route);
        return "redirect:/";
    }
}
