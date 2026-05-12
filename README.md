# EMC Mitgliederverwaltung -- Backend

Spring Boot Backend für die Verwaltung der Mitgliedsdaten des EMC
Männerchors.

Das Backend stellt eine REST-API bereit, kapselt den Zugriff auf MariaDB
und läuft als Docker-Container auf dem NAS DH2300. Das Frontend wird
separat als React-Anwendung umgesetzt.

------------------------------------------------------------------------

# Technologien

-   Java 21
-   Spring Boot 3
-   Spring Web
-   Spring JDBC (`JdbcTemplate`)
-   MariaDB
-   Maven
-   Docker
-   Portainer
-   Lombok
-   Jakarta Validation

------------------------------------------------------------------------

# Architektur

``` text
Controller
→ Service
→ Repository
→ Mapper
→ MariaDB
```

## Verantwortlichkeiten

### Controller

-   REST-Endpunkte
-   HTTP Request/Response
-   Request DTO Binding
-   Response-Serialisierung

### Service

-   Geschäftslogik
-   fachliche Validierung
-   Transaktionen
-   Fehlerbehandlung

### Repository

-   SQL-Zugriffe mit `JdbcTemplate`
-   Datenbankoperationen

### Mapper

-   Mapping von `ResultSet` auf DTOs
-   zentrale Datentyp-Konvertierung

### DTOs

-   `dto.member` → Response-/Fachmodelle
-   `dto.request` → Request DTOs
-   `dto.error` → Fehlerantworten

------------------------------------------------------------------------

# Fachliches Datumsmodell

## Grundsatz

**Das API-Fachmodell gewinnt über das technische Datenbankmodell.**

Auch wenn MariaDB-Spalten technisch als `datetime` definiert sind,
verwendet die API für fachliche Datumsfelder konsequent:

``` text
LocalDate
```

JSON-Format:

``` json
"2026-05-12"
```

Nicht verwendet für fachliche Datumsfelder:

``` text
LocalDateTime
```

Technische Zeitstempel (Fehlerzeitpunkte, Logging) verwenden weiterhin:

``` text
LocalDateTime
```

## Betroffene Fachbereiche

-   Stammdaten (`geburtsdatum`)
-   Mitgliedschaft (`eintritt`, `austritt`)
-   Datenschutz (`datumDatenschutz`)
-   Chorkleidung
    -   `uebergabeAm`
    -   `datumAnteil`
    -   `rueckgabeAm`
    -   `kaufdatum`
    -   `sommerkleidungErhalten`
    -   `sommerkleidungRueckgabe`

------------------------------------------------------------------------

# Projektstruktur

