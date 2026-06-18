# EMC Mitgliederverwaltung – Backend

## 1. Überblick

Spring Boot Backend für die Mitgliederverwaltung des EMC Männerchors.

Das Backend stellt eine REST-API für Mitgliederverwaltung, Lookup-Daten, Authentifizierung, rollenbasierte Autorisierung, administrative Benutzerverwaltung sowie technische Betriebsinformationen bereit.

Die Anwendung nutzt MariaDB als Persistenzschicht, läuft containerisiert auf dem NAS und wird durch ein separates React-Frontend verwendet.

Diese README beschreibt den technischen Überblick des Backend-Repositories.

Projektsteuerung, Roadmap, Backlog und Projektstatus werden zentral in der Produktdokumentation geführt.

Betriebs- und Infrastrukturthemen werden zentral in EMC-INFRA dokumentiert.

---

## 2. Technologie-Stack

* Java 21
* Spring Boot 3.5.x
* Spring Web
* Spring JDBC (`JdbcTemplate`)
* Spring Security
* Spring Boot Actuator
* Jakarta Validation
* MariaDB
* Flyway
* Maven
* Docker
* Lombok
* Spring Boot Build Metadata (`build-info`)

---

## 3. Systemarchitektur

```text
Controller
→ Service
→ Repository
→ Mapper
→ MariaDB
```

### Controller

* REST-Endpunkte
* HTTP Request / Response
* DTO Binding
* Response Serialisierung

### Service

* Geschäftslogik
* fachliche Validierung
* Transaktionssteuerung

### Repository

* SQL mit `JdbcTemplate`
* Persistenzoperationen

### Mapper

* ResultSet → DTO Mapping
* Typkonvertierungen

### Exception Handling

* zentraler `GlobalExceptionHandler`
* strukturierte Fehlerantworten
* Request-ID Korrelation

---

## 4. Sicherheit und Autorisierung

### Authentifizierungsmodell

Das Backend verwendet sessionbasierte Authentifizierung mit Spring Security.

Bewusst nicht verwendet werden:

* Basic Auth
* JWT

Ablauf:

1. Login erzeugt eine serverseitige Session.
2. Die Session wird über ein Cookie gehalten.
3. Das Frontend prüft die aktive Session über `/api/auth/me`.

### Rollenmodell

* `ADMIN`
* `EDITOR`
* `VIEWER`

### Berechtigungsmatrix

| Bereich             | ADMIN | EDITOR | VIEWER |
| ------------------- | ----: | -----: | -----: |
| Login / Logout / me |     ✅ |      ✅ |      ✅ |
| Systeminformationen |     ✅ |      ✅ |      ✅ |
| Lookup lesen        |     ✅ |      ✅ |      ✅ |
| Mitglieder lesen    |     ✅ |      ✅ |      ✅ |
| Mitglied anlegen    |     ✅ |      ✅ |      ❌ |
| Mitglied ändern     |     ✅ |      ✅ |      ❌ |
| Mitglied löschen    |     ✅ |      ❌ |      ❌ |
| Benutzerverwaltung  |     ✅ |      ❌ |      ❌ |

### Technischer Sicherheitsstand

Implementiert:

* Session Login
* Session Logout
* Session Restore (`/api/auth/me`)
* rollenbasierte Autorisierung
* Admin-Benutzerverwaltung
* neutrales Login-Fehlerverhalten
* Passwort-Hashing via BCrypt
* `last_login_at` Tracking
* authentifizierter technischer System-Endpoint
* öffentliche Health-Endpunkte für Monitoring
* Schutz des letzten aktiven ADMIN-Kontos
* defensives Fehlerverhalten bei unerwarteten technischen Fehlern
* Session Timeout
* gehärtete Session-Cookie-Basiskonfiguration

Bewusst geschützt:

* `/api/system/info` ist authentifiziert.
* `/api/**` ist grundsätzlich geschützt.
* Nur `/actuator/health/**` ist öffentlich freigegeben.

