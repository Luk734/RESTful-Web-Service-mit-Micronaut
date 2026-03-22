# README 01 - Änderungen gegenüber der Micronaut-Vorlage

Dieses Dokument zeigt **nur die konkreten Unterschiede** zwischen dem generierten Micronaut-Startprojekt und eurem aktuellen Projektstand.

## 1) Ausgangspunkt (Micronaut-Template)

Das generierte Template liefert im Kern:

- `Application.java` als Einstiegspunkt (`Micronaut.run(...)`)
- `pom.xml` mit Basis-Abhängigkeiten (Server, JSON, Tests)
- `application.properties` mit App-Name
- einen einfachen Smoke-Test, der nur prüft, ob die App startet

Was **nicht** enthalten war:

- kein fachliches Datenmodell
- keine REST-Endpunkte
- keine Geschäftslogik
- keine CRUD-Funktionalitaet

---

## 2) Neue Fachlogik und API

### Neu: Datenmodell

Datei: `src/main/java/de/dhbw/vs/studentenverwaltung/model/Student.java`

Ergänzt wurden Felder für die Studentenverwaltung:

- `id`
- `vorname`
- `nachname`
- `matrikelnummer`
- `studiengang`
- `semester`

Zusatz gegenüber einer reinen POJO-Klasse:

- `@Serdeable` für JSON-Serialisierung in Micronaut

### Neu: Service-Schicht

Datei: `src/main/java/de/dhbw/vs/studentenverwaltung/service/StudentService.java`

Ergänzt wurden:

- In-Memory-Speicher mit `ConcurrentHashMap<Long, Student>`
- ID-Generierung mit `AtomicLong`
- CRUD-Methoden:
  - `findAll`
  - `findById`
  - `save`
  - `update`
  - `delete`

### Neu: Controller-Schicht

Datei: `src/main/java/de/dhbw/vs/studentenverwaltung/controller/StudentController.java`

Ergänzt wurden REST-Endpunkte unter `/studenten`:

- `GET /studenten`
- `GET /studenten/{id}`
- `POST /studenten`
- `PUT /studenten/{id}`
- `DELETE /studenten/{id}`

HTTP-Statuscodes sind bewusst genutzt:

- `200 OK` bei erfolgreichem Lesen/Aendern
- `201 Created` beim Erstellen
- `204 No Content` beim erfolgreichen Löschen
- `404 Not Found` bei unbekannter ID

---

## 3) Fachliche Erweiterungen nachträglich

Diese Punkte wurden zusätzlich eingebaut, damit die API robuster ist:

### Validierung der Eingaben (400 Bad Request)

Datei: `Student.java` + `StudentController.java`

- `@NotBlank` fuer `vorname`, `nachname`, `matrikelnummer`, `studiengang`
- `@Pattern("\\d{7}")` fuer exakt 7-stellige `matrikelnummer`
- `@Min(1)` fuer `semester`

Ungültige Daten liefern jetzt `400 Bad Request`.

### Duplikat-Schutz bei Matrikelnummer (409 Conflict)

Datei: `StudentService.java` + `StudentController.java`

- zusätzlicher Index `ConcurrentHashMap<String, Long> matrikelnummern`
- Verhinderung doppelter Matrikelnummern bei `POST` und `PUT`
- bei Konflikt: `409 Conflict`
- Sonderfall `PUT`: gleiche Matrikelnummer beim **gleichen** Studenten bleibt erlaubt

---

## 4) Test-Upgrade

Datei: `src/test/java/de/dhbw/vs/StudentenverwaltungTest.java`

Vorher:

- 1 Smoke-Test (`application.isRunning()`)

Nachher:

- `testCrudLifecycle()` prüft End-to-End CRUD
- `testValidationAndDuplicateMatrikelnummer()` prüft Fehlerfälle:
  - `400` bei ungültigem Request-Body
  - `409` bei doppelter Matrikelnummer

Zusaetzlich:

- Testserver läuft mit `micronaut.server.port=-1` auf zufälligem Port, damit Tests nicht mit lokalem `8090` kollidieren.

---

## 5) Build-Konfiguration erweitert

Datei: `pom.xml`

Neu hinzugefügte Runtime-Abhängigkeiten:

- `io.micronaut.validation:micronaut-validation`
- `io.micronaut:micronaut-http-validation`

Damit sind Bean-Validation und HTTP-Validierung im laufenden Service aktiv.

---

## 6) Konfiguration angepasst

Datei: `src/main/resources/application.properties`

Ergänzung:

- `micronaut.server.port=8090`

Damit läuft die Anwendung standardmässig auf Port `8090` statt `8080`.
