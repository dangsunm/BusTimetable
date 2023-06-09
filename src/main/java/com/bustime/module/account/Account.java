package com.bustime.module.account;

import com.bustime.module.Tag.Tag;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
@SequenceGenerator(name = "ROUTE_SEQ_GENERATOR",
        sequenceName = "ROUTE_SEQ", //매핑할 데이터베이스 시퀀스 이름
        initialValue = 1,
        allocationSize = 1)
public class Account {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String username;

    private String password;

    private String usertype = "A";

    private LocalDateTime joinedAt;

    private boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime emailCheckTokenGeneratedAt;

    private String passwordResetToken;

    private LocalDateTime passwordResetTokenGeneratedAt;

    @ColumnDefault("true")
    private boolean EmailNotification;

    @ColumnDefault("true")
    private boolean NewRouteNotification;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }
    public boolean canSendConfirmEmail() {
        return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }

    public void generatePasswordRestToken() {
        this.passwordResetToken = UUID.randomUUID().toString();
        this.passwordResetTokenGeneratedAt = LocalDateTime.now();
    }

    public boolean canSendPasswordResetToken() {
        return this.passwordResetTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }

    public boolean validResetPasswordTiming() {
        return this.passwordResetTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(72));
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isModOrAdmin(Account account){
       return (this.usertype.equals("A") || this.usertype.equals("M"));
    }

}
