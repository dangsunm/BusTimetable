package com.bustime.module.route;

import com.bustime.module.account.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Transactional(readOnly = true)
public interface BusRouteRepository extends JpaRepository<BusRoute, Long> {

    BusRoute getById (Long path);

    @EntityGraph(attributePaths = "watchers")
    BusRoute findBusRouteWithWatchersById(Long path);

    @EntityGraph(attributePaths = {"watchers"})
    List<BusRoute> findByWatchersContaining(Account account);

    @EntityGraph(attributePaths = "tags")
    BusRoute findBusRouteWithTagsById(Long id);

    List<BusRoute> getBusRouteByIsNoLongerOperatingOrderByPublishedDateTimeDesc(boolean isNoLongerOperating);
}
