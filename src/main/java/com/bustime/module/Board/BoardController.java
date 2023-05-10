package com.bustime.module.Board;

import com.bustime.module.Board.comment.*;
import com.bustime.module.MessageDto;
import com.bustime.module.MessageHandler;
import com.bustime.module.account.Account;
import com.bustime.module.account.CurrentUser;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final BoardRepository boardRepository;
    private final CommentService commentService;
    private final CommentRepository commentRepository;
    private final ModelMapper modelMapper;
    private final MessageHandler messageHandler;

    @GetMapping("/board")
    private String boardList (Model model,
                              @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC)
                                Pageable pageable)
    {
        Page<Board> boardPage = boardRepository.findByBoardType("free", pageable);
        model.addAttribute("boardPage", boardPage);
        model.addAttribute("commentForm", new CommentForm());
        return "board/list";
    }



    @GetMapping("/board/{path}")
    private String viewRoute (@PathVariable String path, Model model,
                              @RequestParam(name="page", required = false) String pageN)
    {
        Board board = boardService.getBoard(path);
        model.addAttribute(board);
        model.addAttribute(path);
        if (pageN == null){
            pageN = "0";
        }
        model.addAttribute("page", pageN);
        model.addAttribute(new CommentForm());

        boardService.increaseViewCount(path);
        List<Comment> comments = commentRepository.findCommentsByBoard(board);
        model.addAttribute("comments", comments);
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
    public String modifyPostForm(@CurrentUser Account account, Model model, @PathVariable String path){
        Board post = boardService.getBoard(path);
        model.addAttribute(account);
        model.addAttribute(path);
        model.addAttribute(post);
        model.addAttribute(modelMapper.map(post, BoardForm.class));
        return "board/modify";
    }

    @PostMapping("/board/{path}/edit")
    public String updatePost(@CurrentUser Account account, @PathVariable String path,
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

    /* 댓글 관련 */
    @PostMapping("/board/{path}/new-comment")
    public String newComment (@CurrentUser Account account, @PathVariable String path, @Valid CommentForm commentForm,
                              Errors errors, Model model){

        Board post = boardService.getBoard(path);
        Long id = post.getId();
        String postId = String.valueOf(id);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "redirect:/board/" + URLEncoder.encode(postId, StandardCharsets.UTF_8);
        }

        commentService.createNewComment(modelMapper.map(commentForm, Comment.class), account, id);

        String pathURL = "/board/" + URLEncoder.encode(postId, StandardCharsets.UTF_8);
        MessageDto message = new MessageDto("댓글 등록이 완료되었습니다.", pathURL, RequestMethod.GET, null);
        return messageHandler.showMessageAndRedirect(message, model);
    }

    @PostMapping("board/{path}/commnent-delete")
    public String removeComment (@PathVariable String path, @RequestParam(name="cid") String cid, Model model){
        Board post = boardService.getBoard(path);
        Long id = post.getId();
        String postId = String.valueOf(id);

        commentService.deleteComment(Long.valueOf(cid));

        String pathURL = "/board/" + URLEncoder.encode(postId, StandardCharsets.UTF_8);
        MessageDto message = new MessageDto("댓글 삭제가 완료되었습니다.", pathURL, RequestMethod.GET, null);
        return messageHandler.showMessageAndRedirect(message, model);
    }

    // 댓글 리스트 조회
//    @GetMapping("/board/{path}/comment")
//    public List<Comment> findAllComment(@PathVariable String path) {
//        Board board = boardService.getBoard(path);
//        List<Comment> comments = commentRepository.findCommentsByBoard(board);
//
//        return comments;
//    }

    @GetMapping("/board/{path}/comment/{id}")
    public @ResponseBody CommentForm findCommentById(@PathVariable String path, @PathVariable final Long id) {
        Comment comment = commentService.findCommentById(id);
        return modelMapper.map(comment, CommentForm.class);
    }


    // 기존 댓글 수정
    @PatchMapping("/board/{path}/comment/{id}")
    public @ResponseBody Comment updateComment(@PathVariable String path, @PathVariable final Long id,
                                @RequestBody final CommentForm commentForm) {
        commentService.updateComment(commentForm, id);
        return commentService.findCommentById(id);
    }

}
