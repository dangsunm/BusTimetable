package com.bustime.module.route;

import com.bustime.module.Tag.Tag;
import com.bustime.module.account.Account;
import com.bustime.module.account.UserAccount;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor

@SequenceGenerator(name = "ROUTE_SEQ_GENERATOR",
        sequenceName = "ROUTE_SEQ", //매핑할 데이터베이스 시퀀스 이름
        initialValue = 1,
        allocationSize = 1)
public class BusRoute {
    @Id
    @GeneratedValue (strategy = GenerationType.TABLE,
            generator = "ROUTE_SEQ_GENERATOR")
    private Long id;

    @ManyToMany
    private Set<Account> managers = new HashSet<>();

    @ManyToMany
    private Set<Account> watchers = new HashSet<>();

    @Column(nullable = false)
    private String title; // 검색에 표출할 제목

    @Column(nullable = false)
    private String City;

    @Column(nullable = false)
    private String routeNumber;

    private String routeType;

    private String operator;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String description; // Summernote를 통해 이미지 등 게시.

    private boolean isPublished = false;

    public LocalDateTime publishedDateTime;

    @Enumerated(EnumType.STRING)
    private RouteStatus routeStatus;

    private int watcherCount;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    public void addManager(Account account) {
        this.managers.add(account);
    }

    public void addWatcher(Account account) {
        this.getWatchers().add(account);
        watcherCount = watcherCount + 1;
    }

    public void removeWatcher(Account account) {
        this.getWatchers().remove(account);
        watcherCount = watcherCount - 1;
   }

    public void publish() {
            this.isPublished = true;
            this.publishedDateTime = LocalDateTime.now();
    }

    public String path(){
        return id.toString();
    }
    public String getEncodedPath() {
        return URLEncoder.encode(this.path(), StandardCharsets.UTF_8);
    }

    public boolean isManagedBy(Account account) {
        return this.getManagers().contains(account);
    }

    public boolean isManagedBy(UserAccount userAccount) {
        return isManagedBy(userAccount.getAccount());
    }

    public boolean isMemberWatching(UserAccount userAccount) {
        return this.watchers.contains(userAccount.getAccount());
    }

}
