package com.identityforge.repository;

import com.identityforge.model.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    List<LoginHistory> findByUserIdOrderByLoginTimeDesc(Long userId);

    @Query(value = "SELECT lh FROM LoginHistory lh WHERE lh.user.id = :userId ORDER BY lh.loginTime DESC")
    List<LoginHistory> findLast50ByUserId(@Param("userId") Long userId);

    @Modifying
    @Query(value = "DELETE FROM login_history WHERE user_id = :userId AND id NOT IN " +
           "(SELECT id FROM (SELECT id FROM login_history WHERE user_id = :userId ORDER BY login_time DESC LIMIT 50) AS keep)",
           nativeQuery = true)
    int deleteExcessEntries(@Param("userId") Long userId);
}
