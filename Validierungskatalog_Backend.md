# Validierungskatalog Backend – EMC Mitgliederverwaltung

Stand: Backend-Version `1.1.0-SNAPSHOT`  
Kontext: Spring Boot Backend `mitglieder-backend` für EMC Mitgliederverwaltung

---

## 1. Zweck dieses Dokuments

Dieser Katalog dokumentiert die aktuell bekannten Backend-Validierungen und Fehlermeldungen der REST-API.

Er dient insbesondere dem Frontend als Grundlage für:

- feldbezogene Fehleranzeige in Formularen
- Mapping von `validationErrors[].field` auf Formularfelder
- einheitliche Behandlung von Backend-Fehlern
- Postman- und Frontend-Tests
- spätere Erweiterungen der Formularvalidierung

---

## 2. Fehlerformate

### 2.1 Strukturierter Validierungsfehler

Dieses Format wird für Bean-Validation-Fehler und fachliche feldbezogene Service-Validierungen verwendet.

```json
{
  "timestamp": "2026-05-07T09:21:35.3086045",
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

### 2.2 Allgemeiner Fehler

Dieses Format wird für nicht feldbezogene Fehler verwendet, z. B. nicht gefundene Datensätze oder technische Fehler.

```json
{
  "timestamp": "2026-05-07T09:21:35.3086045",
  "status": 404,
  "error": "Not Found",
  "message": "Mitglied mit Nummer N9999 wurde nicht gefunden.",
  "path": "/api/members/N9999",
  "requestId": "6ba04a80-0485-4a2e-91dc-f12d92dacb86"
}
```

---

## 3. Fehlerquellen

| Quelle | Beschreibung | Fehlerformat |
|---|---|---|
| Bean Validation | Annotationen wie `@NotBlank`, `@NotNull`, `@Size`, `@Email` | `ApiErrorResponse` mit `validationErrors[]` |
| Service-Validierung | fachliche Regeln im Service | `ApiErrorResponse` mit `validationErrors[]`, sofern feldbezogen |
| Datenbank | Foreign Keys, NOT NULL, UNIQUE, sonstige DB-Regeln | allgemeiner `ErrorResponse` |
| NotFoundException | Datensatz nicht gefunden | allgemeiner `ErrorResponse` |
| DuplicateKeyException | Eindeutigkeitsregel verletzt | allgemeiner `ErrorResponse` |

---

## 4. Allgemeine Statuscodes

| HTTP-Status | Bedeutung |
|---|---|
| `400 Bad Request` | Validierungsfehler, fachlicher Fehler oder DB-Regelverletzung |
| `404 Not Found` | Datensatz wurde nicht gefunden |
| `409 Conflict` | Duplicate Key / UNIQUE-Verstoß |
| `500 Internal Server Error` | unerwarteter technischer Fehler |

---

# 5. Stammdaten

## Endpunkte

```http
POST /api/members
PUT /api/members/{mitgliedsnummer}/stammdaten
GET /api/members/{mitgliedsnummer}
```

Die Stammdaten liegen im Objekt:

```json
{
  "stammdaten": {
    "personFirma": false,
    "anrede": "Herr",
    "akademischerTitel": "",
    "vorname": "Max",
    "nachname": "Mustermann",
    "plz": "99084",
    "ort": "Erfurt",
    "strasseHausNr": "Musterstraße 1",
    "geburtsdatum": "1980-05-20"
  }
}
```

## 5.1 Feld `personFirma`

| Eigenschaft | Wert |
|---|---|
| JSON-Feld | `personFirma` |
| Java-Typ | `Boolean` |
| DB-Feld | `tblMitglieder.PersonFirma` |
| DB-Typ | `BIT(1) NOT NULL DEFAULT b'0'` |
| Bedeutung `false` | natürliche Person |
| Bedeutung `true` | Firma / Organisation / Sponsor |
| Default bei Neuanlage | `false` |

### Validierungen

| Endpunkt | Regel | Meldung | Fehlerfeld | Quelle |
|---|---|---|---|---|
| `PUT /api/members/{id}/stammdaten` | muss angegeben werden | `Person/Firma muss angegeben werden` | `personFirma` | Bean Validation / Service |
| `POST /api/members` | falls nicht angegeben, wird `false` angenommen | keine Fehlermeldung | — | Service-Default |

---

## 5.2 Feld `vorname`

Bei natürlichen Personen ist `vorname` der Vorname.  
Bei Firmen kann `vorname` als Firmenzusatz verwendet werden.

### Validierungen

| Endpunkt | Regel | Meldung | Fehlerfeld | Quelle |
|---|---|---|---|---|
| `PUT /api/members/{id}/stammdaten` | maximal 50 Zeichen | `Vorname darf maximal 50 Zeichen haben` | `vorname` | Bean Validation |
| `PUT /api/members/{id}/stammdaten` | Pflicht, wenn `personFirma = false` | `Vorname darf bei Personen nicht leer sein` | `vorname` | Service |
| `POST /api/members` | Pflicht, wenn `personFirma = false` oder nicht angegeben | `Vorname darf bei Personen nicht leer sein` | `vorname` | Service |
| `POST /api/members` / `PUT /stammdaten` | optional, wenn `personFirma = true` | keine Fehlermeldung | — | Service |

---

## 5.3 Feld `nachname`

Bei natürlichen Personen ist `nachname` der Nachname.  
Bei Firmen ist `nachname` der Firmenname.

### Validierungen

| Endpunkt | Regel | Meldung | Fehlerfeld | Quelle |
|---|---|---|---|---|
| `PUT /api/members/{id}/stammdaten` | darf nicht leer sein | `Nachname darf nicht leer sein` | `nachname` | Bean Validation |
| `PUT /api/members/{id}/stammdaten` | maximal 50 Zeichen | `Nachname darf maximal 50 Zeichen haben` | `nachname` | Bean Validation |
| `POST /api/members` | darf nicht leer sein | `Nachname/Firmenname darf nicht leer sein` | `nachname` | Service |
| `PUT /api/members/{id}/stammdaten` | fachlich Pflicht als Nachname/Firmenname | `Nachname/Firmenname darf nicht leer sein` | `nachname` | Service |

Hinweis: Bei `PUT /stammdaten` kann zuerst die Bean Validation greifen und die Meldung `Nachname darf nicht leer sein` liefern.

---

## 5.4 Feld `anrede`

### Validierungen

| Endpunkt | Regel | Meldung | Fehlerfeld | Quelle |
|---|---|---|---|---|
| `PUT /api/members/{id}/stammdaten` | maximal 50 Zeichen | `Anrede darf maximal 50 Zeichen haben` | `anrede` | Bean Validation |

Hinweise:

- Bei Personen wird bei Neuanlage ohne Angabe bisher der Default `Herr` verwendet.
- Bei Firmen ist `anrede` fachlich optional.
- Das Frontend sollte `anrede` bei Firmen ausblenden oder deaktivieren.

---

## 5.5 Feld `akademischerTitel`

### Validierungen

| Endpunkt | Regel | Meldung | Fehlerfeld | Quelle |
|---|---|---|---|---|
| `PUT /api/members/{id}/stammdaten` | maximal 50 Zeichen | `Akademischer Titel darf maximal 50 Zeichen haben` | `akademischerTitel` | Bean Validation |

Hinweis: Bei Firmen fachlich optional und im Frontend vorzugsweise auszublenden oder zu deaktivieren.

---

## 5.6 Feld `plz`

### Validierungen

| Endpunkt | Regel | Meldung | Fehlerfeld | Quelle |
|---|---|---|---|---|
| `PUT /api/members/{id}/stammdaten` | maximal 50 Zeichen | `PLZ darf maximal 50 Zeichen haben` | `plz` | Bean Validation |

Hinweis: PLZ wird als Text gespeichert, nicht numerisch.

---

## 5.7 Feld `ort`

### Validierungen

| Endpunkt | Regel | Meldung | Fehlerfeld | Quelle |
|---|---|---|---|---|
| `PUT /api/members/{id}/stammdaten` | maximal 50 Zeichen | `Ort darf maximal 50 Zeichen haben` | `ort` | Bean Validation |

---

## 5.8 Feld `strasseHausNr`

### Validierungen

| Endpunkt | Regel | Meldung | Fehlerfeld | Quelle |
|---|---|---|---|---|
| `PUT /api/members/{id}/stammdaten` | maximal 50 Zeichen | `Straße/Hausnummer darf maximal 50 Zeichen haben` | `strasseHausNr` | Bean Validation |

---

## 5.9 Feld `geburtsdatum`

### Validierungen

Aktuell keine eigene fachliche Regel dokumentiert.

Hinweise:

- Optional.
- Bei Firmen fachlich nicht relevant.
- Das Frontend sollte `geburtsdatum` bei Firmen ausblenden oder deaktivieren.
- Erwartetes JSON-Format: `YYYY-MM-DD`.

---

# 6. Kontakt

## Endpunkt

```http
PUT /api/members/{mitgliedsnummer}/kontakt
```

## Felder

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

## Validierungen

| Feld | Regel | Meldung | Fehlerfeld | Quelle |
|---|---|---|---|---|
| `email` | gültige E-Mail, sofern angegeben | abhängig von Annotation im DTO | `email` | Bean Validation |
| `email` | maximal 100 Zeichen | abhängig von Annotation im DTO | `email` | Bean Validation / DB |
| `adresszusatz` | maximal 50 Zeichen | abhängig von Annotation im DTO | `adresszusatz` | Bean Validation / DB |

Hinweis: Für Telefonnummern ist aktuell nur eine leichte bzw. keine strenge fachliche Validierung vorgesehen.

---

# 7. Mitgliedschaft

## Endpunkt

```http
PUT /api/members/{mitgliedsnummer}/mitgliedschaft
```

## Beispiel

```json
{
  "eintritt": "2024-01-01",
  "austritt": null,
  "mitgliedsstatusId": 4,
  "stimmeId": 6,
  "kammerchor": false
}
```

## Validierungen

| Feld | Regel | Meldung | Fehlerfeld | Quelle |
|---|---|---|---|---|
| `mitgliedsstatusId` | muss in `tblMitgliederstatus_FT` existieren | `Ungültiger Mitgliederstatus` | `mitgliedsstatusId` | Service |
| `stimmeId` | muss in `tblStimme_FT` existieren | `Ungültige Stimme` | `stimmeId` | Service |
| `mitgliedsstatusId` | muss beim Update angegeben sein, sofern per DTO vorgeschrieben | abhängig von Annotation im DTO | `mitgliedsstatusId` | Bean Validation |
| `stimmeId` | muss beim Update angegeben sein, sofern per DTO vorgeschrieben | abhängig von Annotation im DTO | `stimmeId` | Bean Validation |

Hinweis: Eine Regel „Austritt darf nicht vor Eintritt liegen“ ist fachlich sinnvoll. Falls sie im Service noch nicht implementiert ist, sollte sie später ergänzt werden.

---

# 8. Datenschutz

## Endpunkte

```http
GET /api/members/{mitgliedsnummer}/datenschutz
PUT /api/members/{mitgliedsnummer}/datenschutz
```

## Beispiel

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

## Validierungen

| Feld | Regel | Meldung | Fehlerfeld | Quelle |
|---|---|---|---|---|
| `datumDatenschutz` | darf nicht in der Zukunft liegen | `Datum Datenschutz darf nicht in der Zukunft liegen` | `datumDatenschutz` | Service |

## Nicht gefunden

| Fall | Meldung | Fehlerformat |
|---|---|---|
| Datenschutz-Datensatz existiert nicht | `Datenschutz nicht gefunden für Mitglied {mitgliedsnummer}` | allgemeiner `ErrorResponse` |

---

# 9. Chorkleidung

## Endpunkte

```http
GET /api/members/{mitgliedsnummer}/chorkleidung
PUT /api/members/{mitgliedsnummer}/chorkleidung
```

## Beispiel

```json
{
  "ehemaligeStimme": "Tenor",
  "uebergabeAm": "2026-05-01T18:00:00",
  "bemerkungUebergabe": "Ausgegeben",
  "neubeschaffung": true,
  "datumAnteil": "2026-05-02T18:00:00",
  "barzahlung": false,
  "bearbeitungsstand": "offen",
  "rueckgabeAm": null,
  "bemerkungRueckgabe": null,
  "kaufdatum": "2026-05-02T18:00:00",
  "kaufpreis": 199.99,
  "sommerkleidung": true,
  "sommerkleidungErhalten": "2026-05-02T18:00:00",
  "sommerkleidungRueckgabe": null
}
```

## Validierungen

| Feld | Regel | Meldung | Fehlerfeld | Quelle |
|---|---|---|---|---|
| `rueckgabeAm` | darf nicht vor `uebergabeAm` liegen | `Rückgabe darf nicht vor Übergabe liegen` | `rueckgabeAm` | Service |
| `sommerkleidungRueckgabe` | darf nicht vor `sommerkleidungErhalten` liegen | `Sommerkleidung-Rückgabe darf nicht vor Erhalt liegen` | `sommerkleidungRueckgabe` | Service |
| `kaufpreis` | darf nicht negativ sein, sofern im DTO validiert | abhängig von Annotation im DTO | `kaufpreis` | Bean Validation |

## Nicht gefunden

| Fall | Meldung | Fehlerformat |
|---|---|---|
| Chorkleidung-Datensatz existiert nicht | `Chorkleidung nicht gefunden für Mitglied {mitgliedsnummer}` | allgemeiner `ErrorResponse` |

---

# 10. Mitglied anlegen

## Endpunkt

```http
POST /api/members
```

## Beispiel Person

```json
{
  "stammdaten": {
    "personFirma": false,
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

## Beispiel Firma

```json
{
  "stammdaten": {
    "personFirma": true,
    "vorname": "Firmenzusatz",
    "nachname": "Neue Firma GmbH",
    "plz": "99084",
    "ort": "Erfurt",
    "strasseHausNr": "Musterstraße 1"
  },
  "kontakt": {
    "email": "info@neue-firma.example"
  },
  "mitgliedschaft": {
    "mitgliedsstatusId": 4,
    "stimmeId": 6,
    "kammerchor": false
  }
}
```

## Validierungen bei Neuanlage

| Feld | Regel | Meldung | Fehlerfeld | Quelle |
|---|---|---|---|---|
| `stammdaten` | muss angegeben werden | `Stammdaten müssen angegeben werden` | aktuell allgemeiner Fehler oder Service-Fehler | Service |
| `personFirma` | falls nicht angegeben, Default `false` | keine Fehlermeldung | — | Service |
| `vorname` | Pflicht bei Person | `Vorname darf bei Personen nicht leer sein` | `vorname` | Service |
| `nachname` | Pflicht bei Person und Firma | `Nachname/Firmenname darf nicht leer sein` | `nachname` | Service |
| `mitgliedsstatusId` | muss existieren, sofern angegeben | `Ungültiger Mitgliederstatus` | `mitgliedsstatusId` | Service |
| `stimmeId` | muss existieren, sofern angegeben | `Ungültige Stimme` | `stimmeId` | Service |

---

# 11. Mapping für das Frontend

## 11.1 Feldmapping Stammdaten

| Backend-Feld | Person-Label im Frontend | Firma-Label im Frontend |
|---|---|---|
| `personFirma` | Person/Firma | Person/Firma |
| `vorname` | Vorname | Firmenzusatz |
| `nachname` | Nachname | Firmenname |
| `anrede` | Anrede | ausblenden/deaktivieren |
| `akademischerTitel` | Akademischer Titel | ausblenden/deaktivieren |
| `geburtsdatum` | Geburtsdatum | ausblenden/deaktivieren |
| `plz` | PLZ | PLZ |
| `ort` | Ort | Ort |
| `strasseHausNr` | Straße/Hausnummer | Straße/Hausnummer |

## 11.2 Fehlerfeld-Mapping

Das Frontend sollte `validationErrors[].field` direkt auf Formularfelder mappen.

Beispiele:

| `field` | Formularbereich | Feld |
|---|---|---|
| `personFirma` | Stammdaten | Person/Firma |
| `vorname` | Stammdaten | Vorname / Firmenzusatz |
| `nachname` | Stammdaten | Nachname / Firmenname |
| `mitgliedsstatusId` | Mitgliedschaft | Mitgliedsstatus |
| `stimmeId` | Mitgliedschaft | Stimme |
| `datumDatenschutz` | Datenschutz | Datum Datenschutz |
| `rueckgabeAm` | Chorkleidung | Rückgabe am |
| `sommerkleidungRueckgabe` | Chorkleidung | Sommerkleidung Rückgabe |

---

# 12. Testfälle für Frontend und Postman

## 12.1 Stammdaten Person erfolgreich

```json
{
  "personFirma": false,
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

Erwartung: `200 OK`

---

## 12.2 Person ohne Vorname

```json
{
  "personFirma": false,
  "vorname": "",
  "nachname": "Mustermann"
}
```

Erwartung: `400 Bad Request`

```json
{
  "message": "Validierungsfehler",
  "validationErrors": [
    {
      "field": "vorname",
      "message": "Vorname darf bei Personen nicht leer sein"
    }
  ]
}
```

---

## 12.3 Firma ohne Vorname/Firmenzusatz

```json
{
  "personFirma": true,
  "vorname": "",
  "nachname": "Neue Firma GmbH"
}
```

Erwartung: `200 OK`

---

## 12.4 Firma ohne Nachname/Firmenname

```json
{
  "personFirma": true,
  "vorname": "Zusatz",
  "nachname": ""
}
```

Erwartung: `400 Bad Request`

```json
{
  "message": "Validierungsfehler",
  "validationErrors": [
    {
      "field": "nachname",
      "message": "Nachname darf nicht leer sein"
    }
  ]
}
```

Hinweis: Je nach Validierungspfad kann alternativ `Nachname/Firmenname darf nicht leer sein` geliefert werden.

---

## 12.5 Ungültiger Mitgliederstatus

```json
{
  "eintritt": "2024-01-01",
  "austritt": null,
  "mitgliedsstatusId": 9999,
  "stimmeId": 6,
  "kammerchor": false
}
```

Erwartung: `400 Bad Request`

```json
{
  "message": "Validierungsfehler",
  "validationErrors": [
    {
      "field": "mitgliedsstatusId",
      "message": "Ungültiger Mitgliederstatus"
    }
  ]
}
```

---

## 12.6 Ungültige Stimme

```json
{
  "eintritt": "2024-01-01",
  "austritt": null,
  "mitgliedsstatusId": 4,
  "stimmeId": 9999,
  "kammerchor": false
}
```

Erwartung: `400 Bad Request`

```json
{
  "message": "Validierungsfehler",
  "validationErrors": [
    {
      "field": "stimmeId",
      "message": "Ungültige Stimme"
    }
  ]
}
```

---

## 12.7 Datenschutzdatum in der Zukunft

```json
{
  "datumDatenschutz": "2999-01-01T00:00:00",
  "datenschutzNr14": true,
  "datenschutzNr15": false,
  "datenschutzNr16": false,
  "datenschutzNr17": false,
  "datenschutzNr18": false
}
```

Erwartung: `400 Bad Request`

```json
{
  "message": "Validierungsfehler",
  "validationErrors": [
    {
      "field": "datumDatenschutz",
      "message": "Datum Datenschutz darf nicht in der Zukunft liegen"
    }
  ]
}
```

---

## 12.8 Chorkleidung Rückgabe vor Übergabe

```json
{
  "uebergabeAm": "2026-05-10T10:00:00",
  "rueckgabeAm": "2026-05-01T10:00:00"
}
```

Erwartung: `400 Bad Request`

```json
{
  "message": "Validierungsfehler",
  "validationErrors": [
    {
      "field": "rueckgabeAm",
      "message": "Rückgabe darf nicht vor Übergabe liegen"
    }
  ]
}
```

---

# 13. Hinweise für künftige Erweiterungen

Bei neuen fachlichen feldbezogenen Regeln sollte bevorzugt `BusinessValidationException` verwendet werden.

Empfohlenes Muster:

```text
throwValidationError("feldname", "Fehlermeldung");
```

Damit bleibt das Fehlerformat für das Frontend einheitlich.

Nicht feldbezogene Fehler können weiterhin als `BadRequestException`, `NotFoundException` oder andere allgemeine Fehler behandelt werden.

---

# 14. Offene bzw. zu prüfende Punkte

| Thema | Status |
|---|---|
| Automatisierte Unit-/Integrationstests | geplant in separatem Schritt |
| Vollständiger Abgleich aller Bean-Validation-Meldungen in DTOs | bei Bedarf nachziehen |
| Mitgliedschaftsregel `Austritt darf nicht vor Eintritt liegen` | fachlich sinnvoll, Implementierungsstand prüfen |
| Einheitliche Feldnamen bei verschachtelten POST-Requests | Frontend sollte Mapping testen |
| PROD-Deployment der Person/Firma-Erweiterung | noch nicht erfolgt |
