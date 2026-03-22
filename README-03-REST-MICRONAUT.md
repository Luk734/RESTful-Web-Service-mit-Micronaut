# README 03 - RESTful Web Services mit Micronaut

Dieses Dokument erklaert die fachliche und technische Idee hinter eurem Projekt: **RESTful API** umgesetzt mit **Micronaut**.

## 1) Was ist ein RESTful Web Service?

REST (Representational State Transfer) ist ein Architekturstil für verteilte Systeme. Eine REST-API stellt Ressourcen über HTTP bereit.

In eurem Projekt ist die Ressource:

- `Student`

Die Ressource wird über URI adressiert:

- Sammlung: `/studenten`
- einzelnes Element: `/studenten/{id}`

### Typische HTTP-Methoden

- `GET` -> lesen
- `POST` -> neu anlegen
- `PUT` -> ersetzen/aktualisieren
- `DELETE` -> löschen

### Wichtige REST-Prinzipien

- **Stateless**: jeder Request enthält alle nötigen Infos
- **Ressourcenorientierung**: URL beschreibt ein Objekt, nicht eine Aktion
- **Standardisierte Semantik**: Methoden und Statuscodes haben klare Bedeutung

---

## 2) REST auf eurem API-Design

Eure API folgt diesem Mapping:

- `GET /studenten` -> Liste aller Studenten
- `GET /studenten/{id}` -> ein Student
- `POST /studenten` -> Student erstellen
- `PUT /studenten/{id}` -> Student aktualisieren
- `DELETE /studenten/{id}` -> Student löschen

Statuscodes:

- `200` bei erfolgreichem Lesen/Aktualisieren
- `201` bei erfolgreichem Erstellen
- `204` bei erfolgreichem Löschen ohne Response-Body
- `400` bei Validierungsfehlern
- `404` bei nicht gefundener ID
- `409` bei Konflikt (doppelte Matrikelnummer)

Das ist REST-konform und für Clients gut vorhersagbar.

---

## 3) Welche Rolle spielt Micronaut dabei?

Micronaut ist nicht nur ein HTTP-Router, sondern ein Framework für performante, cloud-nahe Services.

### Kernrolle in eurem Projekt

- Routing (`@Controller`, `@Get`, `@Post`, `@Put`, `@Delete`)
- Dependency Injection (z. B. `StudentService` in `StudentController`)
- JSON-Handling (`@Serdeable` + Serde/Jackson)
- Validation-Integration (`jakarta.validation` + Micronaut Validation)
- Test-Support (`@MicronautTest`, `@Client`)

### Was Micronaut besonders macht

Micronaut arbeitet stark mit **Compile-Time-Metadaten** statt Reflection zur Laufzeit.

Praktischer Effekt:

- schneller Start
- geringer Speicherverbrauch
- gut für Microservices, Container und kurze Startzyklen

---

## 4) Bezug zur Micronaut-Vorlage

Wenn man ein Projekt mit Micronaut Launch erzeugt, bekommt man ein lauffähiges, aber fachlich leeres Grundgerüst.

Enthalten sind typischerweise:

- Startklasse (`Application.java`)
- Build-Setup (`pom.xml`)
- Logging/Config-Dateien
- Basis-Test

Eure Arbeit bestand darin, diese Vorlage in eine fachliche API zu überführen:

1. Modell hinzugefügt (`Student`)
2. Service eingeführt (`StudentService`)
3. REST-Endpunkte erstellt (`StudentController`)
4. Regeln und Fehlerfälle eingebaut (Validation, 409-Konflikt)
5. Integrationstests als Nachweis ergänzt

---

## 5) Wie Requests intern verarbeitet werden

Am Beispiel `POST /studenten`:

1. HTTP-Request trifft am Micronaut-Router ein.
2. Micronaut mappt auf `StudentController#create`.
3. Request-Body wird in ein `Student`-Objekt konvertiert.
4. Validierung wird ausgeführt.
5. Service prüft fachliche Kollisionen (Matrikelnummer).
6. Bei Erfolg: `201 Created`; bei Konflikt: `409`; bei ungültigen Daten: `400`.

Damit sieht man klar die Trennung:

- Protokollfragen (HTTP) im Controller
- Fachentscheidungen im Service

---

## 7) Grenzen und naechste sinnvolle Ausbaustufen

Aktuelle Grenzen:

- keine Persistenz (Daten sind nach Neustart weg)
- keine Authentifizierung/Autorisierung
- keine Pagination/Filter

Sinnvolle spaetere Erweiterungen:

- persistenter Speicher (z. B. PostgreSQL)
- OpenAPI/Swagger-Doku
- Sicherheitskonzept (JWT/Role-based)
- weitere Integrations- und Lasttests

Diese Punkte sind **nicht** notwendig, um den aktuellen REST-Kern zu verstehen, aber hilfreich fuer ein reales Produktionssystem.
