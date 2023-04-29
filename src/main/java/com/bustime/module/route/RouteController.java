package com.bustime.module.route;

import com.bustime.module.Tag.Tag;
import com.bustime.module.Tag.TagForm;
import com.bustime.module.Tag.TagRepository;
import com.bustime.module.Tag.TagService;
import com.bustime.module.account.Account;
import com.bustime.module.account.CurrentUser;
import com.bustime.module.notification.NotificationRepository;
import com.bustime.module.route.request.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
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
    private final RouteEditRequestRepository routeEditRequestRepository;
    private final NotificationRepository notificationRepository;
    private final TagService tagService;
    private final TagRepository tagRepository;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    @GetMapping ("/route")
    public String viewRouteList (Model model,
                                 @PageableDefault(size = 10,
                                         sort = "publishedDateTime",
                                         direction = Sort.Direction.DESC)
                                 Pageable pageable)
    {
        Page<BusRoute> routePage = busRouteRepository.findAll(pageable);
        model.addAttribute("routePage", routePage);
        model.addAttribute("count", busRouteService.getCount());

        String searchCondition = "normal";
        model.addAttribute(searchCondition);

        return "route/list";
    }

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
    private String viewRoute (@PathVariable String path, Model model,
            @RequestParam(name="page", required = false) String pageN)
    {
        BusRoute busroute = busRouteService.getRoute(path);
        //model.addAttribute(account);

        model.addAttribute(busroute);
        model.addAttribute(path);

        if (pageN == null){
            pageN = "0";
        }
        model.addAttribute("page", pageN);

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
    public String updateTagsForm(@PathVariable String path, Model model) throws JsonProcessingException {
        Set<Tag> tags = busRouteService.getTags(path);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));
        model.addAttribute("route", busRouteService.getRoute(path));

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

    /* 노선내 수정 요청과 관련한 Mapping */
    @GetMapping("/route/{path}/request-edit")
    public String EditRequestForm(@CurrentUser Account account, Model model, @PathVariable String path){
        BusRoute route = busRouteService.getRoute(path);
        model.addAttribute(account);
        model.addAttribute(path);
        model.addAttribute(route);
        model.addAttribute(modelMapper.map(route, BusRouteEditRequestForm.class));
        return "route/request/modifyRequest";
    }

    @PostMapping("/route/{path}/request-edit")
    public String sendEditRequest(@CurrentUser Account account, @PathVariable String path,
                                  @Valid BusRouteEditRequestForm form , Errors errors,
                                  Model model, RedirectAttributes attributes) {
        BusRoute route = busRouteService.getRoute(path);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(route);
            return "route/request/modifyRequest";
        }

        busRouteService.RequestUpdateRoute(modelMapper.map(form, BusRouteEditRequest.class), account, route);
        attributes.addFlashAttribute("message", "노선 수정 요청을 전송했습니다.");
        String routeId = route.getId().toString();


        return "redirect:/route/" + URLEncoder.encode(routeId, StandardCharsets.UTF_8);
    }

    @GetMapping("/edit-request")
    public String editRequestList(Model model,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC)
            Pageable pageable)
    {
        Page<BusRouteEditRequest> routeReqPage = routeEditRequestRepository.findAll(pageable);
        model.addAttribute("routeReqPage", routeReqPage);

        return "route/request/requestList";
    }

    @GetMapping("/edit-request/{path}")
    public String viewEditRequest(Model model, @PathVariable String path, @RequestParam(name="page", required = false) String pageN)
    {
        BusRouteEditRequest request = busRouteService.getEditRequest(path);
        model.addAttribute("request", request);
        model.addAttribute(path);

        if (pageN == null){
            pageN = "0";
        }
        model.addAttribute("page", pageN);

        return "route/request/requestDetails";
    }

    @GetMapping("/edit-request/{path}/response")
    public String responseRequestForm(@CurrentUser Account account, Model model, @PathVariable String path)
    {
        BusRouteEditRequest request = busRouteService.getEditRequest(path);
        request.getRoute().isManagedBy(account);
        model.addAttribute("request", request);
        model.addAttribute(new BusRouteEditRequestForm());
        model.addAttribute(path);
        model.addAttribute(modelMapper.map(request, BusRouteEditRequestForm.class));

        return "route/request/handleRequest";
    }

    @PostMapping("/edit-request/{path}/response")
    public String responseEditRequest (@CurrentUser Account account, @PathVariable String path,
                                        @Valid BusRouteEditRequestForm form , Errors errors, Model model) {
        BusRouteEditRequest request = busRouteService.getEditRequest(path);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(request);
            return "route/request/handleRequest";
        }

        busRouteService.updateRouteProcessUpdate(form, request);
        String requestId = request.getId().toString();

        return "redirect:/edit-request/" + URLEncoder.encode(requestId, StandardCharsets.UTF_8);
    }

    @PostMapping("/edit-request/{path}/delete")
    public String removeEditRequest (@CurrentUser Account account, @PathVariable String path){
        BusRouteEditRequest request = busRouteService.getEditRequest(path);
        busRouteService.removeEditRequest(request);
        return "redirect:/edit-request";
    }
}
