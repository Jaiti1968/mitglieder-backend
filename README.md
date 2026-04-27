# EMC Mitgliederverwaltung – Backend

Spring Boot Backend für die Verwaltung von Mitgliedsdaten des EMC Männerchors.

Das Backend stellt eine REST-API bereit, kapselt den Zugriff auf MariaDB und wird perspektivisch als Docker-Container auf dem NAS DH2300 betrieben.

---

## Technologien

- Java 21
- Spring Boot 3
- Spring Web / REST
- Spring JDBC (`JdbcTemplate`)
- MariaDB
- Maven
- Docker, geplant für Deployment auf NAS

---

## Architektur

Das Backend ist schichtenorientiert aufgebaut:

```text
Controller -> Service -> Repository -> MariaDB
```

### Verantwortlichkeiten

- **Controller**: REST-Endpunkte, HTTP Request/Response
- **Service**: Geschäftslogik, Transaktionen, Validierung
- **Repository**: SQL-Zugriffe mit `JdbcTemplate`
- **DTOs**:
  - `dto.member` -> Response- und Fachmodelle
  - `dto.request` -> Request-Modelle
- **Mapper**: Mapping von SQL-ResultSets auf DTOs
- **Exception Handling**: zentrale Fehlerbehandlung über `GlobalExceptionHandler`

---

## Datenbank

MariaDB läuft auf dem NAS DH2300.

Aktuelle Entwicklungsdatenbank:

```properties
spring.datasource.url=jdbc:mariadb://<NAS-IP>:3306/emc_mitglieder_dev
spring.datasource.username=...
spring.datasource.password=...
```

Produktiv wird später eine separate Konfiguration verwendet.

### Wichtige Tabellen im MVP

- `tblMitglieder`
- `tblKontaktdaten`
- `tblMitgliedschaft`
- `tblDatenschutz`
- `tblChorkleidung`
- `tblAllgemein_FT`
- `tblMitgliederstatus_FT`
- `tblStimme_FT`

### Datenbankregeln

Die Datenbank nutzt inzwischen verbindlichere Regeln:

- InnoDB
- Foreign Keys
- `ON DELETE CASCADE`
- `UNIQUE`
- `NOT NULL`
- Default-Werte bei Boolean-Feldern

Das Backend berücksichtigt diese Regeln durch:

- DTO-Validierung
- fachliche Validierung im Service
- transaktionale Anlage neuer Mitglieder
- zentrale Behandlung von Datenbankfehlern

---

## Mitgliedsnummer

Die Mitgliedsnummer wird vom Backend erzeugt.

Ablauf bei `POST /api/members`:

1. Lesen von `tblAllgemein_FT.neueMitgliedsnummer`
2. Diese Nummer wird für das neue Mitglied verwendet
3. Die Nummer wird um 1 erhöht
4. Der neue Wert wird in `tblAllgemein_FT.neueMitgliedsnummer` zurückgeschrieben
5. Danach werden die Datensätze in den beteiligten Tabellen angelegt

Die Nummernvergabe ist transaktionssicher abgesichert:

```sql
SELECT neueMitgliedsnummer
FROM tblAllgemein_FT
FOR UPDATE
```

Dadurch wird verhindert, dass zwei gleichzeitige Requests dieselbe Mitgliedsnummer vergeben.

---

## Fachliches Datenmodell

Ein Mitglied wird im Backend als zentrales Objekt betrachtet und in drei Bereiche aufgeteilt:

```json
{
  "mitgliedsnummer": "N1234",
  "stammdaten": {},
  "kontakt": {},
  "mitgliedschaft": {}
}
```

### Stammdaten

Quelle: `tblMitglieder`

```json
{
  "anrede": "Herr",
  "akademischerTitel": "",
  "vorname": "Max",
  "nachname": "Mustermann",
  "plz": "99084",
  "ort": "Erfurt",
  "strasseHausNr": "Musterstraße 1",
  "geburtsdatum": "1980-05-20"
}
```

### Kontakt

Quelle: `tblKontaktdaten`

```json
{
  "telefonPrivat": "0361...",
  "telefonGeschaeftlich": "0361...",
  "mobiltelefon": "0151...",
  "email": "max@example.de",
  "adresszusatz": "",
  "briefanrede": "Lieber Sangesfreund Max Mustermann"
}
```

Wichtige DB-Längen:

- `EMail`: `varchar(100)`
- `Adresszusatz`: `varchar(50)`

### Mitgliedschaft

Quelle: `tblMitgliedschaft` plus Lookup-Tabellen

```json
{
  "eintritt": "2024-01-01",
  "austritt": null,
  "mitgliedsstatusId": 4,
  "mitgliedsstatus": "Kandidat",
  "stimmeId": 6,
  "stimme": "keine",
  "kammerchor": false
}
```

---

## API-Endpunkte

### Lookups

```http
GET /api/lookups/member-status
GET /api/lookups/voices
```

Antwortformat:

```json
[
  { "id": 1, "label": "Aktives Mitglied" },
  { "id": 4, "label": "Kandidat" }
]
```

---

### Mitgliederliste

```http
GET /api/members
```

Query-Parameter:

