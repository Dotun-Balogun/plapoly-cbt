package com.psp.cbt.controller;

import com.psp.cbt.entity.Student;
import com.psp.cbt.repository.StudentRepository;
import com.psp.cbt.service.MatricNumberService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/students")
public class AdminStudentController {

    private final StudentRepository studentRepository;
    private final MatricNumberService matricNumberService;

    public AdminStudentController(StudentRepository studentRepository,
                                   MatricNumberService matricNumberService) {
        this.studentRepository = studentRepository;
        this.matricNumberService = matricNumberService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        List<Student> students;
        if (q != null && !q.isBlank()) {
            students = studentRepository.findByFullNameContainingIgnoreCaseOrMatricNumberContainingIgnoreCase(q, q);
        } else {
            students = studentRepository.findAll();
        }
        model.addAttribute("students", students);
        model.addAttribute("q", q);
        return "admin/students";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("student", new Student());
        return "admin/student-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentRepository.findById(id).orElseThrow());
        return "admin/student-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Student student, Model model) {
        // Build the matric number from the academic components every time,
        // so it always stays consistent with the current field values.
        student.setMatricNumber(matricNumberService.build(student));
        try {
            studentRepository.save(student);
        } catch (DataIntegrityViolationException e) {
            // Most likely cause: another student already has this exact
            // combination of school/department/programme/year/serial,
            // which produces the same matric number.
            model.addAttribute("error",
                    "A student with matriculation number '" + student.getMatricNumber() +
                    "' already exists. Check the serial number and try again.");
            model.addAttribute("student", student);
            return "admin/student-form";
        }
        return "redirect:/admin/students";
    }

    @PostMapping("/{id}/toggle")
    public String toggleActive(@PathVariable Long id) {
        Student student = studentRepository.findById(id).orElseThrow();
        student.setActive(!student.isActive());
        studentRepository.save(student);
        return "redirect:/admin/students";
    }
}
