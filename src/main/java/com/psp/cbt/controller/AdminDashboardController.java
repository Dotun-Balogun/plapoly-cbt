package com.psp.cbt.controller;

import com.psp.cbt.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminDashboardController {

    private final StudentRepository studentRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamAttemptRepository examAttemptRepository;

    public AdminDashboardController(StudentRepository studentRepository,
                                     ExamRepository examRepository,
                                     QuestionRepository questionRepository,
                                     ExamAttemptRepository examAttemptRepository) {
        this.studentRepository = studentRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.examAttemptRepository = examAttemptRepository;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalStudents", studentRepository.count());
        model.addAttribute("totalExams", examRepository.count());
        model.addAttribute("totalQuestions", questionRepository.count());
        model.addAttribute("completedExams", examAttemptRepository.findBySubmittedTrue().size());
        return "admin/dashboard";
    }
}
