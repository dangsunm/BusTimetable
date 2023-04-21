package com.bustime.module.Board.comment;

import com.bustime.module.Board.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findCommentsByBoard (Board board);
}
