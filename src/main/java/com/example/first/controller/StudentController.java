package com.example.first.controller;

import com.example.first.model.Student;
import com.example.first.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentRepository studentRepo;

    // Create
    @PostMapping
    public Student createStudent(@RequestBody Student student) {
        return studentRepo.save(student);
    }

    // Read All
    @GetMapping
    public List<Student> getAllStudents() {
        return studentRepo.findAll();
    }

    // Read by ID
    @GetMapping("/{id}")
    public Optional<Student> getStudentById(@PathVariable String id) {
        return studentRepo.findById(id);
    }

    // Update
    @PutMapping("/{id}")
    public Student updateStudent(@PathVariable String id, @RequestBody Student studentDetails) {
        Student student = studentRepo.findById(id).orElseThrow();
        student.setName(studentDetails.getName());
        student.setEmail(studentDetails.getEmail());
        return studentRepo.save(student);
    }

    // Delete
    @DeleteMapping("/{id}")
    public void deleteStudent(@PathVariable String id) {
        studentRepo.deleteById(id);
    }
}
