package de.dhbw.vs.studentenverwaltung.model;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// Erlaubt JSON-Konvertierung (JSON ↔ Objekt)
@Serdeable
public class Student {

    private Long id;

    // Darf nicht leer sein
    @NotBlank
    private String vorname;

    // Darf nicht leer sein
    @NotBlank
    private String nachname;

    // Darf nicht leer sein und muss genau 7 Ziffern haben (z.B. "1234567")
    @NotBlank
    @Pattern(regexp = "\\d{7}", message = "matrikelnummer muss genau 7 Ziffern enthalten")
    private String matrikelnummer;

    // Darf nicht leer sein
    @NotBlank
    private String studiengang;

    // Mindestens 1, kein Maximum
    @Min(1)
    private int semester;

    public Student() {
    }

    public Student(Long id, String vorname, String nachname, String matrikelnummer, String studiengang, int semester) {
        this.id = id;
        this.vorname = vorname;
        this.nachname = nachname;
        this.matrikelnummer = matrikelnummer;
        this.studiengang = studiengang;
        this.semester = semester;
    }

    // Getter und Setter für alle Felder
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public String getNachname() {
        return nachname;
    }

    public void setNachname(String nachname) {
        this.nachname = nachname;
    }

    public String getMatrikelnummer() {
        return matrikelnummer;
    }

    public void setMatrikelnummer(String matrikelnummer) {
        this.matrikelnummer = matrikelnummer;
    }

    public String getStudiengang() {
        return studiengang;
    }

    public void setStudiengang(String studiengang) {
        this.studiengang = studiengang;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }
}
