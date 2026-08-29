package com.psp.cbt.repository;

import com.psp.cbt.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByStatus(Exam.Status status);
}
