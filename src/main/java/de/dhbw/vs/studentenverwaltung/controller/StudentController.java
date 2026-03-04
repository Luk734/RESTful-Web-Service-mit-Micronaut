package de.dhbw.vs.studentenverwaltung.controller;

import de.dhbw.vs.studentenverwaltung.model.Student;
import de.dhbw.vs.studentenverwaltung.service.StudentService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;

import java.util.Collection;

@Controller("/studenten")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @Get
    public Collection<Student> list() {
        return studentService.findAll();
    }

    @Get("/{id}")
    public HttpResponse<Student> get(Long id) {
        return studentService.findById(id)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Post
    public HttpResponse<Student> create(@Body Student student) {
        Student created = studentService.save(student);
        return HttpResponse.created(created);
    }

    @Put("/{id}")
    public HttpResponse<Student> update(Long id, @Body Student student) {
        return studentService.update(id, student)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Delete("/{id}")
    public HttpResponse<Void> delete(Long id) {
        if (studentService.delete(id)) {
            return HttpResponse.noContent();
        }
        return HttpResponse.notFound();
    }
}