### Session- und Cookie-Governance

Die Anwendung verwendet serverseitige Sessions.

Konfiguriert:

* Session Timeout: 30 Minuten
* `HttpOnly=true`
* `SameSite=Lax`
* `Secure=true` im PROD-Profil bei HTTPS-Betrieb

Umgebungsspezifische Cookie-Einstellungen werden nicht buildabhängig gepflegt, sondern über Spring Profile gesteuert.

---

## 5. Datenhaltung

### Datenbank

Das Backend nutzt MariaDB.

Die Datenbankverbindung wird über Umgebungsvariablen konfiguriert:

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
SPRING_PROFILES_ACTIVE=dev
```

### Kernbereiche

Mitgliederdaten:

* `tblMitglieder`
* `tblKontaktdaten`
* `tblMitgliedschaft`
* `tblDatenschutz`
* `tblChorkleidung`

Benutzerverwaltung:

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

Lookup-Tabellen:

* `tblMitgliederstatus_FT`
* `tblStimme_FT`
* `tblAllgemein_FT`

### Test-Datenbank

Für automatisierte Backend-Integrationstests existiert eine separate Test-Datenbank:

```text
emc_mitglieder_test
```

Zugriff erfolgt über einen dedizierten Test-Datenbankbenutzer:

```text
emc_backend_test_rw
```

Abgrenzung:

| Umgebung              | Zweck                                    |
| --------------------- | ---------------------------------------- |
| `emc_mitglieder_dev`  | lokale Entwicklung, Frontend, Postman    |
| `emc_mitglieder_test` | automatisierte Backend-Integrationstests |
| `emc_mitglieder_prod` | produktiver Betrieb                      |

---

### Datenbankschema

Das Datenbankschema wird aktuell manuell gepflegt.

Die Flyway-Abhängigkeit ist bereits vorbereitet, wird derzeit jedoch noch nicht aktiv für Datenbankmigrationen verwendet.

Eine spätere Nutzung von Flyway für strukturierte Datenbankmigrationen bleibt möglich.

---

## 6. REST API

### Authentifizierung und System

```http
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/me
GET  /api/system/info
```

### Lookups

```http
GET /api/lookups/member-status
GET /api/lookups/voices
```

### Mitglieder

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

Query-Parameter:

* `search`
* `statusId` mehrfach möglich
* `stimmeId` mehrfach möglich
* `page`
* `pageSize`

Beispiele:

```http
GET /api/members?page=1&pageSize=20
GET /api/members?search=wolf
GET /api/members?statusId=1&statusId=4
GET /api/members?stimmeId=2&stimmeId=5
```

### Admin Benutzerverwaltung

```http
GET /api/admin/users
POST /api/admin/users
PUT /api/admin/users/{id}/role
PUT /api/admin/users/{id}/active
PUT /api/admin/users/{id}/password
```

---

## 7. Betriebsinformationen und Monitoring

### Systeminformationen

Das Backend stellt einen authentifizierten System-Endpoint bereit:

```http
GET /api/system/info
```

Beispiel:

```json
{
  "backendVersion": "1.1.2-SNAPSHOT",
  "environment": "DEV",
  "activeProfiles": [
    "dev"
  ],
  "buildTime": "2026-06-11T12:48:17.103Z"
}
```

Gelieferte Informationen:

* Backend-Version
* Umgebung (`LOCAL`, `DEV`, `PROD`)
* aktive Spring-Profile
* Build-Zeitpunkt

Zugriff:

* authentifiziert
* nutzbar durch `ADMIN`, `EDITOR`, `VIEWER`

Verwendungszwecke:

* Frontend-Versionsanzeige
* Deployment Smoke Tests
* Support
* Betriebsidentifikation

### Environment-Erkennung

Die Umgebung wird aus den aktiven Spring-Profilen abgeleitet.

| Spring-Profil                  | Anzeige |
| ------------------------------ | ------- |
| `dev`                          | `DEV`   |
| `prod`                         | `PROD`  |
| kein Profil / sonstige Profile | `LOCAL` |

### Healthchecks

Das Backend stellt Spring Boot Actuator Health-Endpunkte bereit.

Öffentlich erreichbar:

```http
GET /actuator/health
GET /actuator/health/liveness
GET /actuator/health/readiness
```

### Liveness

Prüft, ob die Anwendung grundsätzlich lauffähig ist.

Beispiel:

```json
{
  "status": "UP"
}
```

### Readiness

Prüft, ob die Anwendung fachlich betriebsbereit ist.

Bestandteile:

* Spring Readiness State
* MariaDB Datenbankverbindung

Projektentscheidung:

Die MariaDB-Verbindung ist Bestandteil der Readiness-Prüfung.

Ist MariaDB nicht erreichbar, liefert der Readiness-Endpunkt:

```json
{
  "status": "DOWN"
}
```

mit HTTP-Status:

```text
503 Service Unavailable
```

### Gesamt-Health

Der Gesamt-Health-Endpunkt berücksichtigt die registrierten Health-Indikatoren.

Bei erreichbarer Datenbank:

```json
{
  "status": "UP"
}
```

Bei nicht erreichbarer Datenbank:

```json
{
  "status": "DOWN"
}
```

### Actuator-Konfiguration

```properties
management.endpoints.web.exposure.include=health
management.endpoint.health.show-details=never
management.endpoint.health.probes.enabled=true
management.health.defaults.enabled=true
management.endpoint.health.group.readiness.include=readinessState,db
```

### Sicherheitsentscheidung

Öffentlich:

```text
/actuator/health
/actuator/health/liveness
/actuator/health/readiness
```

Authentifiziert:

```text
/api/system/info
```

Nicht veröffentlicht:

```text
/actuator/env
/actuator/metrics
/actuator/mappings
/actuator/beans
/actuator/configprops
/actuator/info
```

Damit wird die Informationspreisgabe auf das notwendige Minimum beschränkt.

---

## 8. Logging und Fehlerbehandlung

### Logging

Logging-Konzept:

* strukturierte Logs
* Request-ID Korrelation
* `WARN` für fachliche Fehler
* `ERROR` für technische Fehler

Header:

```text
X-Request-Id
```

### Fehlerformat

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

HTTP-Statuscodes:

* `400 Bad Request`
* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`
* `409 Conflict`
* `500 Internal Server Error`

