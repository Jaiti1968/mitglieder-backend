# Validierungskatalog Backend – EMC Mitgliederverwaltung

Stand: Backend Phase 3c  
Kontext: Spring Boot Backend `mitglieder-backend`

---

# 1. Zweck dieses Dokuments

Dieses Dokument beschreibt die aktuell implementierten Backend-Validierungen, Fehlerformate und das erwartete API-Fehlerverhalten.

Zielgruppen:

- Backend-Entwicklung
- Frontend-Entwicklung
- API-Tests (Postman / Integrationstests)
- Dokumentation

Dieses Dokument beschreibt ausschließlich das Verhalten des Backends.

Nicht Bestandteil:

- Frontend UI Verhalten
- Formularlogik im Frontend
- Anzeigeempfehlungen

---

# 2. Fehlerformate

Das Backend verwendet zwei standardisierte Fehlerformate.

---

## 2.1 Feldbezogene Validierungsfehler (`ApiErrorResponse`)

Wird verwendet bei:

- Bean Validation
- fachlicher Feldvalidierung
- `BusinessValidationException`

Beispiel:

```json
{
  "timestamp": "2026-05-19T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validierungsfehler",
  "path": "/api/members/N1139/stammdaten",
  "requestId": "6ba04a80-0485-4a2e-91dc-f12d92dacb86",
  "validationErrors": [
    {
      "field": "vorname",
      "message": "Vorname darf bei Personen nicht leer sein"
    }
  ]
}
```

Struktur:

| Feld | Typ |
|------|-----|
| `timestamp` | `LocalDateTime` |
| `status` | Integer |
| `error` | String |
| `message` | String |
| `path` | String |
| `requestId` | String |
| `validationErrors[]` | Array |

---

## 2.2 Allgemeiner Fehler (`ErrorResponse`)

Wird verwendet bei:

- `NotFoundException`
- `BadRequestException`
- `DuplicateKeyException`
- technische Fehler
- Security Fehler

Beispiel:

```json
{
  "timestamp": "2026-05-19T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Mitglied nicht gefunden",
  "path": "/api/members/N9999",
  "requestId": "6ba04a80-0485-4a2e-91dc-f12d92dacb86"
}
```

---

# 3. HTTP Statuscodes

| Status | Bedeutung |
|--------|-----------|
| `200 OK` | erfolgreicher GET / PUT |
| `204 No Content` | erfolgreicher POST/PUT ohne Response Body |
| `400 Bad Request` | Validierungs- oder Fachfehler |
| `401 Unauthorized` | nicht authentifiziert |
| `403 Forbidden` | authentifiziert, aber keine Berechtigung |
| `404 Not Found` | Ressource nicht gefunden |
| `409 Conflict` | Duplicate Key / Eindeutigkeitsverletzung |
| `500 Internal Server Error` | unerwarteter technischer Fehler |

---

# 4. Auth / Security

## 4.1 Login

Endpoint:

```http
POST /api/auth/login
```

Request:

```json
{
  "username": "admin",
  "password": "secret"
}
```

---

### Erfolgreich

Response:

```http
200 OK
```

```json
{
  "id": 1,
  "username": "admin",
  "role": "ADMIN"
}
```

---

### Fehler

Ungültige Anmeldung:

```http
401 Unauthorized
```

Response:

```json
{
  "message": "Anmeldung nicht möglich."
}
```

Bewusst neutralisiert.

Es wird nicht unterschieden zwischen:

- falsches Passwort
- deaktivierter Benutzer
- spätere Sperrmechanismen

---

## 4.2 Session erforderlich

Geschützte Endpunkte ohne Session:

```http
401 Unauthorized
```

---

## 4.3 Rollenverletzung

Authentifiziert, aber Rolle unzureichend:

```http
403 Forbidden
```

Beispiel:

VIEWER auf:

```http
PUT /api/members/N1001/stammdaten
```

---

# 5. Stammdaten

Endpoints:

```http
POST /api/members
PUT /api/members/{mitgliedsnummer}/stammdaten
```

---

## Felder

| Feld | Typ |
|------|-----|
| `personFirma` | Boolean |
| `anrede` | String |
| `akademischerTitel` | String |
| `vorname` | String |
| `nachname` | String |
| `plz` | String |
| `ort` | String |
| `strasseHausNr` | String |
| `geburtsdatum` | LocalDate |

---

## Validierungen

### personFirma

| Regel | Meldung |
|------|---------|
| bei Update Pflicht | `Person/Firma muss angegeben werden` |

Bei POST:

Default:

```text
false
```

---

### vorname

| Regel | Meldung |
|------|---------|
| max 50 Zeichen | Bean Validation |
| Pflicht bei Person | `Vorname darf bei Personen nicht leer sein` |

---

### nachname

| Regel | Meldung |
|------|---------|
| Pflicht | `Nachname/Firmenname darf nicht leer sein` |
| max 50 Zeichen | Bean Validation |

---

### anrede

| Regel |
|------|
| max 50 Zeichen |

---

### akademischerTitel

| Regel |
|------|
| max 50 Zeichen |

---

### plz

| Regel |
|------|
| max 50 Zeichen |

---

### ort

| Regel |
|------|
| max 50 Zeichen |

---

### strasseHausNr

| Regel |
|------|
| max 50 Zeichen |

---

### geburtsdatum

Format:

```json
"1980-05-20"
```

Typ:

```text
LocalDate
```

