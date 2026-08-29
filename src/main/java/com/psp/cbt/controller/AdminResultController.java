package com.psp.cbt.controller;

import com.psp.cbt.entity.ExamAttempt;
import com.psp.cbt.repository.ExamAttemptRepository;
import com.psp.cbt.repository.ExamRepository;
import com.psp.cbt.repository.StudentAnswerRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class AdminResultController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final ExamAttemptRepository examAttemptRepository;
    private final ExamRepository examRepository;
    private final StudentAnswerRepository studentAnswerRepository;

    public AdminResultController(ExamAttemptRepository examAttemptRepository,
                                  ExamRepository examRepository,
                                  StudentAnswerRepository studentAnswerRepository) {
        this.examAttemptRepository = examAttemptRepository;
        this.examRepository = examRepository;
        this.studentAnswerRepository = studentAnswerRepository;
    }

    // Shared by both the page and the CSV export, so they always show
    // exactly the same rows for the same search/filter.
    private List<ExamAttempt> filteredResults(String q, Long examId) {
        List<ExamAttempt> results = examAttemptRepository.findBySubmittedTrue();

        if (examId != null) {
            results = results.stream().filter(a -> a.getExam().getId().equals(examId)).toList();
        }
        if (q != null && !q.isBlank()) {
            String needle = q.toLowerCase();
            results = results.stream().filter(a ->
                    a.getStudent().getFullName().toLowerCase().contains(needle) ||
                    a.getStudent().getMatricNumber().toLowerCase().contains(needle)
            ).toList();
        }
        return results;
    }

    @GetMapping("/admin/results")
    public String list(@RequestParam(required = false) String q,
                        @RequestParam(required = false) Long examId,
                        Model model) {
        List<ExamAttempt> inProgress = examAttemptRepository.findAll().stream()
                .filter(a -> !a.isSubmitted())
                .toList();

        model.addAttribute("results", filteredResults(q, examId));
        model.addAttribute("inProgress", inProgress);
        model.addAttribute("exams", examRepository.findAll());
        model.addAttribute("q", q);
        model.addAttribute("examId", examId);
        return "admin/results";
    }

    @GetMapping("/admin/results/{id}")
    public String detail(@PathVariable Long id, Model model) {
        ExamAttempt attempt = examAttemptRepository.findById(id).orElseThrow();
        model.addAttribute("attempt", attempt);
        return "admin/result-detail";
    }

    // Deletes an attempt entirely (its answers too), so the student can
    // start completely fresh next time they click "Start Exam".
    @PostMapping("/admin/results/{id}/reset")
    public String reset(@PathVariable Long id) {
        ExamAttempt attempt = examAttemptRepository.findById(id).orElseThrow();
        studentAnswerRepository.deleteAll(studentAnswerRepository.findByAttemptId(attempt.getId()));
        examAttemptRepository.delete(attempt);
        return "redirect:/admin/results";
    }

    // Streams the currently filtered results as a downloadable CSV file.
    @GetMapping("/admin/results/export")
    public void exportCsv(@RequestParam(required = false) String q,
                           @RequestParam(required = false) Long examId,
                           HttpServletResponse response) throws Exception {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"exam-results.csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("Full Name,Matric Number,Exam,Score,Total Marks,Percentage,Grade,Submitted At");

        for (ExamAttempt a : filteredResults(q, examId)) {
            writer.printf("%s,%s,%s,%.1f,%.1f,%.1f,%s,%s%n",
                    csvSafe(a.getStudent().getFullName()),
                    csvSafe(a.getStudent().getMatricNumber()),
                    csvSafe(a.getExam().getTitle()),
                    a.getScore(),
                    a.getTotalMarks(),
                    a.getPercentage(),
                    a.getGrade(),
                    a.getSubmittedAt() != null ? a.getSubmittedAt().format(DATE_FMT) : "");
        }
        writer.flush();
    }

    // Wraps a field in quotes if it contains a comma, so the CSV doesn't
    // break on names like "Smith, John".
    private String csvSafe(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
