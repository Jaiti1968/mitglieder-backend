# EMC Mitgliederverwaltung – Backend

Spring Boot Backend für die Verwaltung von Mitgliedsdaten (MariaDB).

---

## 🚀 Technologien

- Java 21
- Spring Boot 3
- Spring Web / REST
- Spring JDBC
- MariaDB
- Maven

---

## 🧠 Architektur

- Controller → REST API
- Service → Business Logik
- Repository → SQL (JdbcTemplate)
- DTOs:
    - `dto.member` → Response / Fachmodell
    - `dto.request` → Requests
- Mapper → ResultSet → DTO

---

## 🗄️ Datenbank

MariaDB läuft auf NAS (DH2300).

Wichtige Tabellen:

- `tblMitglieder`
- `tblKontaktdaten`
- `tblMitgliedschaft`
- `tblDatenschutz`
- `tblChorkleidung`
- `tblAllgemein_FT` (Mitgliedsnummer)

---

## 🔢 Mitgliedsnummer

Die Mitgliedsnummer wird im Backend erzeugt:

1. Lesen aus `tblAllgemein_FT.neueMitgliedsnummer`
2. Verwendung für neues Mitglied
3. Erhöhung um 1
4. Zurückschreiben in Tabelle

Absicherung über:

```sql
SELECT ... FOR UPDATE

→ verhindert doppelte Nummern

📡 API Endpunkte
🔍 Lookups
GET /api/lookups/member-status
GET /api/lookups/voices
👥 Mitgliederliste
GET /api/members

Parameter:

page
pageSize
search
mitgliedsstatusId
stimmeId
📄 Detail
GET /api/members/{mitgliedsnummer}
➕ Mitglied anlegen
POST /api/members

Beispiel:

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
    "mitgliedsstatusId": 4,
    "stimmeId": 6,
    "kammerchor": false
  }
}
✏️ Stammdaten ändern
PUT /api/members/{mitgliedsnummer}/stammdaten
✏️ Kontakt ändern
PUT /api/members/{mitgliedsnummer}/kontakt
✏️ Mitgliedschaft ändern
PUT /api/members/{mitgliedsnummer}/mitgliedschaft
⚠️ Fehlerhandling

Standardisierte Fehler:

{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "...",
  "path": "/api/..."
}
✅ Validierung
DTO Validierung (@NotNull, @Email, etc.)
Fachliche Validierung (Lookup-IDs)
📝 Logging
Fehler werden serverseitig geloggt
500 Fehler → log.error
400/404 → log.warn
⚙️ Konfiguration
spring.datasource.url=jdbc:mariadb://<NAS-IP>:3306/emc_mitglieder_dev
spring.datasource.username=...
spring.datasource.password=...
🐳 Deployment (Docker auf NAS)

Geplant:

Spring Boot als Docker-Container
MariaDB läuft direkt auf NAS

Typischer Ablauf:

mvn clean package
docker build -t emc-backend .
docker run -d -p 8080:8080 emc-backend
🧪 Test

Mit Postman:

GET /api/members
POST /api/members
PUT Endpunkte
📌 Status
CRUD: ✅
Validierung: ✅
Logging: ✅
Pagination: ✅
Docker: geplant