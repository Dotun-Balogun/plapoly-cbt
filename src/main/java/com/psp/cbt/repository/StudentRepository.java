package com.psp.cbt.repository;

import com.psp.cbt.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByMatricNumber(String matricNumber);

    List<Student> findByFullNameContainingIgnoreCaseOrMatricNumberContainingIgnoreCase(
            String name, String matric);
}
