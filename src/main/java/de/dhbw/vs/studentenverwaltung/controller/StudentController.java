package de.dhbw.vs.studentenverwaltung.controller;

import de.dhbw.vs.studentenverwaltung.model.Student;
import de.dhbw.vs.studentenverwaltung.service.StudentService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.validation.Validated;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

import java.util.Collection;
import java.util.Set;

// REST-Controller für den Pfad /studenten
@Controller("/studenten")
// Aktiviert Validierung für alle Methoden
@Validated
public class StudentController {

    private final StudentService studentService;
    // Validator prüft die Annotations wie @NotBlank, @Min, @Pattern
    private final Validator validator;

    // Constructor Injection: Micronaut übergibt beide Abhängigkeiten
    public StudentController(StudentService studentService, Validator validator) {
        this.studentService = studentService;
        this.validator = validator;
    }

    // GET /studenten - Alle Studenten zurückgeben
    @Get
    public Collection<Student> list() {
        return studentService.findAll();
    }

    // GET /studenten/{id} - Einen Studenten abrufen
    @Get("/{id}")
    public HttpResponse<Student> get(Long id) {
        return studentService.findById(id)
                .map(HttpResponse::ok)           // Wenn gefunden: 200 OK
                .orElse(HttpResponse.notFound()); // Wenn nicht gefunden: 404
    }

    // POST /studenten - Neuen Studenten erstellen
    @Post
    public HttpResponse<Student> create(@Body @Valid Student student) {
        // Zusätzliche Validierung auf Constraint-Violations prüfen
        validateStudent(student);
        // Speichern - wenn Matrikelnummer existiert: Optional.empty()
        Student created = studentService.save(student)
                // Wenn Duplikat: 409 Conflict werfen
                .orElseThrow(() -> new HttpStatusException(HttpStatus.CONFLICT, "Matrikelnummer existiert bereits"));
        return HttpResponse.created(created);   // 201 Created
    }

    // PUT /studenten/{id} - Studenten aktualisieren
    @Put("/{id}")
    public HttpResponse<Student> update(Long id, @Body @Valid Student student) {
        // Validieren ob die Daten korrekt sind
        validateStudent(student);
        // Service versucht zu updaten und gibt Status zurück
        StudentService.UpdateStatus status = studentService.update(id, student);

        // Je nach Status unterschiedliche HTTP-Codes zurückgeben
        if (status == StudentService.UpdateStatus.NOT_FOUND) {
            return HttpResponse.notFound();     // 404 - Studieren existiert nicht
        }
        if (status == StudentService.UpdateStatus.DUPLICATE_MATRIKELNUMMER) {
            // 409 Conflict - Matrikelnummer von anderem Studenten
            throw new HttpStatusException(HttpStatus.CONFLICT, "Matrikelnummer existiert bereits");
        }

        student.setId(id);
        return HttpResponse.ok(student);       // 200 OK - erfolgreich aktualisiert
    }

    // Hilfsmethode: Validiert den Studenten und wirft 400 Bad Request bei Fehlern
    private void validateStudent(Student student) {
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        if (!violations.isEmpty()) {
            // Erste Verletzung auswählen und als Fehlermeldung zurückgeben
            ConstraintViolation<Student> first = violations.iterator().next();
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, first.getMessage());
        }
    }

    // DELETE /studenten/{id} - Studenten löschen
    @Delete("/{id}")
    public HttpResponse<Void> delete(Long id) {
        if (studentService.delete(id)) {
            return HttpResponse.noContent();     // 204 No Content - erfolgreich gelöscht
        }
        return HttpResponse.notFound();          // 404 - Studieren nicht gefunden
    }
}