### Login Fehlerverhalten

Login-Fehler werden aus Sicherheitsgründen neutral beantwortet:

```text
Anmeldung nicht möglich.
```

Damit werden keine Details preisgegeben über:

* falsches Passwort
* deaktivierten Benutzer
* spätere Sperrmechanismen

### Unerwartete technische Fehler

Unerwartete technische Fehler werden gegenüber Clients neutral beantwortet.

Beispiel:

```text
Ein unerwarteter Fehler ist aufgetreten.
```

Technische Details werden ausschließlich im Backend-Log protokolliert.

Dadurch werden keine internen Implementierungsdetails an Clients weitergegeben.

---

## 9. Qualitätssicherung und Tests

### Teststrategie

Das Backend nutzt eine mehrstufige Teststrategie.

### Unit / Slice Tests

Ziel:

* schnelle technische Rückmeldung
* isolierte Controller-, Security- und Validierungsprüfungen

Bereiche:

* Controller Tests
* Security Tests
* DTO- und validierungsnahe Tests
* Service-nahe Tests

### Integration Tests

Ziel:

* realistische Ende-zu-Ende-Prüfung innerhalb des Spring Backends

Technik:

* vollständiger Spring Boot Kontext (`@SpringBootTest`)
* MockMvc
* echte Spring Security
* echte sessionbasierte Authentifizierung
* echte MariaDB Test-Datenbank
* echte JdbcTemplate Persistenzprüfung

Bewusste Projektentscheidung:

