# EMC Mitgliederverwaltung – Backend

## Überblick

Spring Boot Backend für die Mitgliederverwaltung des EMC Männerchors.

Das Backend stellt eine REST-API für Mitgliederverwaltung, Lookup-Daten und administrative Benutzerverwaltung bereit. Die Anwendung nutzt MariaDB als Persistenzschicht, läuft containerisiert auf dem NAS und wird durch ein separates React-Frontend verwendet.

**Aktueller Stand: Phase 3c abgeschlossen (Auth + Rollen + Admin-Benutzerverwaltung + Versionsinformationen + Backend Integration Tests Phase 1)**

---

## Technologien

- Java 21
- Spring Boot 3.5.x
- Spring Web
- Spring JDBC (`JdbcTemplate`)
- Spring Security
- Jakarta Validation
- MariaDB
- Flyway
- Maven
- Docker
- Lombok
- Spring Boot Build Metadata (`build-info`)

---

## Architektur

```text
Controller
→ Service
→ Repository
→ Mapper
→ MariaDB
```

### Verantwortlichkeiten

#### Controller

- REST-Endpunkte
- HTTP Request/Response
- DTO Binding
- Response Serialisierung

#### Service

- Geschäftslogik
- fachliche Validierung
- Transaktionssteuerung

#### Repository

- SQL mit `JdbcTemplate`
- Persistenzoperationen

#### Mapper

- ResultSet → DTO Mapping
- Typkonvertierungen

#### Exception Handling / Logging

- zentraler `GlobalExceptionHandler`
- strukturierte Fehlerantworten
- Request-ID Korrelation

---

## Fachliches Datumsmodell

Grundsatz:

> Das API-Fachmodell gewinnt über das technische Datenbankmodell.

Fachliche Datumsfelder werden konsequent als:

```text
LocalDate
```

serialisiert:

```json
"2026-05-12"
```

Technische Zeitstempel verwenden weiterhin:

```text
LocalDateTime
```

### Beispiele für fachliche Datumsfelder

- `geburtsdatum`
- `eintritt`
- `austritt`
- Datenschutz-Datum
- Chorkleidungs-Datumsfelder

---

## Sicherheit / Authentifizierung

### Authentifizierungsmodell

Session-basierte Authentifizierung mit Spring Security.

Es wird bewusst verwendet:

- kein Basic Auth
- kein JWT

Ablauf:

- Login erzeugt Server-Session
- Session wird über Cookie gehalten
- Frontend prüft Session über `/api/auth/me`

### Rollenmodell

- `ADMIN`
- `EDITOR`
- `VIEWER`

### Berechtigungsmatrix

| Bereich | ADMIN | EDITOR | VIEWER |
|--------|------|--------|--------|
| Login / Logout / me | ✅ | ✅ | ✅ |
| Systeminformationen | ✅ | ✅ | ✅ |
| Lookup lesen | ✅ | ✅ | ✅ |
| Mitglieder lesen | ✅ | ✅ | ✅ |
| Mitglied anlegen | ✅ | ✅ | ❌ |
| Mitglied ändern | ✅ | ✅ | ❌ |
| Mitglied löschen | ✅ | ❌ | ❌ |
| Benutzerverwaltung | ✅ | ❌ | ❌ |

### Aktueller Sicherheitsstand

Bereits implementiert:

- Session Login
- Session Logout
- Session Restore (`/api/auth/me`)
- rollenbasierte Autorisierung
- Admin-Benutzerverwaltung
- neutrales Login-Fehlerverhalten
- Passwort-Hashing via BCrypt
- `last_login_at` Tracking
- authentifizierter technischer System-Endpoint

Noch nicht umgesetzt:

- Session Timeout / Auto Logout
- Fehlversuchszähler
- temporäre Kontosperre
- Passwortwechsel beim Erstlogin
- Initialpasswort-Workflow
- Passwort Reset Workflow
- Schutz letzter aktiver Admin
- Session Invalidierung bei Rollenänderung

---

## Projektstruktur

```text
src/main/java/de/emc/mitglieder
├── config
├── constant
├── controller
│   └── SystemInfoController
├── dto
│   ├── admin
│   ├── auth
│   ├── error
│   ├── member
│   └── request
├── exception
├── logging
├── mapper
├── repository
├── security
├── service
└── validation
```

