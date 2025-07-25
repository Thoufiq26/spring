package com.example.first.controller;

import com.example.first.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.profiles.active=qa",
    "spring.data.mongodb.uri=mongodb://localhost:27017/qa_db"
})
class StudentControllerIT { // Changed from StudentControllerIntegrationTest

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/students";
        mongoTemplate.getDb().drop();
    }

    @Test
    void testHealthCheck() {
        ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl + "/health", Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("qa", response.getBody().get("stage"));
    }

    @Test
    void testGetAllStudents_EmptyList() {
        ResponseEntity<Student[]> response = restTemplate.getForEntity(baseUrl, Student[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);
    }

    @Test
    void testCreateAndGetAllStudents() {
        Student student = new Student(null, "John Doe", "john@example.com");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Student> request = new HttpEntity<>(student, headers);
        ResponseEntity<Student> createResponse = restTemplate.postForEntity(baseUrl, request, Student.class);
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        assertNotNull(createResponse.getBody().getId());
        assertEquals("John Doe", createResponse.getBody().getName());
        assertEquals("john@example.com", createResponse.getBody().getEmail());
        ResponseEntity<Student[]> getResponse = restTemplate.getForEntity(baseUrl, Student[].class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals(1, getResponse.getBody().length);
        assertEquals("John Doe", getResponse.getBody()[0].getName());
    }

    @Test
    void testGetStudentById() {
        Student student = new Student(null, "Jane Smith", "jane@example.com");
        Student created = restTemplate.postForObject(baseUrl, student, Student.class);
        assertNotNull(created);
        String id = created.getId();
        ResponseEntity<Student> response = restTemplate.getForEntity(baseUrl + "/" + id, Student.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(id, response.getBody().getId());
        assertEquals("Jane Smith", response.getBody().getName());
        assertEquals("jane@example.com", response.getBody().getEmail());
    }

    @Test
    void testGetStudentById_NotFound() {
        ResponseEntity<Student> response = restTemplate.getForEntity(baseUrl + "/nonexistent", Student.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.isNull(response.getBody()) || response.getBody().getId() == null);
    }

    @Test
    void testUpdateStudent() {
        Student student = new Student(null, "Alice Brown", "alice@example.com");
        Student created = restTemplate.postForObject(baseUrl, student, Student.class);
        assertNotNull(created);
        String id = created.getId();
        Student updatedStudent = new Student(null, "Alice Updated", "alice.updated@example.com");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Student> request = new HttpEntity<>(updatedStudent, headers);
        ResponseEntity<Student> response = restTemplate.exchange(
            baseUrl + "/" + id, HttpMethod.PUT, request, Student.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(id, response.getBody().getId());
        assertEquals("Alice Updated", response.getBody().getName());
        assertEquals("alice.updated@example.com", response.getBody().getEmail());
    }

    @Test
    void testDeleteStudent() {
        Student student = new Student(null, "Bob Wilson", "bob@example.com");
        Student created = restTemplate.postForObject(baseUrl, student, Student.class);
        assertNotNull(created);
        String id = created.getId();
        ResponseEntity<Void> response = restTemplate.exchange(
            baseUrl + "/" + id, HttpMethod.DELETE, null, Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseEntity<Student> getResponse = restTemplate.getForEntity(baseUrl + "/" + id, Student.class);
        assertTrue(Objects.isNull(getResponse.getBody()) || getResponse.getBody().getId() == null);
    }
}