| Parameter | Bedeutung |
|---|---|
| `search` | Freitextsuche über Mitgliedsnummer, Vorname, Nachname, Ort |
| `statusId` | Filter nach Mitgliederstatus, mehrfach möglich |
| `stimmeId` | Filter nach Stimme, mehrfach möglich |
| `page` | Seitennummer, Start bei 1 |
| `pageSize` | Anzahl Datensätze pro Seite |

Beispiele:

```http
GET /api/members?page=1&pageSize=20
GET /api/members?search=wolf
GET /api/members?statusId=1&statusId=4
GET /api/members?stimmeId=1&stimmeId=3
GET /api/members?statusId=1&statusId=4&stimmeId=1&stimmeId=3&page=1&pageSize=20
```

Mehrfachauswahl wird über mehrfach gesetzte Query-Parameter umgesetzt, nicht über kommaseparierte Werte.

Frontend-Beispiel:

```js
const params = new URLSearchParams();

statusIds.forEach(id => params.append("statusId", id));
stimmeIds.forEach(id => params.append("stimmeId", id));
```

Antwortformat:

```json
{
  "items": [
    {
      "mitgliedsnummer": "N1139",
      "vorname": "Christian",
      "nachname": "Wolf",
      "ort": "Erfurt",
      "mitgliedsstatusId": 3,
      "mitgliedsstatus": "Angestellter",
      "stimmeId": 5,
      "stimme": "Chorleiter"
    }
  ],
  "pagination": {
    "page": 1,
    "pageSize": 20,
    "totalItems": 155,
    "totalPages": 8
  }
}
```

---

### Mitglied Detail

```http
GET /api/members/{mitgliedsnummer}
```

Beispiel:

```http
GET /api/members/N1139
```

Antwortformat:

```json
{
  "mitgliedsnummer": "N1139",
  "stammdaten": {
    "anrede": "Herr",
    "akademischerTitel": "",
    "vorname": "Christian",
    "nachname": "Wolf",
    "plz": "99084",
    "ort": "Erfurt",
    "strasseHausNr": "Musterstraße 1",
    "geburtsdatum": "1980-05-20"
  },
  "kontakt": {
    "telefonPrivat": "0361...",
    "telefonGeschaeftlich": "0361...",
    "mobiltelefon": "0157...",
    "email": "christian@example.de",
    "adresszusatz": null,
    "briefanrede": "Lieber Sangesfreund Christian Wolf"
  },
  "mitgliedschaft": {
    "eintritt": "2024-01-01",
    "austritt": null,
    "mitgliedsstatusId": 3,
    "mitgliedsstatus": "Angestellter",
    "stimmeId": 5,
    "stimme": "Chorleiter",
    "kammerchor": false
  }
}
```

---

### Mitglied anlegen

```http
POST /api/members
```

Die Mitgliedsnummer wird nicht vom Frontend übergeben, sondern im Backend erzeugt.

Beispiel-Request:

```json
{
  "stammdaten": {
    "anrede": "Herr",
    "akademischerTitel": "",
    "vorname": "Max",
    "nachname": "Mustermann",
    "plz": "99084",
    "ort": "Erfurt",
    "strasseHausNr": "Musterstraße 1",
    "geburtsdatum": "1980-05-20"
  },
  "kontakt": {
    "email": "max@example.de",
    "briefanrede": "Lieber Sangesfreund Max Mustermann"
  },
  "mitgliedschaft": {
    "eintritt": "2024-01-01",
    "austritt": null,
    "mitgliedsstatusId": 4,
    "stimmeId": 6,
    "kammerchor": false
  }
}
```

Beim Anlegen werden Datensätze in folgenden Tabellen erzeugt:

- `tblMitglieder`
- `tblKontaktdaten`
- `tblMitgliedschaft`
- `tblDatenschutz`
- `tblChorkleidung`

Die Anlage läuft transaktional. Schlägt ein Insert fehl, wird die gesamte Anlage zurückgerollt.

Standardwerte, falls nicht übergeben:

| Feld | Default |
|---|---|
| `Anrede` | `Herr` |
| `AkademischerTitel` | leerer String |
| `IDMitgliederstatus` | `4` |
| `IDStimme` | `6` |
| `Kammerchor` | `false` |
| `Briefanrede` | `Lieber Sangesfreund` |
| `Neubeschaffung` | `false` |
| `Barzahlung` | `false` |

Antwort: Der neu angelegte Mitgliederdatensatz im Detailformat.

---

### Stammdaten ändern

```http
PUT /api/members/{mitgliedsnummer}/stammdaten
```

Beispiel:

```json
{
  "anrede": "Herr",
  "akademischerTitel": "Dr.",
  "vorname": "Max",
  "nachname": "Mustermann",
  "plz": "99084",
  "ort": "Erfurt",
  "strasseHausNr": "Musterstraße 1",
  "geburtsdatum": "1980-05-20"
}
```

Antwort: Der aktualisierte Mitgliederdatensatz im Detailformat.

---

### Kontakt ändern

```http
PUT /api/members/{mitgliedsnummer}/kontakt
```

Beispiel:

