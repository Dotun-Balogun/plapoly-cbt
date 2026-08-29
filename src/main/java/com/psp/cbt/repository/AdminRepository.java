package com.psp.cbt.repository;

import com.psp.cbt.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository already gives us save(), findById(), findAll(), delete() etc.
// for free. We just add the extra lookup we need.
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsername(String username);
}
