# Studentenverwaltung - RESTful Web Service mit Micronaut

## Dokumentationsstruktur (neu)

Fuer besseres Lernen und Nachvollziehen ist die Doku in 4 Dateien aufgeteilt:

1. `README-01-AENDERUNGEN.md` - Alle Aenderungen gegenueber der Micronaut-Vorlage
2. `README-02-CODE-ERKLAERUNG.md` - Ausfuehrliche Erklaerung der Implementierung
3. `README-03-REST-MICRONAUT.md` - RESTful Web Services mit Micronaut im Detail
4. `README-04-SETUP-START-TEST.md` - Setup, Start und Testanleitung

---
Quellen für das Paper:
https://www.mdpi.com/2076-3417/13/3/1343
https://link.springer.com/chapter/10.1007/978-3-658-45192-9_5
https://ieeexplore.ieee.org/abstract/document/9245290
https://www.mdpi.com/1424-8220/22/20/7759
https://books.google.de/books?id=ZvM5EAAAQBAJ
https://ieeexplore.ieee.org/abstract/document/10741246
https://books.google.de/books?id=3zkzEAAAQBAJ
https://roy.gbiv.com/pubs/dissertation/fielding_dissertation.pdf
https://urn.kb.se/resolve?urn=urn:nbn:se:bth-26504

## Inhaltsverzeichnis

