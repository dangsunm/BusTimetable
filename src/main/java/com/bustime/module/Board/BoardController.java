package com.bustime.module.Board;

import com.bustime.module.account.Account;
import com.bustime.module.account.CurrentUser;
import com.bustime.module.route.BusRoute;
import com.bustime.module.route.BusRouteForm;
import com.bustime.module.route.BusRouteService;
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
public class BoardController {
    private final BoardService boardService;
    private final ModelMapper modelMapper;

    @GetMapping("/board/{path}")
    private String viewRoute (@PathVariable String path, Model model)
    {
        Board board = boardService.getBoard(path);
        model.addAttribute(board);
        model.addAttribute(path);

        boardService.increaseViewCount(path);
        return "board/view";
    }

    @GetMapping("/new-post")
    public String newBoardForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new BoardForm());
        return "board/new";
    }

    @PostMapping("/new-post")
    public String processNewBoard(@CurrentUser Account account, @Valid BoardForm boardForm, Model model, Errors errors){
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "board/new";
        }

        Board board = boardService.createNewPost(modelMapper.map(boardForm, Board.class), account, "free");
        String boardId = String.valueOf(board.getId());
        return "redirect:/board/" + URLEncoder.encode(boardId, StandardCharsets.UTF_8);
    }

    @PostMapping("/board/{path}/delete")
    private String deleteBoard (@PathVariable String path){
        boardService.removePost(path);
        return "redirect:/";
    }

    @GetMapping("/board/{path}/edit")
    public String modifyRouteForm(@CurrentUser Account account, Model model, @PathVariable String path){
        Board post = boardService.getBoard(path);
        model.addAttribute(account);
        model.addAttribute(path);
        model.addAttribute(post);
        model.addAttribute(modelMapper.map(post, BoardForm.class));
        return "board/modify";
    }

    @PostMapping("/board/{path}/edit")
    public String updateRouteInfo(@CurrentUser Account account, @PathVariable String path,
                                  @Valid BoardForm boardForm, Errors errors,
                                  Model model, RedirectAttributes attributes) {
        Board post = boardService.getBoard(path);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(post);
            return "board/modify";
        }

        boardService.updatePost(post, boardForm);
        attributes.addFlashAttribute("message", "노선 정보를 수정했습니다.");
        String postId = String.valueOf(post.getId());
        return "redirect:/board/" + URLEncoder.encode(postId, StandardCharsets.UTF_8);
    }
}
