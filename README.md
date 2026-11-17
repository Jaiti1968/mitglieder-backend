# EMC Mitgliederverwaltung – Backend

## 1. Überblick

Spring Boot Backend für die Mitgliederverwaltung des EMC Männerchors.

Das Backend stellt eine REST-API für Mitgliederverwaltung, Lookup-Daten,
Reporting, Authentifizierung, rollenbasierte Autorisierung,
administrative Benutzerverwaltung sowie technische Betriebsinformationen bereit.

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

Für Reportingfunktionen wird dieselbe Schichtenarchitektur verwendet.

Zusätzlich erfolgt eine Trennung zwischen fachlicher Datenaufbereitung und Ausgabeformat:

```text
ReportingService
        ↓
Browser
PDF
Excel
CSV
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
| Mitglied löschen    |     — |      — |      — |
| Ehrungen lesen      |     ✅ |      ✅ |      ✅ |
| Ehrung anlegen      |     ✅ |      ✅ |      ❌ |
| Ehrung bearbeiten   |     ✅ |      ✅ |      ❌ |
| Ehrung löschen      |     ✅ |      ✅ |      ❌ |
| Benutzerverwaltung  |     ✅ |      ❌ |      ❌ |
| Reporting           |     ✅ |      ❌ |      ❌ |

Das Löschen einer Ehrung ist separat berechtigt. Für das Löschen eines Mitglieds enthält `SecurityConfig` eine `ADMIN`-Regel, im aktuellen `MemberController` ist jedoch kein entsprechender DELETE-Endpunkt implementiert. Die Zeile „Mitglied löschen“ bezeichnet deshalb keine derzeit verfügbare API-Funktion.

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
* `/api/auth/login` und `/actuator/health/**` sind ausdrücklich ohne Anmeldung erreichbar.
* Für nicht anderweitig erfasste Pfade gilt derzeit `anyRequest().permitAll()`; beispielsweise ist `/api/hello` öffentlich erreichbar.
* CSRF-Schutz ist derzeit in `SecurityConfig` deaktiviert (`csrf.disable()`). Bei sessionbasierter Authentifizierung ist dies ein gesondert zu bewertender Sicherheitsaspekt.

### Session- und Cookie-Governance

Die Anwendung verwendet serverseitige Sessions.

Konfiguriert:

* Session Timeout: 30 Minuten
* `HttpOnly=true`
* `SameSite=Lax`
* `Secure=false` im aktuellen PROD-Profil (`application-prod.properties`), da HTTPS noch nicht eingeführt ist

Umgebungsspezifische Cookie-Einstellungen werden über Spring-Profile gesteuert. Die HTTPS-Einführung ist als **BL-026** im Backlog vorgesehen. Im Zuge dieser Umsetzung muss insbesondere `Secure=true` für den HTTPS-Betrieb geprüft und eingerichtet werden.

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

Ehrungen eines Mitglieds:

* `tblEhrungen` – Zuordnung über `Mitgliedsnummer`; einzelne Ehrung über `IDTabEhrungen` identifiziert

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
* `tblEhrungen_FT` – Ehrungsarten (`IDEhrungen`, `Ehrungen`)
* `tblAllgemein_FT`

Die Ehrungsart wird gegen `tblEhrungen_FT` geprüft (`existsHonorTypeById`). Funktionen und Verteiler werden hier erst nach ihrer Backend-Implementierung ergänzt.

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
GET /api/lookups/honors
```

### Mitglieder

```http
GET    /api/members
GET    /api/members/{mitgliedsnummer}
POST   /api/members
PUT    /api/members/{mitgliedsnummer}/stammdaten
PUT    /api/members/{mitgliedsnummer}/kontakt
PUT    /api/members/{mitgliedsnummer}/mitgliedschaft
GET    /api/members/{mitgliedsnummer}/datenschutz
PUT    /api/members/{mitgliedsnummer}/datenschutz
GET    /api/members/{mitgliedsnummer}/chorkleidung
PUT    /api/members/{mitgliedsnummer}/chorkleidung
```

### Ehrungen (BL-009)

```http
GET    /api/members/{mitgliedsnummer}/honors
POST   /api/members/{mitgliedsnummer}/honors
PUT    /api/members/{mitgliedsnummer}/honors/{honorId}
DELETE /api/members/{mitgliedsnummer}/honors/{honorId}
```

Alle Operationen sind an die angegebene Mitgliedsnummer gebunden. Eine Ehrung kann nicht über die Mitgliedsnummer eines anderen Mitglieds geändert oder gelöscht werden. Erfolgreiches Löschen liefert `204 No Content`; ein nicht vorhandenes Mitglied oder eine nicht zugeordnete Ehrung führt zu `404 Not Found`. Die feldbezogenen Regeln und weiteren Fehlerfälle werden im `Validierungskatalog_Backend.md` beschrieben.

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

### Reporting

#### Mitgliederverzeichnis

```http
POST /api/reporting/members-directory/query
POST /api/reporting/members-directory/export/pdf
POST /api/reporting/members-directory/export/excel
```

#### Geburtstagsliste

```http
POST /api/reporting/birthday-list/query
POST /api/reporting/birthday-list/export/pdf
```

---

## 7. Betriebsinformationen und Monitoring

### Systeminformationen

Das Backend stellt einen authentifizierten System-Endpoint bereit:

```http
GET /api/system/info
```

Schematisches Beispiel (Versionswert ist ein Platzhalter):

```json
{
  "backendVersion": "<aktuelle Backend-Version>",
  "environment": "DEV",
  "activeProfiles": [
    "dev"
  ],
  "buildTime": "<Build-Zeitpunkt im ISO-8601-Format>"
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

### Fachliche HTTP-Fehler

Fachliche Fehler werden mit dem passenden HTTP-Statuscode und einer fachlich verständlichen Fehlermeldung beantwortet.

Beispiele:

```text
404 Nicht gefunden
409 Konflikt
```

Diese Fehlermeldungen dürfen an Clients weitergegeben werden, da sie Teil der fachlichen Anwendungslogik sind.

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

### Testumfang

Die automatisierten Tests decken unter anderem folgende Bereiche ab:

* Authentifizierung, Sessions, Rollen und administrative Benutzerverwaltung
* Mitglieder lesen, anlegen und bearbeiten sowie Mapping und Service-Logik
* BL-009 Ehrungen: Lesen, Anlegen, Bearbeiten und Löschen einschließlich Repository-, Service- und API-Integrationstests
* Reporting: Mitgliederverzeichnis, Geburtstagsliste, Berechnungen und Berechtigungen
* Systeminformationen, Healthchecks und Datenbankanbindung

Für BL-009 sind folgende Testklassen vorhanden:

* `MemberHonorReadIntegrationTest`
* `MemberHonorCreateIntegrationTest`
* `MemberHonorUpdateIntegrationTest`
* `MemberHonorDeleteIntegrationTest`
* `MemberHonorRepositoryIntegrationTest`
* `MemberHonorServiceTest`

Die vollständige Liste der Testklassen ergibt sich aus `src/test/java` und wird nicht dauerhaft in dieser README dupliziert.

Letzter verifizierter Teststand nach Abschluss der BL-009-Backend-Implementierung (Momentaufnahme):

```text
29 Testklassen
162 Tests
0 Fehler
0 fehlgeschlagene Tests
0 übersprungene Tests
BUILD SUCCESS
```

Prüfbefehl unter Windows:

```powershell
.\mvnw.cmd clean verify
```

### Tests ausführen

Tests unter Windows:

```powershell
.\mvnw.cmd test
```

Vollständiger Prüf-Build unter Windows:

```powershell
.\mvnw.cmd clean verify
```

Alternativ mit installiertem Maven: `mvn test` beziehungsweise `mvn clean verify`.

---

## 10. Build und Deployment

### Build

Vollständiger Prüf-Build unter Windows:

```powershell
.\mvnw.cmd clean verify
```

Ein Paket-Build ist alternativ mit `mvn clean package` möglich. Die Projektversion wird in der `pom.xml` geführt (zum Zeitpunkt des Codeabgleichs: `1.3.0-SNAPSHOT`).

Der Build erzeugt Spring Boot Build-Metadaten:

```text
META-INF/build-info.properties
```

Diese Metadaten werden verwendet für:

* Backend-Version
* Build-Zeitpunkt
* Systeminformationen
* Deployment-Nachweise

### Docker Build und Deployment

Das Backend wird als Docker-Container auf dem NAS betrieben. Die Images werden versioniert; starre Beispiel-Tags wie `dev` und `prod` sind deshalb keine verbindliche Build-Anweisung.

Für die jeweils gültigen Docker-Build-, Image-Tagging- und Deployment-Schritte sind bis zur ausstehenden Dokumentationskonsolidierung **EMC-INFRA und EMC-DOKUMENTATION** heranzuziehen. Diese README enthält bewusst keine zweite operative Deployment-Anleitung.

### Docker Runtime

Wichtige Runtime-Variablen:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
SPRING_PROFILES_ACTIVE
```

Spring-Profile unterscheiden die Laufzeitumgebungen, beispielsweise `dev` und `prod`.

### Zielplattform

```text
UGREEN NAS DH2300
```

Die konkreten Betriebs- und Infrastrukturvorgaben werden in den beiden genannten Dokumentations-Repositories gepflegt.

---

## 11. Dokumentation

Dieses Repository enthält die technische Implementierung des EMC-Mitgliederverwaltungs-Backends. Die README dient als Einstieg in die Backend-Architektur und beschreibt die wichtigsten Schnittstellen, Berechtigungen, Datenbankbereiche, Betriebsinformationen, Tests und Build-Grundlagen.

Weiterführende Projekt-, Architektur- und Betriebsdokumentation befindet sich derzeit in **EMC-DOKUMENTATION** und **EMC-INFRA**. Eine Konsolidierung dieser beiden Dokumentationsbestände ist noch vorgesehen. Bis dahin verweist die README auf beide Repositories, ohne eine endgültige Zuständigkeitsverteilung vorwegzunehmen.

Operative Details wie Docker Compose, Monitoring, Backup, Restore und Recovery werden nicht parallel in dieser README gepflegt.
