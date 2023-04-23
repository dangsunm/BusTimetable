package com.bustime.module.Board;

import com.bustime.module.Board.comment.CommentForm;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class NoticeController {

    private BoardRepository boardRepository;
//    @GetMapping("/notice")
//    private String noticeList (Model model,
//                               @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC)
//                               Pageable pageable)
//    {
//        Page<Board> boardPage = boardRepository.findByBoardType("notice", pageable);
//        model.addAttribute("boardPage", boardPage);
//        model.addAttribute("commentForm", new CommentForm());
//        return "board/list";
//    }
}
