package com.psp.cbt.controller;

import com.psp.cbt.entity.Course;
import com.psp.cbt.repository.CourseRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/courses")
public class AdminCourseController {

    private final CourseRepository courseRepository;

    public AdminCourseController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("courses", courseRepository.findAll());
        return "admin/courses";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("course", new Course());
        return "admin/course-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("course", courseRepository.findById(id).orElseThrow());
        return "admin/course-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Course course, Model model) {
        try {
            courseRepository.save(course);
        } catch (DataIntegrityViolationException e) {
            // Most likely cause: another course already uses this code
            // (the "code" column has a unique constraint in the database).
            model.addAttribute("error",
                    "A course with the code '" + course.getCode() + "' already exists. " +
                    "Please use a different code.");
            model.addAttribute("course", course);
            return "admin/course-form";
        }
        return "redirect:/admin/courses";
    }

    @PostMapping("/{id}/toggle")
    public String toggleActive(@PathVariable Long id) {
        Course course = courseRepository.findById(id).orElseThrow();
        course.setActive(!course.isActive());
        courseRepository.save(course);
        return "redirect:/admin/courses";
    }
}