```text
keine Testcontainers
```

Begründung:

* Einzelentwicklerprojekt
* pragmatischer Wartungsaufwand
* produktionsnahe Testumgebung über dedizierte TEST DB

### Test-Infrastruktur

Spring Test-Profil:

```text
application-test.properties
```

Testdatenbank:

```text
emc_mitglieder_test
```

Definierte Testuser:

* `it_admin`
* `it_editor`
* `it_viewer`

Definierte Testmitglieder:

* `N1001`
* `N1002`

### Aktueller Testumfang

Controller / Slice Tests:

* `AuthControllerTest`
* `AdminSecurityTest`
* `AdminUserControllerTest`
* `SystemInfoControllerTest`
* bestehende Member- und Validierungstests

Integration Tests:

* `AuthSessionIntegrationTest`
* `AuthorizationIntegrationTest`
* `MemberReadIntegrationTest`
* `MemberWriteIntegrationTest`
* `AdminUserIntegrationTest`
* `UserLoginStateIntegrationTest`
* `MemberCreateIntegrationTest`
* `ActuatorHealthIntegrationTest`

Zusätzlicher Scope durch Healthcheck-Tests:

* Actuator Health-Endpunkt verfügbar
* Liveness verfügbar
* Readiness verfügbar
* Datenbankstatus wird im Health-Konzept berücksichtigt
* öffentliche Health-Endpunkte sind ohne Login nutzbar

Aktueller Teststand:

```text
73 Tests
0 Fehler
0 Failures
0 Skipped
```

### Tests ausführen

Alle Tests:

```bash
mvn test
```

Vollständiger Build:

```bash
mvn clean package
```

---

## 10. Build und Deployment

### Build

```bash
mvn clean package
```

Der Build erzeugt Spring Boot Build-Metadaten:

```text
META-INF/build-info.properties
```

Diese Metadaten werden verwendet für:

* Backend-Version
* Build-Zeitpunkt
* Systeminformationen
* Deployment-Nachweise

### Docker Build

```bash
docker build -t emc-mitglieder-backend:dev .
```

Beispiel PROD:

```bash
docker build -t emc-mitglieder-backend:prod .
```

### Docker Runtime

Das Backend wird containerisiert auf dem NAS betrieben.

Wichtige Runtime-Variablen:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
SPRING_PROFILES_ACTIVE
```

Beispiel DEV:

```text
SPRING_PROFILES_ACTIVE=dev
```

Beispiel PROD:

```text
SPRING_PROFILES_ACTIVE=prod
```

### Zielplattform

```text
UGREEN NAS DH2300
```

Das operative Deployment erfolgt über die standardisierte Infrastruktur in EMC-INFRA.

Diese README beschreibt nur die technische Backend-Sicht.

---

## 11. Dokumentation

Dieses Repository enthält die technische Implementierung des EMC-Mitgliederverwaltungs-Backends.

Diese README beschreibt:

* technische Architektur
* relevante Schnittstellen
* Sicherheitsmodell
* Datenbankanbindung
* Betriebsinformationen
* Healthchecks
* Teststrategie
* Build-Grundlagen

Nicht Bestandteil dieser README sind:

* Projektsteuerung
* Roadmap
* Backlog
* fachliche Produktplanung
* operative Infrastrukturdetails
* Recovery- und Backup-Prozesse
* Uptime-Kuma-Konfiguration
* Docker-Compose Source of Truth

Zentrale Dokumentationsorte:

### EMC-DOKUMENTATION

Zuständig für:

* Produktvision
* Projektstatus
* Roadmap
* Backlog
* Entscheidungen
* fachliche Dokumentation
* Produktarchitektur
* Benutzerhandbuch

### EMC-INFRA

Zuständig für:

* Docker Compose
* Deployment-Betrieb
* Monitoring
* Uptime Kuma
* Backup
* Restore
* Recovery
* Security
* Betriebsinventare
* Infrastruktur-Governance
