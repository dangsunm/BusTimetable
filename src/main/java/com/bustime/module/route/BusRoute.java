package com.bustime.module.route;

import com.bustime.module.account.Account;
import lombok.*;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    @Column(nullable = false)
    private String Title; // 검색에 표출할 제목

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

    private boolean isOutDated = false;

    private boolean isNoLongerOperating = false;

    public void addManager(Account account) {
        this.managers.add(account);
    }

//    public void addMember(Account account) {
//        this.getMembers().add(account);
//        memberCount = memberCount + 1;
//    }
//
//    public void removeMember(Account account) {
//        this.getMembers().remove(account);
//        memberCount = memberCount - 1;
//   }

    public String path(){
        return id.toString();
    }
    public String getEncodedPath() {
        return URLEncoder.encode(this.path(), StandardCharsets.UTF_8);
    }

    public boolean isManagedBy(Account account) {
        return this.getManagers().contains(account);
    }

}
