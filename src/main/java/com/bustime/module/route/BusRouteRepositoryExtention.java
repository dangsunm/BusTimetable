package com.bustime.module.route;

import com.bustime.module.Tag.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional(readOnly = true)
public interface BusRouteRepositoryExtention{
    Page<BusRoute> findByKeyword(String keyword, Pageable pageable);

    Page<BusRoute> findByTagName(String tag, Pageable pageable);

    public List<BusRoute> findByAccount(Set<Tag> tags);
}