1. [Was ist dieses Projekt?](#was-ist-dieses-projekt)
2. [Was ist Micronaut?](#was-ist-micronaut)
3. [Projektstruktur](#projektstruktur)
4. [Das ursprüngliche Micronaut-Projekt](#das-ursprüngliche-micronaut-projekt)
5. [Unsere Änderungen](#unsere-änderungen)
6. [Die einzelnen Klassen erklärt](#die-einzelnen-klassen-erklärt)
7. [Wie startet man den Server?](#wie-startet-man-den-server)
8. [API-Endpunkte testen](#api-endpunkte-testen)
9. [Weiterführende Links](#weiterführende-links)

---

## Was ist dieses Projekt?

Dieses Projekt ist eine **Studentenverwaltung** als RESTful Web Service. Man kann Studenten anlegen, abrufen, aktualisieren und löschen (CRUD-Operationen). Die Daten werden im Arbeitsspeicher gehalten (In-Memory), d.h. nach einem Neustart sind alle Daten weg – das ist für Demos und Lernzwecke aber völlig okay.

**Technologie-Stack:**
- **Micronaut 4.10.9** – Das Web-Framework
- **Java 17+** – Die Programmiersprache
- **Maven** – Build-Tool
- **Netty** – Der HTTP-Server (läuft unter der Haube)

---

## Was ist Micronaut?

Micronaut ist ein modernes Java-Framework für Microservices und serverlose Anwendungen. Der große Unterschied zu Spring Boot:

| Merkmal | Micronaut | Spring Boot |
|---------|-----------|-------------|
| **Dependency Injection** | Zur Compile-Zeit (AOT) | Zur Laufzeit (Reflection) |
| **Startup-Zeit** | Sehr schnell (~1-2 Sek.) | Langsamer (~3-8 Sek.) |
| **Memory-Verbrauch** | Gering (~50-100 MB) | Höher (~200-400 MB) |
| **Cloud-Native** | Von Grund auf designed | Nachträglich angepasst |

**Warum ist das wichtig?** Micronaut analysiert deine Annotationen (`@Controller`, `@Singleton`, etc.) bereits beim Kompilieren und generiert den notwendigen Code. Das spart Reflection zur Laufzeit und macht die Anwendung schneller und ressourcenschonender.

---

## Projektstruktur

```
studentenverwaltung/
├── src/
│   ├── main/
│   │   ├── java/de/dhbw/vs/studentenverwaltung/
│   │   │   ├── Application.java          # Startpunkt der Anwendung
│   │   │   ├── controller/
│   │   │   │   └── StudentController.java # REST-Endpunkte
│   │   │   ├── model/
│   │   │   │   └── Student.java           # Datenmodell
│   │   │   └── service/
│   │   │       └── StudentService.java    # Geschäftslogik
│   │   └── resources/
│   │       ├── application.properties     # Konfiguration
│   │       └── logback.xml                # Logging-Einstellungen
│   └── test/
│       └── java/de/dhbw/vs/
│           └── StudentenverwaltungTest.java # Automatische Tests
├── target/                                 # Kompilierte Dateien
│   ├── studentenverwaltung-0.1.jar        # Das ausführbare JAR
│   └── classes/
│       └── de/dhbw/vs/studentenverwaltung/
│           ├── controller/
│           │   ├── StudentController.class
│           │   ├── $StudentController$Definition.class      # ← VON MICRONAUT GENERIERT
│           │   └── $StudentController$Definition$Exec.class # ← VON MICRONAUT GENERIERT
│           ├── model/
│           │   ├── Student.class
│           │   └── $Student$Introspection.class             # ← VON MICRONAUT GENERIERT
│           └── service/
│               ├── StudentService.class
│               └── $StudentService$Definition.class         # ← VON MICRONAUT GENERIERT
├── pom.xml                                # Maven-Konfiguration
├── mvnw / mvnw.bat                        # Maven Wrapper (kein Maven-Install nötig)
├── micronaut-cli.yml                      # Metadaten vom Micronaut-Generator
└── aot-jar.properties                     # AOT-Optimierungseinstellungen
```

### Die generierten `$Definition`-Klassen

Das ist das Besondere an Micronaut! Wenn du kompilierst, werden automatisch zusätzliche Klassen erzeugt:

- **`$StudentController$Definition.class`** – Enthält alle Infos über den Controller (welche Methoden, welche HTTP-Methoden, welche Pfade)
- **`$StudentService$Definition.class`** – Enthält die Bean-Definition für Dependency Injection
- **`$Student$Introspection.class`** – Enthält Metadaten über die Felder der Student-Klasse (für JSON-Serialisierung)

Diese Klassen werden zur **Compile-Zeit** generiert, nicht zur Laufzeit. Das ist der Grund, warum Micronaut so schnell startet.

---

## Das ursprüngliche Micronaut-Projekt

Wenn man ein neues Micronaut-Projekt erstellt (z.B. über [Micronaut Launch](https://micronaut.io/launch/)), bekommt man ein "leeres" Grundgerüst. Der Generator fragt ein paar Sachen ab (Name, Java-Version, Build-Tool, gewünschte Features) und spuckt dann ein ZIP mit allen Dateien aus.

### Wie erstellt man so ein Projekt?

1. Gehe auf https://micronaut.io/launch/
2. Wähle:
   - **Application Type:** Micronaut Application
   - **Java Version:** 17 oder höher
   - **Build Tool:** Maven
   - **Test Framework:** JUnit
3. Bei **Features** wurden ausgewählt:
   - `netty-server` (HTTP-Server)
   - `serialization-jackson` (JSON-Verarbeitung)
   - `micronaut-aot` (Ahead-of-Time-Optimierung)
4. Klick auf "Generate" → ZIP herunterladen → entpacken

### Was war schon da (im Detail):

#### 1. `Application.java` – Der Einstiegspunkt

```java
package de.dhbw.vs.studentenverwaltung;

import io.micronaut.runtime.Micronaut;

public class Application {
    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
```

Das ist wirklich alles. Diese eine Zeile `Micronaut.run()` macht die ganze Magie:
- Lädt alle Bean-Definitionen (die zur Compile-Zeit generierten `$Definition`-Klassen)
- Initialisiert den Dependency-Injection-Kontext
- Startet den Netty HTTP-Server
- Wartet auf eingehende Requests

**Zum Vergleich mit Spring Boot:** Dort steht `SpringApplication.run()` – sieht fast gleich aus, funktioniert aber komplett anders unter der Haube (Reflection vs. Compile-Time).

#### 2. `pom.xml` – Die Maven-Konfiguration

Die wichtigsten Teile:

```xml
<!-- Parent POM von Micronaut – enthält alle Versionen und Plugin-Configs -->
<parent>
    <groupId>io.micronaut.platform</groupId>
    <artifactId>micronaut-parent</artifactId>
    <version>4.10.9</version>
</parent>

<!-- Der HTTP-Server (Netty) -->
<dependency>
    <groupId>io.micronaut</groupId>
    <artifactId>micronaut-http-server-netty</artifactId>
</dependency>

<!-- JSON-Serialisierung mit Jackson -->
<dependency>
    <groupId>io.micronaut.serde</groupId>
    <artifactId>micronaut-serde-jackson</artifactId>
</dependency>

<!-- Annotation Processor – generiert die $Definition-Klassen -->
<annotationProcessorPaths>
    <path>
        <groupId>io.micronaut</groupId>
        <artifactId>micronaut-http-validation</artifactId>
    </path>
    <path>
        <groupId>io.micronaut.serde</groupId>
        <artifactId>micronaut-serde-processor</artifactId>
    </path>
</annotationProcessorPaths>
```

**Was sind Annotation Processors?** Das sind Programme, die während des Kompilierens laufen und zusätzlichen Code generieren. Micronaut nutzt sie, um die Bean-Definitionen zu erstellen. Deshalb braucht man zur Laufzeit keine Reflection.

#### 3. `application.properties` – Konfiguration

Ursprünglich nur:
```properties
micronaut.application.name=studentenverwaltung
```

Hier kann man alles Mögliche konfigurieren: Port, Logging-Level, Datenbank-Verbindungen, etc.

#### 4. `logback.xml` – Logging-Konfiguration

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%cyan(%d{HH:mm:ss.SSS}) %gray([%thread]) %highlight(%-5level) %magenta(%logger{36}) - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="info">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

Das sorgt für die farbige Konsolenausgabe. Micronaut nutzt [Logback](https://logback.qos.ch/) als Logging-Framework.

#### 5. `StudentenverwaltungTest.java` – Der Basis-Test

```java
@MicronautTest
class StudentenverwaltungTest {

    @Inject
    EmbeddedApplication<?> application;

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
    }
}
```

Ein simpler "Smoke Test" – er prüft nur, ob die Anwendung überhaupt startet. `@MicronautTest` startet einen eingebetteten Server für den Test, `@Inject` holt sich die laufende Anwendung.

#### 6. `mvnw` / `mvnw.bat` – Maven Wrapper

Damit muss man Maven nicht global installieren. Der Wrapper lädt automatisch die richtige Maven-Version herunter und führt sie aus. Man ruft einfach `.\mvnw.bat` statt `mvn` auf.

#### 7. `micronaut-cli.yml` – Projekt-Metadaten

```yaml
applicationType: default
defaultPackage: de.dhbw.vs
testFramework: junit
sourceLanguage: java
buildTool: maven
features: [app-name, http-client-test, java, java-application, junit, logback, 
           maven, maven-enforcer-plugin, micronaut-aot, micronaut-http-validation, 
           netty-server, serialization-jackson, ...]
```

Diese Datei wird vom Micronaut-Generator erstellt und enthält Infos darüber, welche Features beim Erstellen ausgewählt wurden. Sie hat keinen Einfluss auf die Laufzeit.

#### 8. `aot-jar.properties` – AOT-Optimierungen

```properties
# Caches environment property values
cached.environment.enabled=true

# Precomputes configuration from environment variables
precompute.environment.properties.enabled=true

# Converts logback.xml to Java config
logback.xml.to.java.enabled=true

# Scans for services at build time instead of runtime
serviceloading.jit.enabled=true
```

**AOT = Ahead-of-Time.** Das sind Optimierungen, die beim Bauen des JARs angewendet werden. Sie machen die Anwendung noch schneller, weil noch mehr Arbeit zur Compile-Zeit erledigt wird.

### Was war NICHT da:

- ❌ Kein Controller (keine REST-Endpunkte)
- ❌ Kein Model (keine Datenklassen)
- ❌ Kein Service (keine Geschäftslogik)
- ❌ Keine Datenhaltung

Das Projekt war quasi eine "leere Hülle" – ein Server, der starten konnte, aber auf keinen Request reagiert hat (außer 404 Not Found).

---

## Unsere Änderungen

Wir haben das leere Micronaut-Projekt zu einer funktionierenden Studentenverwaltung erweitert. Hier ist genau, was wir gemacht haben:

### Übersicht der neuen Dateien

| Datei | Typ | Zweck |
|-------|-----|-------|
| `Student.java` | Model | Datenklasse für einen Studenten |
| `StudentService.java` | Service | Geschäftslogik (CRUD-Operationen) |
| `StudentController.java` | Controller | REST-Endpunkte (HTTP → Service) |

### 1. Port geändert (`application.properties`)

```properties
# VORHER (nur eine Zeile):
micronaut.application.name=studentenverwaltung

# NACHHER (zwei Zeilen):
micronaut.application.name=studentenverwaltung
micronaut.server.port=8090
```

Standardmäßig läuft Micronaut auf Port 8080. Wir haben das auf 8090 geändert – praktisch, falls Port 8080 schon von einer anderen Anwendung belegt ist.

---

### 2. Model erstellt (`Student.java`)

**Pfad:** `src/main/java/de/dhbw/vs/studentenverwaltung/model/Student.java`

Der vollständige Code:

```java
package de.dhbw.vs.studentenverwaltung.model;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class Student {

    private Long id;
    private String vorname;
    private String nachname;
    private String matrikelnummer;
    private String studiengang;
    private int semester;

    // Leerer Konstruktor (wird für JSON-Deserialisierung gebraucht)
    public Student() {
    }

    // Konstruktor mit allen Feldern
    public Student(Long id, String vorname, String nachname, 
                   String matrikelnummer, String studiengang, int semester) {
        this.id = id;
        this.vorname = vorname;
        this.nachname = nachname;
        this.matrikelnummer = matrikelnummer;
        this.studiengang = studiengang;
        this.semester = semester;
    }

    // Getter und Setter für alle Felder...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getVorname() { return vorname; }
    public void setVorname(String vorname) { this.vorname = vorname; }
    
    // ... und so weiter für alle Felder
}
```

**Was bedeutet `@Serdeable`?**

Das ist eine Micronaut-Annotation, die sagt: "Diese Klasse kann zu JSON serialisiert und von JSON deserialisiert werden."

Ohne diese Annotation würde der Server einen Fehler werfen, wenn man einen Studenten als JSON zurückgeben will:
```
No serializable introspection present for type Student
```

**Warum brauchen wir zwei Konstruktoren?**

- **Leerer Konstruktor:** Wird gebraucht, wenn Micronaut einen JSON-Request in ein Student-Objekt umwandeln will
- **Vollständiger Konstruktor:** Praktisch für Tests und wenn wir selbst Studenten erstellen

---

### 3. Service erstellt (`StudentService.java`)

**Pfad:** `src/main/java/de/dhbw/vs/studentenverwaltung/service/StudentService.java`

Der vollständige Code:

```java
package de.dhbw.vs.studentenverwaltung.service;

import de.dhbw.vs.studentenverwaltung.model.Student;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class StudentService {

    // Unsere "Datenbank" – eine einfache HashMap
    private final ConcurrentHashMap<Long, Student> students = new ConcurrentHashMap<>();
    
    // Zähler für automatische ID-Vergabe
    private final AtomicLong idCounter = new AtomicLong(1);

    // Alle Studenten zurückgeben
    public Collection<Student> findAll() {
        return students.values();
    }

    // Einen Studenten per ID suchen
    public Optional<Student> findById(Long id) {
        return Optional.ofNullable(students.get(id));
    }

    // Neuen Studenten speichern
    public Student save(Student student) {
        Long id = idCounter.getAndIncrement();  // ID generieren
        student.setId(id);
        students.put(id, student);
        return student;
    }

    // Existierenden Studenten aktualisieren
    public Optional<Student> update(Long id, Student student) {
        if (!students.containsKey(id)) {
            return Optional.empty();  // Student existiert nicht
        }
        student.setId(id);
        students.put(id, student);
        return Optional.of(student);
    }

    // Studenten löschen
    public boolean delete(Long id) {
        return students.remove(id) != null;
    }
}
```

**Was bedeutet `@Singleton`?**

Diese Annotation sagt Micronaut: "Erstelle nur eine einzige Instanz dieser Klasse für die gesamte Anwendung."

Das ist wichtig, weil:
- Alle Controller teilen sich dieselbe Service-Instanz
- Die Studenten-Map ist damit für alle Requests dieselbe
- Ohne `@Singleton` würde für jeden Request ein neuer Service erstellt – und jeder hätte seine eigene (leere) Map

**Warum `ConcurrentHashMap` statt `HashMap`?**

Ein Webserver verarbeitet viele Requests gleichzeitig (parallel). Eine normale `HashMap` ist nicht thread-safe – wenn zwei Requests gleichzeitig schreiben, kann es zu Datenkorruption kommen.

`ConcurrentHashMap` ist dafür gebaut und handhabt parallele Zugriffe sicher.

**Warum `AtomicLong` statt `long`?**

Selber Grund: `long++` ist nicht thread-safe. Wenn zwei Requests gleichzeitig eine neue ID brauchen, könnten sie dieselbe bekommen. `AtomicLong.getAndIncrement()` ist garantiert atomar (unteilbar).

**Warum `Optional<Student>`?**

`Optional` ist ein Container, der entweder einen Wert enthält oder leer ist. Das ist besser als `null` zurückzugeben, weil:
- Der Aufrufer wird gezwungen, den "nicht gefunden"-Fall zu behandeln
- Man bekommt keine `NullPointerException`
- Der Code ist selbstdokumentierend

---

### 4. Controller erstellt (`StudentController.java`)

**Pfad:** `src/main/java/de/dhbw/vs/studentenverwaltung/controller/StudentController.java`

Der vollständige Code:

```java
package de.dhbw.vs.studentenverwaltung.controller;

import de.dhbw.vs.studentenverwaltung.model.Student;
import de.dhbw.vs.studentenverwaltung.service.StudentService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;

import java.util.Collection;

@Controller("/studenten")
public class StudentController {

    private final StudentService studentService;

    // Constructor Injection – Micronaut übergibt automatisch den Service
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // GET /studenten – Alle Studenten abrufen
    @Get
    public Collection<Student> list() {
        return studentService.findAll();
    }

    // GET /studenten/{id} – Einen Studenten abrufen
    @Get("/{id}")
    public HttpResponse<Student> get(Long id) {
        return studentService.findById(id)
                .map(HttpResponse::ok)           // Gefunden → 200 OK
                .orElse(HttpResponse.notFound()); // Nicht gefunden → 404
    }

    // POST /studenten – Neuen Studenten erstellen
    @Post
    public HttpResponse<Student> create(@Body Student student) {
        Student created = studentService.save(student);
        return HttpResponse.created(created);    // 201 Created
    }

    // PUT /studenten/{id} – Studenten aktualisieren
    @Put("/{id}")
    public HttpResponse<Student> update(Long id, @Body Student student) {
        return studentService.update(id, student)
                .map(HttpResponse::ok)           // Aktualisiert → 200 OK
                .orElse(HttpResponse.notFound()); // Nicht gefunden → 404
    }

    // DELETE /studenten/{id} – Studenten löschen
    @Delete("/{id}")
    public HttpResponse<Void> delete(Long id) {
        if (studentService.delete(id)) {
            return HttpResponse.noContent();     // Gelöscht → 204 No Content
        }
        return HttpResponse.notFound();          // Nicht gefunden → 404
    }
}
```

**Die Annotationen im Detail:**

| Annotation | Bedeutung |
|------------|-----------|
| `@Controller("/studenten")` | Diese Klasse ist ein REST-Controller. Alle Endpunkte beginnen mit `/studenten` |
| `@Get` | HTTP GET-Request (Daten abrufen) |
| `@Post` | HTTP POST-Request (neue Daten erstellen) |
| `@Put` | HTTP PUT-Request (bestehende Daten aktualisieren) |
| `@Delete` | HTTP DELETE-Request (Daten löschen) |
| `@Body` | Der Request-Body (JSON) wird in diesen Parameter umgewandelt |
| `/{id}` | Pfad-Variable – die ID aus der URL wird als Parameter übergeben |

**Was ist `HttpResponse<T>`?**

Ein Wrapper, mit dem wir den HTTP-Statuscode kontrollieren können:
- `HttpResponse.ok(data)` → 200 OK mit Daten
- `HttpResponse.created(data)` → 201 Created mit Daten
- `HttpResponse.noContent()` → 204 No Content (erfolgreich, aber keine Daten)
- `HttpResponse.notFound()` → 404 Not Found

**Was ist Constructor Injection?**

```java
public StudentController(StudentService studentService) {
    this.studentService = studentService;
}
```

Micronaut sieht: "Aha, der Controller braucht einen `StudentService`." Es schaut nach, ob es eine Bean von diesem Typ gibt (ja, durch `@Singleton`), erstellt sie und übergibt sie dem Konstruktor.

Das ist **Dependency Injection** – der Controller erstellt seinen Service nicht selbst, sondern bekommt ihn "injiziert". Vorteile:
- Lose Kopplung (man könnte den Service leicht austauschen)
- Bessere Testbarkeit (man kann einen Mock-Service injizieren)
- Weniger Boilerplate-Code

---

## Die einzelnen Klassen erklärt

### `Application.java` – Der Startpunkt

```java
public class Application {
    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
```

Das ist alles. `Micronaut.run()` macht:
1. Scannt nach allen `@Controller`, `@Singleton`, etc.
2. Lädt die generierten `$Definition`-Klassen
3. Startet den Netty HTTP-Server
4. Wartet auf Requests

### `Student.java` – Das Datenmodell

Ein einfaches POJO (Plain Old Java Object) mit:
- Feldern: `id`, `vorname`, `nachname`, `matrikelnummer`, `studiengang`, `semester`
- Gettern und Settern
- Zwei Konstruktoren (leer + mit allen Feldern)
- `@Serdeable` für JSON-Konvertierung

### `StudentService.java` – Die Geschäftslogik

Verwaltet die Studenten-Daten:
- **`findAll()`** – Gibt alle Studenten zurück
- **`findById(id)`** – Sucht einen Studenten, gibt `Optional` zurück (kann leer sein)
- **`save(student)`** – Generiert eine ID und speichert den Studenten
- **`update(id, student)`** – Aktualisiert einen existierenden Studenten
- **`delete(id)`** – Löscht einen Studenten, gibt `true`/`false` zurück

### `StudentController.java` – Die REST-Schnittstelle

Verbindet HTTP-Requests mit der Geschäftslogik:

| HTTP-Methode | Pfad | Methode | Rückgabe |
|--------------|------|---------|----------|
| `GET` | `/studenten` | `list()` | Alle Studenten (200 OK) |
| `GET` | `/studenten/{id}` | `get(id)` | Ein Student (200) oder 404 |
| `POST` | `/studenten` | `create(student)` | Neuer Student (201 Created) |
| `PUT` | `/studenten/{id}` | `update(id, student)` | Aktualisiert (200) oder 404 |
| `DELETE` | `/studenten/{id}` | `delete(id)` | 204 No Content oder 404 |

---

## Wie startet man den Server?

### Voraussetzungen

- **Java 17 oder höher** installiert
- **JAVA_HOME** Umgebungsvariable gesetzt (oder im Terminal setzen)

### Option 1: Mit Maven Wrapper

```powershell
cd C:\Users\tsmel\Downloads\studentenverwaltung
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
.\mvnw.bat mn:run
```

### Option 2: JAR bauen und starten

```powershell
# JAR bauen
.\mvnw.bat package -DskipTests

# JAR starten
java -jar target\studentenverwaltung-0.1.jar
```

### Was du sehen solltest

```
 __  __ _                                  _   
|  \/  (_) ___ _ __ ___  _ __   __ _ _   _| |_ 
| |\/| | |/ __| '__/ _ \| '_ \ / _` | | | | __|
| |  | | | (__| | | (_) | | | | (_| | |_| | |_ 
|_|  |_|_|\___|_|  \___/|_| |_|\__,_|\__,_|\__|
  Micronaut (v4.10.9)

Startup completed in XXXms. Server Running: http://localhost:8090
```

---

## API-Endpunkte testen

### Mit PowerShell

```powershell
# Alle Studenten abrufen
Invoke-RestMethod -Uri "http://localhost:8090/studenten" -Method GET

# Neuen Studenten erstellen
$body = '{"vorname":"Max","nachname":"Mustermann","matrikelnummer":"1234567","studiengang":"Wirtschaftsinformatik","semester":4}'
Invoke-RestMethod -Uri "http://localhost:8090/studenten" -Method POST -Body $body -ContentType "application/json"

# Einzelnen Studenten abrufen
Invoke-RestMethod -Uri "http://localhost:8090/studenten/1" -Method GET

# Studenten aktualisieren
$update = '{"vorname":"Max","nachname":"Mustermann","matrikelnummer":"1234567","studiengang":"Informatik","semester":5}'
Invoke-RestMethod -Uri "http://localhost:8090/studenten/1" -Method PUT -Body $update -ContentType "application/json"

# Studenten löschen
Invoke-RestMethod -Uri "http://localhost:8090/studenten/1" -Method DELETE
```

### Mit dem Browser

- `http://localhost:8090/studenten` – Alle Studenten (JSON)
- `http://localhost:8090/studenten/1` – Student mit ID 1

### Mit Postman oder Insomnia

Für die Live-Demo empfohlen! Erstelle eine Collection mit allen CRUD-Operationen.

---

## Weiterführende Links

### Micronaut Dokumentation
- [User Guide](https://docs.micronaut.io/4.10.9/guide/index.html)
- [API Reference](https://docs.micronaut.io/4.10.9/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/4.10.9/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)

### Verwendete Features
- [Micronaut Maven Plugin](https://micronaut-projects.github.io/micronaut-maven-plugin/latest/)
- [Micronaut Serialization (Jackson)](https://micronaut-projects.github.io/micronaut-serialization/latest/guide/)
- [Micronaut AOT](https://micronaut-projects.github.io/micronaut-aot/latest/guide/)
- [Maven Enforcer Plugin](https://maven.apache.org/enforcer/maven-enforcer-plugin/)

---

*Erstellt für das Modul "Verteilte Systeme" an der DHBW.*
