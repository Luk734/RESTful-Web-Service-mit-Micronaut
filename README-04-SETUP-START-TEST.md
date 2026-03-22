# README 04 - Setup, Start und Tests

Dieses Dokument ist die praktische Anleitung fuer lokale Ausfuehrung und Verifikation der API.

## 1) Voraussetzungen

Ihr braucht lokal:

- Windows mit PowerShell
- JDK 17 oder hoeher
- Internet beim ersten Build (Dependencies werden geladen)

Projekt nutzt Maven Wrapper:

- `mvnw` / `mvnw.bat` ist im Repo enthalten
- separate Maven-Installation ist nicht noetig

---

## 2) Projekt klonen und in Ordner wechseln

```powershell
Set-Location "C:\Users\<dein-user>\Downloads\studentenverwaltung"
Get-ChildItem .\mvnw.bat
```

Wenn `mvnw.bat` gefunden wird, bist du im richtigen Ordner.

---

## 3) Java pruefen

```powershell
java -version
```

Wenn Java nicht gefunden wird, `JAVA_HOME` setzen.

### 3.1 Temporar fuer aktuelles Terminal

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

### 3.2 Dauerhaft fuer neue Terminals (optional)

```powershell
setx JAVA_HOME "C:\Program Files\Java\jdk-23"
```

Danach ein **neues** Terminal oeffnen.

---

## 4) Server starten

```powershell
Set-Location "C:\Users\<dein-user>\Downloads\studentenverwaltung"
.\mvnw.bat mn:run
```

Info:

- App-Port ist in `src/main/resources/application.properties` auf `8090` gesetzt.
- API-Basis ist damit: `http://localhost:8090/studenten`

Server stoppen:

```powershell
Ctrl + C
```

---

## 5) Automatische Tests ausfuehren

In separatem Terminal:

```powershell
Set-Location "C:\Users\<dein-user>\Downloads\studentenverwaltung"
.\mvnw.bat test
```

Was geprueft wird:

- kompletter CRUD-Ablauf
- Fehlerfall ungueltiger Body (`400`)
- Fehlerfall doppelte Matrikelnummer (`409`)

Hinweis:

- Tests laufen auf zufaelligem Port und kollidieren deshalb nicht mit einem lokal laufenden Server auf `8090`.

---

## 6) Manuelle API-Tests mit PowerShell

In einem zweiten Terminal (waehrend Server laeuft):

```powershell
$base = "http://localhost:8090/studenten"
```

### 6.1 Student erstellen (POST)

```powershell
$body = @{
  vorname = "Max"
  nachname = "Mustermann"
  matrikelnummer = "1234567"
  studiengang = "Wirtschaftsinformatik"
  semester = 4
} | ConvertTo-Json

$created = Invoke-RestMethod -Uri $base -Method POST -Body $body -ContentType "application/json"
$created | ConvertTo-Json
```

### 6.2 Alle Studenten lesen (GET)

```powershell
Invoke-RestMethod -Uri $base -Method GET | ConvertTo-Json -Depth 5
```

### 6.3 Einen Studenten lesen (GET/{id})

```powershell
$id = $created.id
Invoke-RestMethod -Uri "$base/$id" -Method GET | ConvertTo-Json
```

### 6.4 Student aktualisieren (PUT/{id})

```powershell
$update = @{
  vorname = "Max"
  nachname = "Mustermann"
  matrikelnummer = "1234567"
  studiengang = "Informatik"
  semester = 5
} | ConvertTo-Json

Invoke-RestMethod -Uri "$base/$id" -Method PUT -Body $update -ContentType "application/json" | ConvertTo-Json
```

### 6.5 Student loeschen (DELETE/{id})

```powershell
Invoke-RestMethod -Uri "$base/$id" -Method DELETE
```

---

## 7) Wichtige Negativtests

### 7.1 Ungueltige Daten -> 400

```powershell
$invalid = @{
  vorname = ""
  nachname = ""
  matrikelnummer = "12ab"
  studiengang = ""
  semester = 0
} | ConvertTo-Json

try {
  Invoke-RestMethod -Uri $base -Method POST -Body $invalid -ContentType "application/json"
} catch {
  $_.Exception.Response.StatusCode.value__
}
```

### 7.2 Doppelte Matrikelnummer -> 409

```powershell
$one = @{
  vorname = "Anna"
  nachname = "Meyer"
  matrikelnummer = "7654321"
  studiengang = "Informatik"
  semester = 2
} | ConvertTo-Json

$two = @{
  vorname = "Ben"
  nachname = "Schmitt"
  matrikelnummer = "7654321"
  studiengang = "Informatik"
  semester = 2
} | ConvertTo-Json

Invoke-RestMethod -Uri $base -Method POST -Body $one -ContentType "application/json"

try {
  Invoke-RestMethod -Uri $base -Method POST -Body $two -ContentType "application/json"
} catch {
  $_.Exception.Response.StatusCode.value__
}
```

---

## 8) Typische Fehler und Loesungen

### Fehler: `JAVA_HOME not found`

Loesung:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

### Fehler: `mvnw.bat` nicht gefunden

Du bist im falschen Ordner. Erst wechseln:

```powershell
Set-Location "C:\Users\<dein-user>\Downloads\studentenverwaltung"
Get-ChildItem .\mvnw.bat
```

### Fehler: Port 8090 bereits belegt

Entweder laufenden Prozess stoppen oder Port in `application.properties` aendern.

---

## 9) Schnellablauf (3 Befehle)

```powershell
Set-Location "C:\Users\<dein-user>\Downloads\studentenverwaltung"
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
.\mvnw.bat mn:run
```

In zweitem Terminal:

```powershell
Set-Location "C:\Users\<dein-user>\Downloads\studentenverwaltung"
.\mvnw.bat test
```

Damit hast du Start + automatischen Funktionsnachweis.
