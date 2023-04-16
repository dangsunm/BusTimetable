package com.bustime.module.Board;

import com.bustime.module.Board.comment.Comment;
import com.bustime.module.Board.comment.CommentRepository;
import com.bustime.module.account.Account;
import com.bustime.module.route.BusRoute;
import com.bustime.module.route.BusRouteForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final ModelMapper modelMapper;
    public Board createNewPost(Board board, Account account, String boardType) {
        Board newPost = boardRepository.save(board);
        board.setAccount(account);
        board.setBoardType(boardType);
        newPost.publish();

        return newPost;
    }

    public Board getBoard(String path) {
        Board post = this.boardRepository.getById(Long.parseLong(path));
        checkIfExistingPost(post);
        return post;
    }

    public void increaseViewCount(String path){
        Board post = this.boardRepository.getById(Long.parseLong(path));
        post.setView(post.getView()+1);
    }

    private void checkIfExistingPost(Board post) {
        if (post == null) {
                throw new IllegalArgumentException("게시글이 존재하지 않습니다.");
        }
    }

    public void removePost(String path) {
        Board post = this.boardRepository.getById(Long.parseLong(path));
        boardRepository.delete(post);
    }

    public void updatePost(Board post, BoardForm boardForm) {
        modelMapper.map(boardForm, post);
        post.setModifiedDate(LocalDateTime.now());
//        eventPublisher.publishEvent(new BusRouteUpdateEvent(route, "스터디 소개를 수정했습니다."));
    }

    public Comment createNewComment(Comment comment, Account account, Long id) {
        Comment newComment = commentRepository.save(comment);
        comment.setAccount(account);
        comment.setBoard(boardRepository.getById(id));
        newComment.publish();
        return newComment;
    }
}