---

## Datenbank

### Kernbereiche

Mitgliederdaten:

- `tblMitglieder`
- `tblKontaktdaten`
- `tblMitgliedschaft`
- `tblDatenschutz`
- `tblChorkleidung`

### Benutzerverwaltung

```text
tblUsers
```

Wichtige Felder:

```text
id
username
password_hash
role
active
created_at
last_login_at
```

### Lookup Tabellen

- `tblMitgliederstatus_FT`
- `tblStimme_FT`
- `tblAllgemein_FT`

### Konfiguration

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Beispiel DEV:

```text
DB_URL=jdbc:mariadb://192.168.x.x:3306/emc_mitglieder_dev
DB_USERNAME=emc_backend_dev_rw
DB_PASSWORD=********
```

### Test-Datenbank

Für Backend Integration Tests existiert eine separate Test-Datenbank:

```text
emc_mitglieder_test
```

Zweck:

- isolierte automatisierte Integrationstests
- keine Beeinflussung der DEV-Daten
- reproduzierbare Testdatenbasis

Abgrenzung:

- `emc_mitglieder_dev` → manuelle Entwicklung / Frontend / Postman
- `emc_mitglieder_test` → automatisierte Backend Integration Tests
- `emc_mitglieder_prod` → produktiver Betrieb

---

## Mitgliedsnummernvergabe

Transaktionssichere Vergabe über:

```sql
SELECT neueMitgliedsnummer FROM tblAllgemein_FT FOR UPDATE
```

Ablauf:

1. Nummer lesen
2. Datensatz sperren
3. Nummer vergeben
4. Nummer inkrementieren
5. speichern

Damit keine Doppelvergabe bei parallelen Requests.

---

## REST API

## Auth / System

```http
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/me
GET  /api/system/info
```

### Systeminformationen

Authentifizierter technischer Endpoint für Betriebs- und Supportzwecke.

```http
GET /api/system/info
```

Beispiel Response:

```json
{
  "backendVersion": "1.1.1-SNAPSHOT"
}
```

Verwendungszwecke:

- Deployment Smoke Tests
- Support / Diagnose
- Frontend Versionsanzeige
- Betriebsidentifikation

---

## Lookups

```http
GET /api/lookups/member-status
GET /api/lookups/voices
```

---

## Mitglieder

```http
GET    /api/members
GET    /api/members/{mitgliedsnummer}
POST   /api/members
PUT    /api/members/{mitgliedsnummer}/stammdaten
PUT    /api/members/{mitgliedsnummer}/kontakt
PUT    /api/members/{mitgliedsnummer}/mitgliedschaft
PUT    /api/members/{mitgliedsnummer}/datenschutz
PUT    /api/members/{mitgliedsnummer}/chorkleidung
DELETE /api/members/{mitgliedsnummer}
```

### Mitgliederliste Filter

Query Parameter:

- `search`
- `statusId` (mehrfach)
- `stimmeId` (mehrfach)
- `page`
- `pageSize`

Beispiele:

```http
GET /api/members?page=1&pageSize=20
GET /api/members?search=wolf
GET /api/members?statusId=1&statusId=4
GET /api/members?stimmeId=2&stimmeId=5
```

---

## Admin Benutzerverwaltung

```http
GET /api/admin/users
POST /api/admin/users
PUT /api/admin/users/{id}/role
PUT /api/admin/users/{id}/active
PUT /api/admin/users/{id}/password
```

---

## Fehlerhandling

Standardformat:

```json
{
  "timestamp": "2026-05-19T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validierungsfehler",
  "path": "/api/members",
  "requestId": "uuid"
}
```

HTTP Statuscodes:

- 400 Bad Request
- 401 Unauthorized
- 403 Forbidden
- 404 Not Found
- 409 Conflict
- 500 Internal Server Error

### Login Fehlerverhalten

Aus Sicherheitsgründen werden Login-Fehler neutral beantwortet:

```text
Anmeldung nicht möglich.
```

Damit werden keine Details preisgegeben:

- falsches Passwort
- deaktivierter Benutzer
- spätere Sperrmechanismen

---

## Logging

Logging-Konzept:

