package com.bustime.module.route.request;

import com.bustime.module.account.Account;
import com.bustime.module.route.BusRoute;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

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
public class BusRouteEditRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE,
            generator = "ROUTE_SEQ_GENERATOR")
    private Long id;

    @ManyToOne
    @JoinColumn(name="routeId")
    private BusRoute route;

    @ManyToOne
    @JoinColumn(name="userId")
    private Account account;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime modifiedDate;

    @Enumerated(EnumType.STRING)
    private RouteEditRequestStatus status;

    private String description;

    private String response;

    public void publish() {
        this.createdDate = LocalDateTime.now();
        this.modifiedDate = LocalDateTime.now();
    }

    public String path(){
        return id.toString();
    }
}
