package com.psp.cbt.service;

import com.psp.cbt.entity.Student;
import org.springframework.stereotype.Service;

// Builds a matriculation number from a student's academic details,
// e.g. PSP/ICT/CSE/ND/24/0004
// "PSP" is the only fixed part (the institution running this system).
@Service
public class MatricNumberService {

    private static final String INSTITUTION = "PSP";

    public String build(Student student) {
        return String.join("/",
                INSTITUTION,
                student.getSchool(),
                student.getDepartment(),
                student.getProgramme(),
                student.getAdmissionYear(),
                student.getSerialNumber());
    }
}