- strukturierte Logs
- Request-ID Korrelation
- WARN für fachliche Fehler
- ERROR für technische Fehler

Header:

```text
X-Request-Id
```

---

## Tests

### Teststrategie

Das Backend nutzt eine mehrstufige Teststrategie.

#### Unit / Slice Tests

Ziel:

- schnelle technische Rückmeldung
- isolierte Controller-/Security-/Validierungsprüfungen

Bereiche:

- Controller Tests
- Security Tests
- DTO / Validierungsnahe Tests
- bestehende Member-/Service-nahe Tests

#### Integration Tests

Ziel:

- realistische Ende-zu-Ende Backend-Prüfung innerhalb des Spring Backends

Technik:

- vollständiger Spring Boot Kontext (`@SpringBootTest`)
- MockMvc
- echte Spring Security
- echte Session-basierte Authentifizierung
- echte MariaDB Test-Datenbank
- echte JdbcTemplate Persistenzprüfung

Bewusste Projektentscheidung:

```text
keine Testcontainers
```

Begründung:

- Einzelentwicklerprojekt
- pragmatischer Wartungsaufwand
- produktionsnahe Testumgebung über dedizierte TEST DB

### Aktueller Backend-Teststand

#### Unit / bestehende Tests

- `AuthControllerTest`
- `AdminSecurityTest`
- `AdminUserControllerTest`
- `SystemInfoControllerTest`
- bestehende Member-/Validierungs-Tests

#### Integration Tests – Phase 1

- `AuthSessionIntegrationTest`
- `AuthorizationIntegrationTest`
- `MemberReadIntegrationTest`
- `MemberWriteIntegrationTest`

Abgedeckter Scope:

- Login
- Logout
- Session Restore (`/api/auth/me`)
- Rollenbasierte Autorisierung
- Mitglieder lesen
- Mitglieder schreiben
- echte Persistenzprüfung via JdbcTemplate

### Test-Infrastruktur

Spring Test Profil:

```text
application-test.properties
```

Testdatenbank:

```text
emc_mitglieder_test
```

Definierte Testuser:

- `it_admin`
- `it_editor`
- `it_viewer`

Definierte Testmitglieder:

- `N1001`
- `N1002`

Hinweis:

Write-Integrationtests setzen geänderte Testdaten zurück, damit die Gesamttest-Suite stabil reproduzierbar bleibt.

### Ausführen

Alle Tests:

```bash
mvn test
```

Vollständiger Build:

```bash
mvn clean package
```

---

## Build / Deployment

### Build

```bash
mvn clean package
```

Der Build erzeugt zusätzlich Spring Boot Build-Metadaten:

```text
META-INF/build-info.properties
```

Diese Metadaten werden für den System-Endpoint verwendet:

```http
GET /api/system/info
```

und liefern die aktuell deployte Backend-Version.

### Docker

```bash
docker build -t emc-backend .
docker run -d -p 8080:8080 emc-backend
```

### Zielplattform

```text
UGREEN NAS DH2300
```

Deployment erfolgt containerisiert auf dem NAS.

Details zum operativen Deployment:

siehe Deployment-Handbuch.

---

## Projektstatus

### Fertig

- Lookup APIs
- Mitgliederliste
- Suche / Filter / Pagination
- Detailansicht
- Mitglied anlegen
- Mitglied aktualisieren
- Datenschutz
- Chorkleidung
- Löschen
- Global Exception Handling
- Request-ID Logging
- Session Auth
- Rollenmodell
- Benutzerverwaltung
- Security Tests
- Backend Integration Tests Phase 1
- System Info Endpoint
- Build Version Metadata

### Geplant (Phase 4+)

Security Hardening:

- Passwort Lifecycle
- Initialpasswort
- Passwortwechsel beim Erstlogin
- Session Timeout
- Auto Logout
- Brute Force Schutz
- temporäre Sperren
- Recovery-Konzept Admin

Teststrategie:

- Backend Integration Tests Phase 2 (Admin Benutzerverwaltung)
- Backend Integration Tests Phase 3 (Mitglied anlegen / Transaktionen)

---

## Nicht im MVP

Derzeit bewusst nicht umgesetzt:

- Ehrungen
- Funktionen
- Verteiler
- Historisierung
- Berichte
- Mailversand
- Exportfunktionen