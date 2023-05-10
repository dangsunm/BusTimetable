package com.bustime.module.Board.comment;

import com.bustime.module.Board.BoardRepository;
import com.bustime.module.account.Account;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final ModelMapper modelMapper;

    public Comment createNewComment(Comment comment, Account account, Long id) {
        Comment newComment = commentRepository.save(comment);
        comment.setAccount(account);
        comment.setBoard(boardRepository.getById(id));
        newComment.publish();
        return newComment;
    }

    public void deleteComment(Long cid){
        commentRepository.deleteById(cid);
    }

    public void updateComment(CommentForm commentForm, Long id) {
        Comment comment = commentRepository.findCommentById(id);
        modelMapper.map(commentForm, comment);
        comment.setModifiedDate(LocalDateTime.now());
    }

    public Comment findCommentById(Long id) {
        return commentRepository.findCommentById(id);
    }
}
