package de.dhbw.vs.studentenverwaltung.service;

import de.dhbw.vs.studentenverwaltung.model.Student;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// Nur eine Instanz für die gesamte Anwendung (Singleton)
@Singleton
public class StudentService {

    // Speichert alle Studenten: Schlüssel=ID, Wert=Student
    private final ConcurrentHashMap<Long, Student> students = new ConcurrentHashMap<>();
    // Speichert die Matrikelnummern um Duplikate zu verhindern: Schlüssel=Matrikelnummer, Wert=ID
    private final ConcurrentHashMap<String, Long> matrikelnummern = new ConcurrentHashMap<>();
    // Zähler für automatische ID-Vergabe (thread-safe)
    private final AtomicLong idCounter = new AtomicLong(1);

    // Status-Enum um verschiedene Fehler beim Update zu unterscheiden
    public enum UpdateStatus {
        UPDATED,                      // Erfolgreich aktualisiert
        NOT_FOUND,                    // Studieren existiert nicht
        DUPLICATE_MATRIKELNUMMER      // Matrikelnummer ist schon vergeben
    }

    public Collection<Student> findAll() {
        return students.values();
    }

    public Optional<Student> findById(Long id) {
        return Optional.ofNullable(students.get(id));
    }

    // synchronized: Nur ein Thread darf gleichzeitig speichern (verhindert Duplikate)
    public synchronized Optional<Student> save(Student student) {
        // Prüfe ob Matrikelnummer schon existiert
        if (matrikelnummern.containsKey(student.getMatrikelnummer())) {
            return Optional.empty();  // Duplikat - nicht speichern
        }

        // Neue ID generieren und Studenten speichern
        Long id = idCounter.getAndIncrement();
        student.setId(id);
        students.put(id, student);
        // Matrikelnummer für Duplikat-Prüfung registrieren
        matrikelnummern.put(student.getMatrikelnummer(), id);
        return Optional.of(student);
    }

    // synchronized: Nur ein Thread darf gleichzeitig updaten (verhindert Race Conditions)
    public synchronized UpdateStatus update(Long id, Student student) {
        // Prüfe ob Studieren existiert
        Student existing = students.get(id);
        if (existing == null) {
            return UpdateStatus.NOT_FOUND;
        }

        // Prüfe ob neue Matrikelnummer schon von jemand anderes verwendet wird
        Long existingForMatrikelnummer = matrikelnummern.get(student.getMatrikelnummer());
        if (existingForMatrikelnummer != null && !existingForMatrikelnummer.equals(id)) {
            return UpdateStatus.DUPLICATE_MATRIKELNUMMER;  // Duplikat bei anderer ID
        }

        // Update durchführen: alte Matrikelnummer entfernen und neue registrieren
        student.setId(id);
        students.put(id, student);
        matrikelnummern.remove(existing.getMatrikelnummer());
        matrikelnummern.put(student.getMatrikelnummer(), id);
        return UpdateStatus.UPDATED;
    }

    // synchronized: Nur ein Thread darf gleichzeitig löschen
    public synchronized boolean delete(Long id) {
        // Versuche Studieren zu löschen
        Student removed = students.remove(id);
        if (removed == null) {
            return false;  // Nicht gefunden
        }

        // Auch die Matrikelnummer-Registrierung löschen
        matrikelnummern.remove(removed.getMatrikelnummer());
        return true;
    }
}
