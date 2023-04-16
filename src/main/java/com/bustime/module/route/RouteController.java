package com.bustime.module.route;

import com.bustime.module.Tag.Tag;
import com.bustime.module.Tag.TagForm;
import com.bustime.module.Tag.TagRepository;
import com.bustime.module.Tag.TagService;
import com.bustime.module.account.Account;
import com.bustime.module.account.CurrentUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class RouteController {

    private final BusRouteService busRouteService;
    private final BusRouteRepository busRouteRepository;
    private final TagService tagService;
    private final TagRepository tagRepository;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    @GetMapping ("/route/list")
    public String viewList (@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        String searchCondition = "normal";
        model.addAttribute(searchCondition);
        return "route/list";
    }

    //@PostMapping ("/route/list")
//    public String search_from_list

    @GetMapping("/new-route")

    public String newRouteForm(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        model.addAttribute(new BusRouteForm());

        List<String> allWL = tagRepository.findAll().stream().map(Tag::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allWL));

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

    @GetMapping("/route/{path}/edit")
    public String modifyRouteForm(@CurrentUser Account account, Model model, @PathVariable String path){
        BusRoute route = busRouteService.getRouteForManager(account, path);
        model.addAttribute(account);
        model.addAttribute(path);
        model.addAttribute(route);
        model.addAttribute(modelMapper.map(route, BusRouteForm.class));
        return "route/modify";
    }

    @PostMapping("/route/{path}/edit")
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

    @GetMapping("/route/{path}")
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

    @PostMapping("/route/{path}/delete")
    private String deleteRoute (@PathVariable String path){
        BusRoute route = busRouteService.getRoute(path);
        busRouteService.removeRoute(route);
        return "redirect:/";
    }

    @GetMapping("/route/{path}/addWatchList")
    public String addWatchList(@CurrentUser Account account, @PathVariable String path) {
        BusRoute route = busRouteRepository.findBusRouteWithWatchersById(Long.parseLong(path));
        busRouteService.addWatcher(route, account);
        return "redirect:/route/" + route.getEncodedPath();
    }

    @GetMapping("/route/{path}/removeWatchList")
    public String removeWatchList(@CurrentUser Account account, @PathVariable String path) {
        BusRoute route = busRouteRepository.findBusRouteWithWatchersById(Long.parseLong(path));
        busRouteService.removeWatcher(route, account);
        return "redirect:/route/" + route.getEncodedPath();
    }


    /* Tag 관련 추가 및 삭제 */
    @GetMapping("/route/{path}/tags")
    public String updateTags(@PathVariable String path, Model model) throws JsonProcessingException {
        Set<Tag> tags = busRouteService.getTags(path);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return "/route/tags";
    }

    @PostMapping("/route/{path}/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@PathVariable String path, @RequestBody TagForm tagForm) {
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        busRouteService.addTag(path, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/route/{path}/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentUser Account account, @PathVariable String path, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        busRouteService.removeTag(path, tag);
        return ResponseEntity.ok().build();
    }

}
