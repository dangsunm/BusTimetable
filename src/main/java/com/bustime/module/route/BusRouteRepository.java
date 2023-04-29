package com.bustime.module.route;

import com.bustime.module.account.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface BusRouteRepository extends JpaRepository<BusRoute, Long>, BusRouteRepositoryExtention {

    //@EntityGraph(attributePaths = {"tags", "managers", "watchers"}, type = EntityGraph.EntityGraphType.LOAD)
    BusRoute getById (Long path);

    @EntityGraph(attributePaths = "watchers")
    BusRoute findBusRouteWithWatchersById(Long path);

    @EntityGraph(attributePaths = {"watchers"})
    List<BusRoute> findByWatchersContaining(Account account);

    @EntityGraph(attributePaths = {"tags", "watchers"})
    BusRoute findBusRouteWithTagsById(Long id);
    @EntityGraph(attributePaths = {"managers", "watchers"})
    BusRoute findBusRouteWithManagersAndWatchersById(Long id);

    @EntityGraph(attributePaths = {"managers"})
    BusRoute findBusRouteWithManagersById(Long id);
}
