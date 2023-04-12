package com.bustime.module.route;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface BusRouteRepository extends JpaRepository<BusRoute, Long> {

    BusRoute getById (Long path);

    @EntityGraph(attributePaths = "watchers")
    BusRoute findBusRouteWithWatchersById(Long path);

    @EntityGraph(attributePaths = "tags")
    BusRoute findBusRouteWithTagsById(Long id);
}
