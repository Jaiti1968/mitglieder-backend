# EMC Mitgliederverwaltung – Backend

Spring Boot Backend für die Verwaltung der Mitgliedsdaten des EMC Männerchors.

Das Backend stellt eine REST-API bereit, kapselt den Zugriff auf MariaDB und läuft als Docker-Container auf dem NAS DH2300. Das Frontend wird separat als React-Anwendung umgesetzt.

---

# Technologien

- Java 21
- Spring Boot 3
- Spring Web REST
- Spring JDBC (`JdbcTemplate`)
- MariaDB
- Maven
- Docker
- Portainer
- Lombok
- Jakarta Validation

---

# Architektur

Das Backend folgt einer klassischen Schichtenarchitektur:

```text
Controller
→ Service
→ Repository
→ MariaDB
```

## Verantwortlichkeiten

### Controller

- REST-Endpunkte
- HTTP Request/Response
- Übergabe der Request DTOs
- Response-Serialisierung

### Service

- Geschäftslogik
- fachliche Validierung
- Transaktionen
- Fehlerbehandlung

### Repository

- SQL-Zugriffe mit `JdbcTemplate`
- Mapping von ResultSets
- Datenbankoperationen

### DTOs

#### dto.member

Response- und Fachmodelle

#### dto.request

Request DTOs

#### dto.error

Fehler-DTOs und Validierungsfehler

---

# Projektstruktur

```text
src/main/java/de/emc/mitglieder
├── constant
├── controller
├── dto
│   ├── error
│   ├── member
│   └── request
├── exception
├── logging
├── mapper
├── repository
├── service
└── validation
```

---

# Datenbank

MariaDB läuft auf dem NAS DH2300.

