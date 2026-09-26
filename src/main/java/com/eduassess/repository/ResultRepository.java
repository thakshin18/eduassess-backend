package com.eduassess.repository;

import com.eduassess.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<Result, String> {
    List<Result> findByUserId(String userId);
    List<Result> findByTestId(String testId);
    Optional<Result> findByIdAndUserId(String id, String userId);
}
