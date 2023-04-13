package com.bustime.module.Board;

import com.bustime.module.Board.comment.Comment;
import com.bustime.module.account.Account;
import com.bustime.module.account.UserAccount;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import javax.swing.*;
import java.time.LocalDateTime;
import java.util.List;

@SequenceGenerator(name = "BOARD_SEQ_GENERATOR",
        sequenceName = "BOARD_SEQ", //매핑할 데이터베이스 시퀀스 이름
        initialValue = 1,
        allocationSize = 1)

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Board {

    @Id
    @GeneratedValue (strategy = GenerationType.TABLE,
            generator = "BOARD_SEQ_GENERATOR")
    private long id;

    @ManyToOne // Many = Board, User = One 한명의 유저는 여러개의 게시글을 쓸 수 있다.
    @JoinColumn(name="userId") // foreign key (userId) references User (id)
    private Account account;

    @Column(length = 500, nullable = false)
    private String title;

    private String boardType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "integer default 0")
    private int view;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime modifiedDate;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @OrderBy("id asc") // 댓글 정렬
    private List<Comment> comments;

    public void publish() {
        this.createdDate = LocalDateTime.now();
        this.modifiedDate = LocalDateTime.now();
    }

    public boolean isAuthor(UserAccount userAccount) {
        return this.account.getUsername().equals(userAccount.getUsername());
    }
}
