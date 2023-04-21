package com.bustime.module.Board;

import com.bustime.module.route.BusRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface BoardRepository extends JpaRepository<Board, Long> {
    Board getById (Long path);

    Page<Board> findByBoardType(String boardType, Pageable pageable);
}
