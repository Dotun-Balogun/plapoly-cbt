package com.psp.cbt.controller;

import com.psp.cbt.entity.Admin;
import com.psp.cbt.entity.Student;
import com.psp.cbt.repository.AdminRepository;
import com.psp.cbt.repository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

// Handles the landing page and both login/logout flows.
@Controller
public class AuthController {

    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AdminRepository adminRepository,
                           StudentRepository studentRepository,
                           PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    // ---------- Admin ----------

    @GetMapping("/admin/login")
    public String adminLoginForm() {
        return "admin-login";
    }

    @PostMapping("/admin/login")
    public String adminLogin(@RequestParam String username,
                              @RequestParam String password,
                              HttpSession session,
                              Model model) {
        Optional<Admin> adminOpt = adminRepository.findByUsername(username);
        if (adminOpt.isPresent() && passwordEncoder.matches(password, adminOpt.get().getPassword())) {
            session.setAttribute("adminId", adminOpt.get().getId());
            session.setAttribute("adminUsername", adminOpt.get().getUsername());
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("error", "Invalid username or password.");
        return "admin-login";
    }

    @GetMapping("/admin/logout")
    public String adminLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // ---------- Student ----------

    @GetMapping("/student/login")
    public String studentLoginForm() {
        return "student-login";
    }

    @PostMapping("/student/login")
    public String studentLogin(@RequestParam String matricNumber,
                                HttpSession session,
                                Model model) {
        Optional<Student> studentOpt = studentRepository.findByMatricNumber(matricNumber.trim());

        if (studentOpt.isEmpty()) {
            model.addAttribute("error", "That matriculation number is not registered.");
            return "student-login";
        }
        Student student = studentOpt.get();
        if (!student.isActive()) {
            model.addAttribute("error", "This student account has been deactivated. Contact the admin.");
            return "student-login";
        }

        session.setAttribute("studentId", student.getId());
        return "redirect:/student/exams";
    }

    @GetMapping("/student/logout")
    public String studentLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
