package com.bustime.module.route;

import com.bustime.module.Tag.QTag;
import com.bustime.module.Tag.Tag;
import com.bustime.module.route.QBusRoute;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.Set;

public class BusRouteRepositoryExtentionImpl extends QuerydslRepositorySupport implements BusRouteRepositoryExtention{

    public BusRouteRepositoryExtentionImpl() {
        super(BusRoute.class);
    }

    @Override
    public Page<BusRoute> findByKeyword(String keyword, Pageable pageable){
        QBusRoute busRoute = QBusRoute.busRoute;
        JPQLQuery<BusRoute> query = from(busRoute).where(busRoute.isPublished.isTrue()
                        .and(busRoute.title.containsIgnoreCase(keyword))
                        .or(busRoute.City.containsIgnoreCase(keyword))
                        .or(busRoute.operator.containsIgnoreCase(keyword))
                        .or(busRoute.tags.any().title.containsIgnoreCase(keyword)))
                .leftJoin(busRoute.tags, QTag.tag).fetchJoin()
                .distinct();
        JPQLQuery<BusRoute> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<BusRoute> fetchResults = pageableQuery.fetchResults();
        return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal());
    }

    @Override
    public Page<BusRoute> findByTagName(String tag, Pageable pageable){
        QBusRoute busRoute = QBusRoute.busRoute;
        JPQLQuery<BusRoute> query = from(busRoute).where(busRoute.isPublished.isTrue()
                        .and(busRoute.tags.any().title.containsIgnoreCase(tag)))
                .leftJoin(busRoute.tags, QTag.tag).fetchJoin()
                .distinct();
        JPQLQuery<BusRoute> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<BusRoute> fetchResults = pageableQuery.fetchResults();
        return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal());
    }

    @Override
    public List<BusRoute> findByAccount(Set<Tag> tags){
        QBusRoute busRoute = QBusRoute.busRoute;
        JPQLQuery<BusRoute> query = from(busRoute).where(busRoute.isPublished.isTrue()
                        .and(busRoute.tags.any().in(tags)))
                .leftJoin(busRoute.tags, QTag.tag).fetchJoin()
                .orderBy(busRoute.publishedDateTime.desc())
                .distinct()
                .limit(10);
        return query.fetch();
    }

}
