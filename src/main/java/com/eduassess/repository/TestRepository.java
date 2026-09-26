package com.eduassess.repository;

import com.eduassess.entity.Test;
import com.eduassess.entity.TestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestRepository extends JpaRepository<Test, String> {
    List<Test> findByStatus(TestStatus status);
    Optional<Test> findByTitle(String title);
}
