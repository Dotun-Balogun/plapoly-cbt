package com.psp.cbt.controller;

import com.psp.cbt.entity.Exam;
import com.psp.cbt.entity.Question;
import com.psp.cbt.repository.ExamRepository;
import com.psp.cbt.repository.QuestionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/exams/{examId}/questions")
public class AdminQuestionController {

    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;

    public AdminQuestionController(QuestionRepository questionRepository, ExamRepository examRepository) {
        this.questionRepository = questionRepository;
        this.examRepository = examRepository;
    }

    @GetMapping
    public String list(@PathVariable Long examId, Model model) {
        Exam exam = examRepository.findById(examId).orElseThrow();
        model.addAttribute("exam", exam);
        model.addAttribute("questions", questionRepository.findByExamId(examId));
        return "admin/questions";
    }

    @GetMapping("/new")
    public String newForm(@PathVariable Long examId, Model model) {
        model.addAttribute("exam", examRepository.findById(examId).orElseThrow());
        model.addAttribute("question", new Question());
        return "admin/question-form";
    }

    @GetMapping("/{questionId}/edit")
    public String editForm(@PathVariable Long examId, @PathVariable Long questionId, Model model) {
        model.addAttribute("exam", examRepository.findById(examId).orElseThrow());
        model.addAttribute("question", questionRepository.findById(questionId).orElseThrow());
        return "admin/question-form";
    }

    @PostMapping("/save")
    public String save(@PathVariable Long examId, @ModelAttribute Question question) {
        question.setExam(examRepository.findById(examId).orElseThrow());
        questionRepository.save(question);
        return "redirect:/admin/exams/" + examId + "/questions";
    }

    @PostMapping("/{questionId}/delete")
    public String delete(@PathVariable Long examId, @PathVariable Long questionId) {
        questionRepository.deleteById(questionId);
        return "redirect:/admin/exams/" + examId + "/questions";
    }
}