``` text
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

------------------------------------------------------------------------

# Datenbank

## Konfiguration

``` properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
```

## Beispiel ENV

``` text
DB_URL=jdbc:mariadb://192.168.xxx.xxx:3306/emc_mitglieder_dev
DB_USERNAME=emc_mitglieder_rw
DB_PASSWORD=********
```

## Tabellen

### Kern

-   `tblMitglieder`
-   `tblKontaktdaten`
-   `tblMitgliedschaft`

### Erweiterungen

-   `tblDatenschutz`
-   `tblChorkleidung`

### Lookups

-   `tblMitgliederstatus_FT`
-   `tblStimme_FT`
-   `tblAllgemein_FT`

------------------------------------------------------------------------

# Mitgliedsnummernvergabe

Transaktionssicher über:

``` sql
SELECT neueMitgliedsnummer
FROM tblAllgemein_FT
FOR UPDATE
```

Ablauf: 1. Nummer lesen 2. sperren 3. vergeben 4. inkrementieren 5.
speichern

------------------------------------------------------------------------

# REST API

## Lookups

``` http
GET /api/lookups/member-status
GET /api/lookups/voices
```

## Mitgliederliste

``` http
GET /api/members
```

Query Parameter:

  Parameter   Beschreibung
  ----------- ---------------------------------
  search      Freitextsuche
  statusId    Mehrfachfilter Mitgliederstatus
  stimmeId    Mehrfachfilter Stimme
  page        Seite
  pageSize    Seitengröße

Beispiele:

``` http
GET /api/members?page=1&pageSize=20
GET /api/members?search=wolf
GET /api/members?statusId=1&statusId=4
GET /api/members?stimmeId=1&stimmeId=3
```

## Mitglied Detail

``` http
GET /api/members/{mitgliedsnummer}
```

## Mitglied anlegen

``` http
POST /api/members
```

Automatisch erzeugte Datensätze: - `tblMitglieder` - `tblKontaktdaten` -
`tblMitgliedschaft` - `tblDatenschutz` - `tblChorkleidung`

------------------------------------------------------------------------

## Aktualisierung

``` http
PUT /api/members/{mitgliedsnummer}/stammdaten
PUT /api/members/{mitgliedsnummer}/kontakt
PUT /api/members/{mitgliedsnummer}/mitgliedschaft
PUT /api/members/{mitgliedsnummer}/datenschutz
PUT /api/members/{mitgliedsnummer}/chorkleidung
```

------------------------------------------------------------------------

# API Beispiele

## Datenschutz Request

``` json
{
  "datumDatenschutz": "2026-05-01",
  "datenschutzNr14": true,
  "datenschutzNr15": true,
  "datenschutzNr16": false,
  "datenschutzNr17": false,
  "datenschutzNr18": true
}
```

## Chorkleidung Request

``` json
{
  "ehemaligeStimme": "Tenor",
  "uebergabeAm": "2026-05-01",
  "datumAnteil": "2026-05-03",
  "barzahlung": true,
  "kaufdatum": "2026-05-05",
  "kaufpreis": 199.99,
  "sommerkleidungErhalten": "2026-05-10"
}
```

------------------------------------------------------------------------

# Validierung

## DTO Validierung

-   Pflichtfelder
-   Feldlängen
-   E-Mail Format
-   numerische Wertebereiche

## Fachliche Validierung

-   Lookup-Werte müssen existieren
-   Datumslogik
-   Zukunftsprüfungen, wo fachlich relevant

## Datenbankregeln

-   Foreign Keys
-   NOT NULL
-   UNIQUE

------------------------------------------------------------------------

# Fehlerhandling

Standard Error Response:

``` json
{
  "timestamp": "2026-05-12T11:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Mitglied nicht gefunden",
  "path": "/api/members/N9999",
  "requestId": "uuid"
}
```

Statuscodes: - 400 Bad Request - 404 Not Found - 409 Conflict - 500
Internal Server Error

------------------------------------------------------------------------

# Logging

-   strukturierte Logs
-   Request-ID Korrelation
-   WARN für fachliche Fehler
-   ERROR für technische Fehler

Response Header:

``` text
X-Request-Id
```

------------------------------------------------------------------------

# Docker Deployment

## Build

``` bash
mvn clean package
docker build -t emc-backend .
```

## Run

``` bash
docker run -d -p 8080:8080 emc-backend
```

## Deployment Modell

-   DEV Image (`:dev`)
-   PROD Image (`:prod`)

NAS Zielplattform: UGREEN NAS DH2300

------------------------------------------------------------------------

# Test

Empfohlen: Postman

Wichtige Endpunkte:

``` http
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

Datums-Tests immer mit:

``` json
"2026-05-12"
```

nicht:

``` json
"2026-05-12T00:00:00"
```

------------------------------------------------------------------------

# Projektstatus

  Bereich              Status
  -------------------- --------
-   Lookup APIs          fertig
-   Mitgliederliste      fertig
-   Suche                fertig
-   Pagination           fertig
-   Detailansicht        fertig
-   POST Mitglied        fertig
-   PUT Stammdaten       fertig
-   PUT Kontakt          fertig
-   PUT Mitgliedschaft   fertig
-   Datenschutz          fertig
-   Chorkleidung         fertig
-   Fehlerhandling       fertig
-   Request-ID Logging   fertig
-   Docker DEV/PROD      fertig
-   Login/Auth           offen
-   Rollen/Rechte        offen

------------------------------------------------------------------------

# Nicht im MVP

-   Ehrungen
-   Funktionen
-   Verteiler
-   Historisierung
-   Berichte
-   Benutzerverwaltung
-   Mailversand
