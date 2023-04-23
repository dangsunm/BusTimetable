package com.bustime.module;

import com.bustime.module.route.BusRoute;
import com.bustime.module.route.BusRouteRepository;
import com.bustime.module.route.BusRouteRepositoryExtention;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final BusRouteRepositoryExtention busRouteRepository;
    @GetMapping("/search/route")
    public String searchStudy(String keyword, Model model,
                              @PageableDefault(size = 10, sort = "publishedDateTime", direction = Sort.Direction.DESC)
                              Pageable pageable) {
        Page<BusRoute> routePage = busRouteRepository.findByKeyword(keyword, pageable);
        model.addAttribute("routePage", routePage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortProperty",
                pageable.getSort().toString().contains("publishedDateTime") ? "publishedDateTime" : "memberCount");
        return "search";
    }

    @GetMapping("/search/tag/{path}")
    public String searchTag(@PathVariable String path, Model model,
                            @PageableDefault(size = 10, sort = "publishedDateTime", direction = Sort.Direction.DESC)
                        Pageable pageable){
        Page<BusRoute> routePage = busRouteRepository.findByTagName(path, pageable);
        model.addAttribute("routePage", routePage);
        model.addAttribute("keyword", path);
        model.addAttribute("sortProperty",
                pageable.getSort().toString().contains("publishedDateTime") ? "publishedDateTime" : "memberCount");
        return "search";
    }
}
