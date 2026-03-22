# README 02 - Code-Erklaerung (ausfuehrlich)

Dieses Dokument erklaert den aktuellen Code mit Fokus auf die Stellen, die fuer das Verstaendnis wichtig sind (Architektur, Datenfluss, Validierung, Fehlerbehandlung, Nebenlaeufigkeit).

## 1) Gesamtarchitektur

Das Projekt ist in 3 Schichten aufgeteilt:

- **Controller** (`StudentController`): HTTP-Eingang, Statuscodes, Request/Response
- **Service** (`StudentService`): Fachlogik und In-Memory-Datenhaltung
- **Model** (`Student`): Datenstruktur und Validierungsregeln

Ablauf einer Anfrage:

1. Client sendet HTTP-Request an Controller.
2. Controller validiert Eingaben und ruft Service auf.
3. Service verarbeitet Daten und liefert Ergebnisstatus.
4. Controller uebersetzt Ergebnis in passenden HTTP-Status.

---

## 2) `Application.java`

Datei: `src/main/java/de/dhbw/vs/studentenverwaltung/Application.java`

- `Micronaut.run(...)` startet den Application Context.
- Micronaut findet automatisch Beans wie `StudentController` und `StudentService`.
- Danach lauscht Netty auf dem konfigurierten Port (`8090`).

Wichtig: Diese Klasse enthaelt absichtlich keine Fachlogik.

---

## 3) `Student.java` (Modell + Regeln)

Datei: `src/main/java/de/dhbw/vs/studentenverwaltung/model/Student.java`

### Struktur

- `id`: interne ID
- `vorname`, `nachname`, `matrikelnummer`, `studiengang`, `semester`: Fachdaten

### Relevante Annotationen

- `@Serdeable`
  - erlaubt Serialisierung/Deserialisierung zwischen Java-Objekt und JSON

- `@NotBlank` auf String-Feldern
  - verbietet `null`, leere Strings und reine Leerzeichen

- `@Pattern(regexp = "\\d{7}")` auf `matrikelnummer`
  - erzwingt genau 7 Ziffern
  - Beispiele:
    - gueltig: `1234567`
    - ungueltig: `12345`, `12ab567`

- `@Min(1)` auf `semester`
  - Semester muss mindestens 1 sein

Warum das wichtig ist:

- Regeln liegen direkt am Datenmodell.
- Gleiche Regeln gelten fuer `POST` und `PUT`.
- Fehler werden frueh abgefangen, bevor Daten gespeichert werden.

---

## 4) `StudentService.java` (Fachlogik)

Datei: `src/main/java/de/dhbw/vs/studentenverwaltung/service/StudentService.java`

### Datenhaltung

- `students: ConcurrentHashMap<Long, Student>`
  - Hauptspeicher: ID -> Student

- `matrikelnummern: ConcurrentHashMap<String, Long>`
  - Zusatzindex fuer Eindeutigkeit der Matrikelnummer

- `idCounter: AtomicLong`
  - erzeugt neue IDs aufsteigend (`1, 2, 3, ...`)

### Warum `synchronized` bei `save`, `update`, `delete`?

Obwohl `ConcurrentHashMap` thread-safe ist, bestehen diese Methoden aus **mehreren Schritten** (lesen + schreiben in 2 Maps). `synchronized` sorgt dafuer, dass diese Schrittfolgen atomar bleiben.

### Methoden im Detail

- `findAll()`
  - liefert alle Studentenwerte

- `findById(Long id)`
  - gibt `Optional<Student>` zurueck
  - kein Treffer -> `Optional.empty()`

- `save(Student student)`
  1. prueft, ob Matrikelnummer schon existiert
  2. vergibt neue ID
  3. schreibt in beide Maps
  4. liefert `Optional.of(student)` oder `Optional.empty()` bei Duplikat

- `update(Long id, Student student)`
  - liefert `UpdateStatus`:
    - `UPDATED`
    - `NOT_FOUND`
    - `DUPLICATE_MATRIKELNUMMER`
  - erlaubt gleiche Matrikelnummer, wenn sie bereits zu genau dieser ID gehoert

- `delete(Long id)`
  - loescht Student aus Hauptmap
  - loescht zugehoerigen Matrikelnummer-Eintrag

---

## 5) `StudentController.java` (HTTP-Schicht)

Datei: `src/main/java/de/dhbw/vs/studentenverwaltung/controller/StudentController.java`

### Controller-Setup

- `@Controller("/studenten")`: Basisroute fuer alle Endpunkte
- `@Validated`: aktiviert methodenbezogene Validierung im Controller
- Constructor Injection:
  - `StudentService` fuer Fachlogik
  - `Validator` fuer explizite Bean-Validation

### Endpunkte

- `GET /studenten` -> `list()`
  - Antwort: `200` mit Liste

- `GET /studenten/{id}` -> `get(Long id)`
  - Treffer: `200`
  - kein Treffer: `404`

- `POST /studenten` -> `create(@Body @Valid Student student)`
  1. `validateStudent(student)` prueft Constraints
  2. `save(...)` wird aufgerufen
  3. bei Duplikat -> `409 Conflict`
  4. sonst -> `201 Created`

- `PUT /studenten/{id}` -> `update(Long id, @Body @Valid Student student)`
  1. Validierung
  2. Service-Status auswerten
  3. `NOT_FOUND` -> `404`
  4. `DUPLICATE_MATRIKELNUMMER` -> `409`
  5. `UPDATED` -> `200`

- `DELETE /studenten/{id}` -> `delete(Long id)`
  - geloescht: `204 No Content`
  - nicht gefunden: `404`

### `validateStudent(Student student)`

- ruft `validator.validate(student)` auf
- nimmt bei Fehlern die erste Verletzung
- wirft `HttpStatusException(BAD_REQUEST, ...)`

Ergebnis: ungueltige Daten fuehren zu `400` statt in den Speicher zu gelangen.

---

## 6) `StudentenverwaltungTest.java` (Integrationstests)

Datei: `src/test/java/de/dhbw/vs/StudentenverwaltungTest.java`

### Test-Setup

- `@MicronautTest`: startet App-Kontext und HTTP-Schicht im Test
- `@Property(name = "micronaut.server.port", value = "-1")`: zufaelliger Port
- `@Client("/") HttpClient`: direkter Test-HTTP-Client gegen laufende Test-App

### Test 1: `testCrudLifecycle()`

Prueft den kompletten Lebenszyklus:

1. `POST` erstellt Student
2. `GET` liest Student
3. `PUT` aktualisiert Student
4. `DELETE` loescht Student
5. erneutes `GET` liefert `404`

Nutzen:

- Sicherung des Happy Paths ueber alle Kernendpunkte.

### Test 2: `testValidationAndDuplicateMatrikelnummer()`

Prueft Fehlerfaelle:

1. ungueltiger Body -> `400 Bad Request`
2. doppelte Matrikelnummer -> `409 Conflict`

Nutzen:

- API-Verhalten bei ungueltigen/fachlich inkonsistenten Requests ist abgesichert.

---

## 7) Warum diese Struktur fuer VS sinnvoll ist

- Trennung von HTTP und Fachlogik ist klar.
- REST-Statuscodes spiegeln den fachlichen Zustand.
- In-Memory-Speicher ist fuer Demo/Lernen schnell und transparent.
- Integrationstests liefern reproduzierbaren Nachweis fuer Funktionsfaehigkeit.
