package com.psp.cbt.controller;

import com.psp.cbt.entity.Exam;
import com.psp.cbt.repository.CourseRepository;
import com.psp.cbt.repository.ExamRepository;
import com.psp.cbt.repository.QuestionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/exams")
public class AdminExamController {

    private final ExamRepository examRepository;
    private final CourseRepository courseRepository;
    private final QuestionRepository questionRepository;

    public AdminExamController(ExamRepository examRepository,
                                CourseRepository courseRepository,
                                QuestionRepository questionRepository) {
        this.examRepository = examRepository;
        this.courseRepository = courseRepository;
        this.questionRepository = questionRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("exams", examRepository.findAll());
        // used by the template to show "12 questions added" per exam
        model.addAttribute("questionRepository", questionRepository);
        return "admin/exams";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("exam", new Exam());
        model.addAttribute("courses", courseRepository.findAll());
        return "admin/exam-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("exam", examRepository.findById(id).orElseThrow());
        model.addAttribute("courses", courseRepository.findAll());
        return "admin/exam-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Exam exam, @RequestParam Long courseId) {
        exam.setCourse(courseRepository.findById(courseId).orElseThrow());
        examRepository.save(exam);
        return "redirect:/admin/exams";
    }

    @PostMapping("/{id}/toggle")
    public String toggleStatus(@PathVariable Long id) {
        Exam exam = examRepository.findById(id).orElseThrow();
        exam.setStatus(exam.getStatus() == Exam.Status.ACTIVE ? Exam.Status.INACTIVE : Exam.Status.ACTIVE);
        examRepository.save(exam);
        return "redirect:/admin/exams";
    }
}
