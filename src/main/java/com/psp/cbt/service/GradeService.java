package com.psp.cbt.service;

import org.springframework.stereotype.Service;

// A small helper that turns a percentage score into a letter grade.
// This is the ONE place you edit if you want to change the grading
// scale for the whole system.
@Service
public class GradeService {

    public String getGrade(double percentage) {
        if (percentage >= 70) return "A";
        if (percentage >= 60) return "B";
        if (percentage >= 50) return "C";
        if (percentage >= 45) return "D";
        if (percentage >= 40) return "E";
        return "F";
    }
}