## Konfiguration

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
```

## Beispiel Environment Variables

```text
DB_URL=jdbc:mariadb://192.168.xxx.xxx:3306/emc_mitglieder_dev
DB_USERNAME=emc_mitglieder_rw
DB_PASSWORD=********
```

---

# Wichtige Tabellen

## Kernbereiche

- tblMitglieder
- tblKontaktdaten
- tblMitgliedschaft

## Erweiterungsbereiche

- tblDatenschutz
- tblChorkleidung

## Lookup-Tabellen

- tblMitgliederstatus_FT
- tblStimme_FT
- tblAllgemein_FT

---

# Mitgliedsnummer

Die Mitgliedsnummer wird transaktionssicher im Backend erzeugt.

## Ablauf

1. Lesen von `tblAllgemein_FT.neueMitgliedsnummer`
2. Reservierung über `SELECT ... FOR UPDATE`
3. Vergabe an neues Mitglied
4. Hochzählen der Nummer
5. Speicherung der neuen Nummer

## SQL

```sql
SELECT neueMitgliedsnummer
FROM tblAllgemein_FT
FOR UPDATE
```

---

# REST API

# Lookups

```http
GET /api/lookups/member-status
GET /api/lookups/voices
```

---

# Mitgliederliste

```http
GET /api/members
```

## Query Parameter

| Parameter | Beschreibung |
|---|---|
| search | Freitextsuche |
| statusId | Mitgliederstatus |
| stimmeId | Stimme |
| page | Seitennummer |
| pageSize | Anzahl Datensätze |

## Beispiele

```http
GET /api/members?page=1&pageSize=20
GET /api/members?search=wolf
GET /api/members?statusId=1&statusId=4
GET /api/members?stimmeId=1&stimmeId=3
```

---

# Mitglied Detail

```http
GET /api/members/{mitgliedsnummer}
```

## Beispielantwort

```json
{
  "mitgliedsnummer": "N1139",
  "stammdaten": {},
  "kontakt": {},
  "mitgliedschaft": {}
}
```

---

# Mitglied anlegen

```http
POST /api/members
```

## Automatisch angelegte Datensätze

Beim POST werden automatisch Datensätze erzeugt in:

- tblMitglieder
- tblKontaktdaten
- tblMitgliedschaft
- tblDatenschutz
- tblChorkleidung

Datenschutz und Chorkleidung werden initial mit Default-Werten angelegt.

---

# Stammdaten ändern

```http
PUT /api/members/{mitgliedsnummer}/stammdaten
```

---

# Kontakt ändern

```http
PUT /api/members/{mitgliedsnummer}/kontakt
```

---

# Mitgliedschaft ändern

```http
PUT /api/members/{mitgliedsnummer}/mitgliedschaft
```

---

# Datenschutz

## Datenschutz lesen

```http
GET /api/members/{mitgliedsnummer}/datenschutz
```

## Datenschutz ändern

```http
PUT /api/members/{mitgliedsnummer}/datenschutz
```

## Beispielrequest

```json
{
  "datumDatenschutz": "2026-05-01T18:00:00",
  "datenschutzNr14": true,
  "datenschutzNr15": true,
  "datenschutzNr16": false,
  "datenschutzNr17": false,
  "datenschutzNr18": true
}
```

---

# Chorkleidung

## Chorkleidung lesen

```http
GET /api/members/{mitgliedsnummer}/chorkleidung
```

## Chorkleidung ändern

```http
PUT /api/members/{mitgliedsnummer}/chorkleidung
```

## Beispielrequest

```json
{
  "ehemaligeStimme": "Tenor",
  "neubeschaffung": true,
  "barzahlung": true,
  "kaufpreis": 199.99,
  "sommerkleidung": true
}
```

---

# Validierung

## DTO-Validierung

- Pflichtfelder
- maximale Feldlängen
- E-Mail-Validierung
- numerische Bereiche

## Fachliche Validierung

- Mitgliederstatus muss existieren
- Stimme muss existieren
- Rückgabe darf nicht vor Übergabe liegen

## Datenbankvalidierung

Zusätzliche Absicherung über:

- Foreign Keys
- UNIQUE
- NOT NULL
- Constraints

---

# Fehlerhandling

## Standard ErrorResponse

```json
{
  "timestamp": "2026-05-01T19:12:44",
  "status": 404,
  "error": "Not Found",
  "message": "Mitglied nicht gefunden",
  "path": "/api/members/N9999",
  "requestId": "c91f9f09-9d31-4c42-b17d-3c8a5fd26a18"
}
```

## Validierungsfehler

```json
{
  "timestamp": "2026-05-01T19:12:44",
  "status": 400,
  "error": "Bad Request",
  "message": "Validierungsfehler",
  "path": "/api/members/N1139/chorkleidung",
  "requestId": "c91f9f09-9d31-4c42-b17d-3c8a5fd26a18",
  "validationErrors": [
    {
      "field": "kaufpreis",
      "message": "Kaufpreis darf nicht negativ sein"
    }
  ]
}
```

## Statuscodes

| Status | Bedeutung |
|---|---|
| 400 Bad Request | Validierungsfehler |
| 404 Not Found | Datensatz nicht gefunden |
| 409 Conflict | Duplicate Key |
| 500 Internal Server Error | Unerwarteter Fehler |

---

# Logging

## Eigenschaften

- strukturierte Logs
- Request-ID-Korrelation
- WARN für fachliche Fehler
- ERROR für technische Fehler

## Request-ID

Jede Anfrage erhält:

```text
X-Request-Id
```

Die ID wird:

- im Response Header zurückgegeben
- im ErrorResponse enthalten
- im Log ausgegeben

---

# Docker Deployment

## DEV / PROD

Das Backend läuft als:

- DEV Container
- PROD Container

auf dem NAS DH2300.

## Build

```bash
mvn clean package
```

## Docker Build

```bash
docker build -t emc-backend .
```

## Docker Run

```bash
docker run -d -p 8080:8080 emc-backend
```

---

# Test

Empfohlenes Werkzeug:

- Postman

## Wichtige Tests

```http
GET /api/lookups/member-status
GET /api/lookups/voices

GET /api/members
GET /api/members/{mitgliedsnummer}

POST /api/members

PUT /api/members/{mitgliedsnummer}/stammdaten
PUT /api/members/{mitgliedsnummer}/kontakt
PUT /api/members/{mitgliedsnummer}/mitgliedschaft

PUT /api/members/{mitgliedsnummer}/datenschutz
PUT /api/members/{mitgliedsnummer}/chorkleidung
```

---

# Versionierung

## Aktuelle Version

```text
1.1.0-SNAPSHOT
```

## Branching

- master → stabile Releases
- feature/... → Featureentwicklung

---

# Aktueller Status

| Bereich | Status |
|---|---|
| Lookup-Endpunkte | fertig |
| Mitgliederliste | fertig |
| Suche | fertig |
| Pagination | fertig |
| Multi-Select-Filter | fertig |
| Mitglied Detail | fertig |
| Mitglied anlegen | fertig |
| Stammdaten ändern | fertig |
| Kontakt ändern | fertig |
| Mitgliedschaft ändern | fertig |
| Datenschutz | fertig |
| Chorkleidung | fertig |
| DTO-Validierung | fertig |
| Zentrales Fehlerhandling | fertig |
| Request-ID Logging | fertig |
| Docker DEV/PROD | fertig |
| Login / Auth | offen |
| Rollen / Rechte | offen |

---

# Nicht Bestandteil des MVP

- Ehrungen
- Funktionen
- Verteiler
- Berichte
- Benutzerverwaltung
- Rechte je Datenfeld
- Historisierung
- Mailversand
