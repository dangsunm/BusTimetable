package com.bustime.module.account.route;

import com.bustime.module.account.Account;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusRoute {
    @Id
    @GeneratedValue
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

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String description; // Summernote를 통해 이미지 등 게시.

    private boolean isPublished = false;

    private boolean isOutDated = false;

    private boolean isNoLongerOperating = false;
}
