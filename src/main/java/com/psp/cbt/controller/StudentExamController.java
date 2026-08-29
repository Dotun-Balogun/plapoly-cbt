package com.psp.cbt.controller;

import com.psp.cbt.entity.*;
import com.psp.cbt.repository.*;
import com.psp.cbt.service.GradeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/student")
public class StudentExamController {

    private final StudentRepository studentRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final GradeService gradeService;

    public StudentExamController(StudentRepository studentRepository,
                                  ExamRepository examRepository,
                                  QuestionRepository questionRepository,
                                  ExamAttemptRepository examAttemptRepository,
                                  StudentAnswerRepository studentAnswerRepository,
                                  GradeService gradeService) {
        this.studentRepository = studentRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.gradeService = gradeService;
    }

    private Student currentStudent(HttpSession session) {
        Long id = (Long) session.getAttribute("studentId");
        return studentRepository.findById(id).orElseThrow();
    }

    // List every ACTIVE exam, and whether this student has already
    // taken (submitted) each one.
    @GetMapping("/exams")
    public String listExams(HttpSession session, Model model) {
        Student student = currentStudent(session);
        List<Exam> exams = examRepository.findByStatus(Exam.Status.ACTIVE);

        Map<Long, ExamAttempt> attemptsByExam = new HashMap<>();
        for (Exam exam : exams) {
            examAttemptRepository.findByStudentIdAndExamId(student.getId(), exam.getId())
                    .ifPresent(a -> attemptsByExam.put(exam.getId(), a));
        }

        model.addAttribute("student", student);
        model.addAttribute("exams", exams);
        model.addAttribute("attemptsByExam", attemptsByExam);
        return "student/exams";
    }

    // Starts a new attempt, or resumes one already in progress.
    @PostMapping("/exams/{examId}/start")
    public String start(@PathVariable Long examId, HttpSession session) {
        Student student = currentStudent(session);
        Exam exam = examRepository.findById(examId).orElseThrow();

        ExamAttempt attempt = examAttemptRepository
                .findByStudentIdAndExamId(student.getId(), examId)
                .orElse(null);

        if (attempt == null) {
            attempt = new ExamAttempt();
            attempt.setStudent(student);
            attempt.setExam(exam);
            attempt.setStartTime(LocalDateTime.now());
            attempt.setEndTime(LocalDateTime.now().plusMinutes(exam.getDurationMinutes()));
            attempt = examAttemptRepository.save(attempt);

            // Create one (empty) answer row per question up front,
            // so the exam page and navigator have something to show.
            for (Question q : questionRepository.findByExamId(examId)) {
                StudentAnswer answer = new StudentAnswer();
                answer.setAttempt(attempt);
                answer.setQuestion(q);
                studentAnswerRepository.save(answer);
            }
        } else if (attempt.isSubmitted()) {
            // Already completed - go straight to the result.
            return "redirect:/student/result/" + attempt.getId();
        }

        return "redirect:/student/exam/" + attempt.getId();
    }

    @GetMapping("/exam/{attemptId}")
    public String takeExam(@PathVariable Long attemptId, HttpSession session, Model model) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId).orElseThrow();
        Student student = currentStudent(session);

        if (!attempt.getStudent().getId().equals(student.getId())) {
            return "redirect:/student/exams"; // not this student's attempt
        }
        if (attempt.isSubmitted()) {
            return "redirect:/student/result/" + attempt.getId();
        }

        long secondsRemaining = ChronoUnit.SECONDS.between(LocalDateTime.now(), attempt.getEndTime());
        if (secondsRemaining <= 0) {
            scoreAndSubmit(attempt);
            return "redirect:/student/result/" + attempt.getId();
        }

        model.addAttribute("attempt", attempt);
        model.addAttribute("exam", attempt.getExam());
        model.addAttribute("student", student);
        model.addAttribute("secondsRemaining", secondsRemaining);
        model.addAttribute("answers", studentAnswerRepository.findByAttemptId(attemptId));
        return "student/exam-take";
    }

    // Called by JavaScript (fetch) every time the student picks/changes an answer.
    @PostMapping("/exam/{attemptId}/answer")
    @ResponseBody
    public Map<String, Object> saveAnswer(@PathVariable Long attemptId,
                                           @RequestParam Long questionId,
                                           @RequestParam String selectedOption,
                                           HttpSession session) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId).orElseThrow();
        Student student = currentStudent(session);
        Map<String, Object> result = new HashMap<>();

        if (!attempt.getStudent().getId().equals(student.getId()) || attempt.isSubmitted()) {
            result.put("ok", false);
            return result;
        }

        StudentAnswer answer = studentAnswerRepository
                .findByAttemptIdAndQuestionId(attemptId, questionId).orElseThrow();
        answer.setSelectedOption(selectedOption);
        studentAnswerRepository.save(answer);

        result.put("ok", true);
        return result;
    }

    @PostMapping("/exam/{attemptId}/submit")
    public String submit(@PathVariable Long attemptId, HttpSession session) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId).orElseThrow();
        Student student = currentStudent(session);

        if (!attempt.getStudent().getId().equals(student.getId())) {
            return "redirect:/student/exams";
        }
        if (!attempt.isSubmitted()) {
            scoreAndSubmit(attempt);
        }
        return "redirect:/student/result/" + attempt.getId();
    }

    @GetMapping("/result/{attemptId}")
    public String result(@PathVariable Long attemptId, HttpSession session, Model model) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId).orElseThrow();
        Student student = currentStudent(session);
        if (!attempt.getStudent().getId().equals(student.getId())) {
            return "redirect:/student/exams";
        }
        model.addAttribute("attempt", attempt);
        return "student/result";
    }

    // Compares every answer to the stored correct option and works
    // out score, percentage and grade. This is the only place scoring happens.
    private void scoreAndSubmit(ExamAttempt attempt) {
        List<StudentAnswer> answers = studentAnswerRepository.findByAttemptId(attempt.getId());

        double totalMarks = 0;
        double score = 0;
        for (StudentAnswer a : answers) {
            Question q = a.getQuestion();
            totalMarks += q.getMarks();
            if (a.getSelectedOption() != null && a.getSelectedOption().equals(q.getCorrectOption())) {
                score += q.getMarks();
            }
        }
        double percentage = totalMarks == 0 ? 0 : (score / totalMarks) * 100.0;

        attempt.setTotalMarks(totalMarks);
        attempt.setScore(score);
        attempt.setPercentage(percentage);
        attempt.setGrade(gradeService.getGrade(percentage));
        attempt.setSubmitted(true);
        attempt.setSubmittedAt(LocalDateTime.now());
        examAttemptRepository.save(attempt);
    }
}
