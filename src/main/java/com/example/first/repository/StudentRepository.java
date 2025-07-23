package com.example.first.repository;

import com.example.first.model.Student;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StudentRepository extends MongoRepository<Student, String> {
    // You can add custom query methods here
}
