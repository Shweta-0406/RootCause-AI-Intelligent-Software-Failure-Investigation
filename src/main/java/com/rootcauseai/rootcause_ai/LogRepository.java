
package com.rootcauseai.rootcause_ai;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<Log, Long> {

    List<Log> findByMessage(String message);

    boolean existsByMessageAndTimestamp(
        String message,
        LocalDateTime timestamp
    );
}