```json
{
  "telefonPrivat": "0361...",
  "telefonGeschaeftlich": "0361...",
  "mobiltelefon": "0151...",
  "email": "max@example.de",
  "adresszusatz": "c/o Beispiel",
  "briefanrede": "Lieber Sangesfreund Max Mustermann"
}
```

Antwort: Der aktualisierte Mitgliederdatensatz im Detailformat.

---

### Mitgliedschaft ändern

```http
PUT /api/members/{mitgliedsnummer}/mitgliedschaft
```

Beispiel:

```json
{
  "eintritt": "2024-01-01",
  "austritt": null,
  "mitgliedsstatusId": 4,
  "stimmeId": 6,
  "kammerchor": false
}
```

Antwort: Der aktualisierte Mitgliederdatensatz im Detailformat.

---

## Validierung

### DTO-Validierung

Beispiele:

- Vorname und Nachname dürfen nicht leer sein.
- E-Mail muss gültig sein, sofern angegeben.
- E-Mail darf maximal 100 Zeichen haben.
- Adresszusatz darf maximal 50 Zeichen haben.
- Mitgliederstatus und Stimme müssen angegeben werden, wenn Mitgliedschaft aktualisiert wird.

### Fachliche Validierung

- `mitgliedsstatusId` muss in `tblMitgliederstatus_FT` existieren.
- `stimmeId` muss in `tblStimme_FT` existieren.

### Datenbankvalidierung

Die Datenbank erzwingt zusätzlich Regeln über:

- Foreign Keys
- `NOT NULL`
- `UNIQUE`
- Default-Werte

Verletzungen werden zentral im Backend abgefangen und als passende HTTP-Fehler zurückgegeben.

---

## Fehlerhandling

Standardisierte Fehlerantwort:

```json
{
  "timestamp": "2026-04-27T16:22:19.1975745",
  "status": 400,
  "error": "Bad Request",
  "message": "Ungültige Daten oder Verstoß gegen Datenbankregeln",
  "path": "/api/members/N1139/kontakt"
}
```

Statuscodes:

| Status | Bedeutung |
|---|---|
| `400 Bad Request` | Validierungsfehler, Foreign-Key-Verstoß, NOT NULL-Verstoß, sonstige DB-Regelverletzung |
| `404 Not Found` | Mitglied wurde nicht gefunden |
| `409 Conflict` | Duplicate Key / UNIQUE-Verstoß |
| `500 Internal Server Error` | Unerwarteter Serverfehler |

---

## Logging

- Unerwartete Fehler werden mit `log.error(...)` inklusive Stacktrace geloggt.
- Validierungs- und fachliche Fehler werden mit `log.warn(...)` geloggt.
- Das Frontend erhält keine internen Stacktraces.

---

## Sicherheit und Zugriff

- Das Frontend greift ausschließlich über das Backend auf Daten zu.
- Kein direkter Datenbankzugriff aus dem Frontend.
- Datenbankzugriff erfolgt über einen dedizierten App-User.
- Zugriff außerhalb des Heimnetzes erfolgt über VPN.
- MariaDB-Port 3306 soll nicht direkt ins Internet freigegeben werden.

---

## Deployment auf NAS / Docker

Geplant:

- Spring Boot Backend als Docker-Container
- MariaDB läuft auf dem NAS DH2300
- Frontend später als separater Container oder statischer Build über Nginx

Typischer Build-Ablauf:

```bash
mvn clean package
docker build -t emc-backend .
docker run -d -p 8080:8080 emc-backend
```

Konfiguration sollte später über Umgebungsvariablen erfolgen, nicht über eingecheckte Zugangsdaten.

---

## Test

Empfohlenes Testwerkzeug: Postman.

Wichtige Tests:

- `GET /api/lookups/member-status`
- `GET /api/lookups/voices`
- `GET /api/members`
- `GET /api/members?statusId=1&statusId=4`
- `GET /api/members?stimmeId=1&stimmeId=3`
- `GET /api/members/{mitgliedsnummer}`
- `POST /api/members`
- `PUT /api/members/{mitgliedsnummer}/stammdaten`
- `PUT /api/members/{mitgliedsnummer}/kontakt`
- `PUT /api/members/{mitgliedsnummer}/mitgliedschaft`

---

## Aktueller Status

| Bereich | Status |
|---|---|
| Lookup-Endpunkte | fertig |
| Mitgliederliste | fertig |
| Suche | fertig |
| Multi-Select-Filter | fertig |
| Pagination | fertig |
| Detailansicht | fertig |
| Mitglied anlegen | fertig |
| Stammdaten ändern | fertig |
| Kontakt ändern | fertig |
| Mitgliedschaft ändern | fertig |
| DTO-Validierung | fertig |
| DB-Fehlerhandling | fertig |
| Logging | fertig |
| Docker-Deployment | geplant |
| Login / Auth | offen |
| DELETE Mitglied | optional / offen |

---

## Nicht im MVP enthalten

- Ehrungen
- Funktionen
- Verteiler
- Chorkleidung im Frontend
- Datenschutz im Frontend
- Berichte
- Historie
- Benutzerverwaltung
- Rechte je Datenfeld