Aktuell keine zusätzliche fachliche Backend-Regel.

---

# 6. Kontakt

Endpoint:

```http
PUT /api/members/{mitgliedsnummer}/kontakt
```

---

## Felder

| Feld |
|------|
| telefonPrivat |
| telefonGeschaeftlich |
| mobiltelefon |
| email |
| adresszusatz |
| briefanrede |

---

## Validierungen

### email

| Regel |
|------|
| gültiges E-Mail Format |
| max 100 Zeichen |

---

### adresszusatz

| Regel |
|------|
| max 50 Zeichen |

---

Telefonnummern:

Aktuell keine strenge fachliche Backend-Validierung.

---

# 7. Mitgliedschaft

Endpoint:

```http
PUT /api/members/{mitgliedsnummer}/mitgliedschaft
```

---

## Felder

| Feld | Typ |
|------|-----|
| `eintritt` | LocalDate |
| `austritt` | LocalDate |
| `mitgliedsstatusId` | Integer |
| `stimmeId` | Integer |
| `kammerchor` | Boolean |

---

## Validierungen

### mitgliedsstatusId

| Regel | Meldung |
|------|---------|
| Lookup muss existieren | `Ungültiger Mitgliederstatus` |

---

### stimmeId

| Regel | Meldung |
|------|---------|
| Lookup muss existieren | `Ungültige Stimme` |

---

### Datum

Format:

```json
"2026-05-19"
```

Typ:

```text
LocalDate
```

---

# 8. Datenschutz

Endpoint:

```http
PUT /api/members/{mitgliedsnummer}/datenschutz
```

---

## Felder

| Feld |
|------|
| datumDatenschutz |
| datenschutzNr14 |
| datenschutzNr15 |
| datenschutzNr16 |
| datenschutzNr17 |
| datenschutzNr18 |

---

## Validierungen

### datumDatenschutz

| Regel | Meldung |
|------|---------|
| nicht in Zukunft | `Datum Datenschutz darf nicht in der Zukunft liegen` |

Format:

```json
"2026-05-19"
```

Typ:

```text
LocalDate
```

---

# 9. Chorkleidung

Endpoint:

```http
PUT /api/members/{mitgliedsnummer}/chorkleidung
```

---

## Datumsfelder

Typ:

```text
LocalDate
```

Format:

```json
"2026-05-19"
```

---

## Validierungen

### rueckgabeAm

| Regel | Meldung |
|------|---------|
| nicht vor Übergabe | `Rückgabe darf nicht vor Übergabe liegen` |

---

### sommerkleidungRueckgabe

| Regel | Meldung |
|------|---------|
| nicht vor Erhalt | `Sommerkleidung-Rückgabe darf nicht vor Erhalt liegen` |

---

### kaufpreis

| Regel |
|------|
| nicht negativ |

---

# 10. Mitglied anlegen

Endpoint:

```http
POST /api/members
```

---

## Validierungen

| Regel | Meldung |
|------|---------|
| Stammdaten Pflicht | `Stammdaten müssen angegeben werden` |
| Vorname bei Person Pflicht | `Vorname darf bei Personen nicht leer sein` |
| Nachname/Firma Pflicht | `Nachname/Firmenname darf nicht leer sein` |
| Mitgliederstatus gültig | `Ungültiger Mitgliederstatus` |
| Stimme gültig | `Ungültige Stimme` |

---

# 11. Admin User Management

Basis:

```http
/api/admin/users
```

Nur Rolle:

```text
ADMIN
```

---

## Benutzer anlegen

Endpoint:

```http
POST /api/admin/users
```

Felder:

| Feld |
|------|
| username |
| password |
| role |

Validierungen:

| Regel |
|------|
| Username Pflicht |
| Passwort Pflicht |
| Rolle Pflicht |
| Username eindeutig |

Duplicate Username:

```http
409 Conflict
```

---

## Rolle ändern

Endpoint:

```http
PUT /api/admin/users/{id}/role
```

Validierungen:

| Regel |
|------|
| gültige Rolle |

---

## Aktiv/Inaktiv

Endpoint:

```http
PUT /api/admin/users/{id}/active
```

Validierungen:

Boolean Request erforderlich.

---

## Passwort ändern

Endpoint:

```http
PUT /api/admin/users/{id}/password
```

Validierungen:

Passwort Pflicht.

---

# 12. Testbeispiele

## Ungültiger Login

Erwartung:

```http
401 Unauthorized
```

---

## Viewer versucht Update

Erwartung:

```http
403 Forbidden
```

---

## Ungültiger Mitgliederstatus

Erwartung:

```http
400 Bad Request
```

---

## Nicht gefundenes Mitglied

Erwartung:

```http
404 Not Found
```

---

## Duplicate Username

Erwartung:

```http
409 Conflict
```

---

# 13. Erweiterungsrichtlinien

Neue fachliche Feldvalidierungen:

bevorzugt:

```java
BusinessValidationException
```

Pattern:

```java
throwValidationError("feldname", "Fehlermeldung");
```

Nicht feldbezogene Fehler:

- `BadRequestException`
- `NotFoundException`

Technische Fehler:

zentraler GlobalExceptionHandler.

---

# 14. Geplante Erweiterungen

Security Hardening:

- Passwortregeln
- Passwortwechsel beim Erstlogin
- Initialpasswort
- Passwort Reset Workflow
- Fehlversuchszähler
- temporäre Sperre
- Session Timeout
- Session Invalidierung