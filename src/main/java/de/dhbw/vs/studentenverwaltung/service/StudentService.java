package de.dhbw.vs.studentenverwaltung.service;

import de.dhbw.vs.studentenverwaltung.model.Student;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class StudentService {

    private final ConcurrentHashMap<Long, Student> students = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public Collection<Student> findAll() {
        return students.values();
    }

    public Optional<Student> findById(Long id) {
        return Optional.ofNullable(students.get(id));
    }

    public Student save(Student student) {
        Long id = idCounter.getAndIncrement();
        student.setId(id);
        students.put(id, student);
        return student;
    }

    public Optional<Student> update(Long id, Student student) {
        if (!students.containsKey(id)) {
            return Optional.empty();
        }
        student.setId(id);
        students.put(id, student);
        return Optional.of(student);
    }

    public boolean delete(Long id) {
        return students.remove(id) != null;
    }
}
