package com.psp.cbt.repository;

import com.psp.cbt.entity.ExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {
    Optional<ExamAttempt> findByStudentIdAndExamId(Long studentId, Long examId);
    List<ExamAttempt> findBySubmittedTrue();
    List<ExamAttempt> findByExamId(Long examId);
}
