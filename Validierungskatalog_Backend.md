# Validierungskatalog Backend – EMC Mitgliederverwaltung

**Dokumentstand:** 17.11.2026

**Codebasis:** `mitglieder-backend(3).zip`, Backend-Version `1.3.0-SNAPSHOT`
**Status:** Konsolidierte Arbeitsfassung nach fachlicher Abstimmung; keine Änderung am Anwendungscode

## 1. Zweck und Geltungsbereich

Dieser Katalog beschreibt die im Spring-Boot-Backend implementierten Eingabevalidierungen, fachlichen Prüfungen und API-Fehlerantworten. Grundlage sind die Request-DTOs, Controller, Services, die zentrale Fehlerbehandlung und relevante Tests des bereitgestellten Backend-Projekts. Er beschreibt **nicht** die Frontend-Formularlogik. Wo eine Regel nur auf einem bestimmten Verarbeitungspfad greift, wird das ausdrücklich angegeben.

Technische Basis: Spring Web, Jakarta Bean Validation, Spring Security, `JdbcTemplate` und MariaDB. Datumsangaben werden in den betreffenden Requests als `LocalDate` verarbeitet und als `YYYY-MM-DD` übertragen; es ist keine Uhrzeit Bestandteil dieser Felder.

## 2. Fehlerformate

### 2.1 Feldbezogene Validierungsfehler: `ApiErrorResponse`

Bei Bean Validation (`MethodArgumentNotValidException`) und fachlichen Feldfehlern (`BusinessValidationException`) wird HTTP `400 Bad Request` mit `validationErrors` zurückgegeben.

```json
{
  "timestamp": "2026-11-17T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validierungsfehler",
  "path": "/api/members/N1001/stammdaten",
  "requestId": "beispiel-request-id",
  "validationErrors": [
    {
      "field": "vorname",
      "message": "Vorname darf bei Personen nicht leer sein"
    }
  ]
}
```

Die Beispielwerte für Zeitstempel und Request-ID sind Platzhalter. `validationErrors` enthält Feldnamen und zugehörige Fehlermeldungen.

### 2.2 Allgemeine Fehler: `ErrorResponse`

Für `NotFoundException`, `BadRequestException`, `ResponseStatusException`, Datenbankkonflikte und unerwartete Fehler wird das allgemeine Format ohne `validationErrors` verwendet.

```json
{
  "timestamp": "2026-11-17T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Mitglied mit Nummer N9999 wurde nicht gefunden.",
  "path": "/api/members/N9999/honors",
  "requestId": "beispiel-request-id"
}
```

Der `GlobalExceptionHandler` behandelt unter anderem:

| Ausnahme / Fehler | HTTP | Verhalten |
|---|---:|---|
| `MethodArgumentNotValidException` | 400 | Feldfehler (`ApiErrorResponse`) |
| `BusinessValidationException` | 400 | Fachliche Feldfehler (`ApiErrorResponse`) |
| `BadRequestException` | 400 | Allgemeiner fachlicher Fehler |
| `NotFoundException` | 404 | Ressource nicht gefunden |
| `ResponseStatusException` | Je nach Status | Fachliche Meldung aus dem Service |
| `DuplicateKeyException` | 409 | Eindeutigkeitsverletzung |
| `DataIntegrityViolationException` | 400 | Datenbank-Regelverletzung |
| Fehlerhafte Anmeldedaten / gesperrter oder deaktivierter Benutzer | 401 | Neutrale Fehlermeldung |
| Sonstige unerwartete Ausnahme | 500 | Neutrale Fehlermeldung; technische Details im Log |

Security-Antworten `401` und `403` müssen nicht in jedem Fall dasselbe JSON-Format wie der `GlobalExceptionHandler` besitzen; sie können bereits in der Spring-Security-Filterkette entstehen.

## 3. HTTP-Statuscodes

| Status | Verwendung |
|---|---|
| `200 OK` | Erfolgreiche Lese- und bestimmte Änderungsoperationen, darunter Ehrungen-GET/PUT |
| `201 Created` | Erfolgreiches Anlegen einer Ehrung (POST) |
| `204 No Content` | Erfolgreiche Operation ohne Antwortkörper, darunter Ehrung löschen und administrative Schreiboperationen |
| `400 Bad Request` | Bean Validation, fachliche Feldfehler oder ungültige Anfrage |
| `401 Unauthorized` | Fehlende oder ungültige Authentifizierung |
| `403 Forbidden` | Authentifiziert, aber nicht berechtigt |
| `404 Not Found` | Mitglied, Ehrung oder andere Ressource nicht vorhanden bzw. nicht zugeordnet |
| `409 Conflict` | Eindeutigkeitsverletzung oder fachlicher Konflikt |
| `500 Internal Server Error` | Unerwarteter technischer Fehler |

Statuscodes sind **operationsabhängig**. Insbesondere darf `204` nicht pauschal für alle POST- oder PUT-Endpunkte angenommen werden.

## 4. Authentifizierung und Autorisierung

Das Backend verwendet serverseitige Sessions. Login: `POST /api/auth/login`; Logout: `POST /api/auth/logout`; Session-Abfrage: `GET /api/auth/me`. Fehlgeschlagene Anmeldungen werden neutral beantwortet: `Anmeldung nicht möglich.`

Die Rollen sind `ADMIN`, `EDITOR` und `VIEWER`.

| Bereich | ADMIN | EDITOR | VIEWER |
|---|:---:|:---:|:---:|
| Mitglieder lesen, Ehrungen lesen, Lookups lesen | Ja | Ja | Ja |
| Mitglieder anlegen und bearbeiten | Ja | Ja | Nein |
| Ehrungen anlegen, bearbeiten und löschen | Ja | Ja | Nein |
| Mitglied löschen: Sicherheitsregel für DELETE-Pfad | Ja | Nein | Nein |
| Reporting | Ja | Nein | Nein |
| Benutzerverwaltung | Ja | Nein | Nein |

Die DELETE-Regel für Mitglieder in der Sicherheitskonfiguration ist **nicht** gleichbedeutend mit einem vorhandenen fachlichen DELETE-Endpunkt im `MemberController`. Das Löschen einer Ehrung ist davon ausdrücklich getrennt.

## 5. Stammdaten

**Endpunkte:** `POST /api/members`, `PUT /api/members/{mitgliedsnummer}/stammdaten`.

| Feld | Regel |
|---|---|
| `personFirma` | Bei Stammdaten-Update Pflicht (`@NotNull`); bei Neuanlage wird fehlender Wert in der Service-Prüfung als `false` behandelt |
| `vorname` | Bei natürlichen Personen Pflicht; im Update-DTO maximal 50 Zeichen |
| `nachname` | Pflicht für Person und Firma; im Update-DTO maximal 50 Zeichen |
| `anrede` | Im Update-DTO maximal 50 Zeichen |
| `akademischerTitel` | Im Update-DTO maximal 50 Zeichen |
| `plz` | Im Update-DTO maximal 50 Zeichen |
| `ort` | Im Update-DTO maximal 50 Zeichen |
| `strasseHausNr` | Im Update-DTO maximal 50 Zeichen |
| `geburtsdatum` | Optionales `LocalDate`; keine zusätzliche fachliche Grenze in der geprüften Stammdatenvalidierung |

Die Fachprüfung im `MemberService` meldet unter anderem:

- `personFirma`: `Person/Firma muss angegeben werden`
- `nachname`: `Nachname/Firmenname darf nicht leer sein`
- `vorname`: `Vorname darf bei Personen nicht leer sein`

**Abgrenzung:** Die hier genannten `@Size`-Regeln stammen aus `UpdateStammdatenRequest`. Für die Neuanlage werden andere DTOs verwendet; die Update-Annotationen dürfen nicht ohne weitere Prüfung als identische POST-Validierung ausgegeben werden.

## 6. Kontakt

**Endpunkt:** `PUT /api/members/{mitgliedsnummer}/kontakt`.

| Feld | Implementierte Bean Validation |
|---|---|
| `telefonPrivat` | Maximal 50 Zeichen |
| `telefonGeschaeftlich` | Maximal 50 Zeichen |
| `mobiltelefon` | Maximal 50 Zeichen |
| `email` | Gültiges E-Mail-Format (`@Email`), maximal 100 Zeichen |
| `adresszusatz` | Maximal 50 Zeichen |
| `briefanrede` | Maximal 100 Zeichen |

Für Telefonnummern gibt es keine Prüfung auf ein bestimmtes Telefonnummernformat. Die Felder sind im geprüften Update-DTO nicht als Pflichtfelder markiert.

## 7. Mitgliedschaft

**Endpunkt:** `PUT /api/members/{mitgliedsnummer}/mitgliedschaft`; Mitgliedschaftsdaten werden auch bei `POST /api/members` übergeben.

| Feld | Regel |
|---|---|
| `eintritt` | Optionales `LocalDate` |
| `austritt` | Optionales `LocalDate` |
| `mitgliedsstatusId` | Beim Update `@NotNull`; bei vorhandenem Wert Prüfung gegen Mitgliederstatus-Lookup |
| `stimmeId` | Beim Update `@NotNull`; bei vorhandenem Wert Prüfung gegen Stimmen-Lookup |
| `kammerchor` | Optionaler boolescher Wert |

Die Methode `validateMitgliedschaft()` prüft, dass `austritt` bei vorhandenen Vergleichsdaten nicht vor `eintritt` liegt. Meldung: `Austritt darf nicht vor Eintritt liegen`. Außerdem werden vorhandene Lookup-IDs geprüft (`Ungültiger Mitgliederstatus`, `Ungültige Stimme`).

**Bekannte Einschränkung im aktuellen Code:** Beim `updateMitgliedschaft()` wird `validateMitgliedschaft()` mit einem neu konstruierten `MitgliedschaftDto` aufgerufen, dessen Datumswerte `null` sind. Die Eintritt-/Austritt-Reihenfolge wird daher **bei diesem Update auf Service-Ebene nicht geprüft**. Bei `createMember()` wird die vollständige Mitgliedschaft an die Prüfung übergeben. Dieser Unterschied ist ein offener technischer Prüfpunkt, keine als durchgehend implementiert anzusehende Regel.

## 8. Datenschutz

**Endpunkte:** `GET /api/members/{mitgliedsnummer}/datenschutz`, `PUT /api/members/{mitgliedsnummer}/datenschutz`.

Felder: `datumDatenschutz`, `datenschutzNr14`, `datenschutzNr15`, `datenschutzNr16`, `datenschutzNr17`, `datenschutzNr18`.

`datumDatenschutz` ist optional. Ist ein Datum angegeben, darf es nicht nach dem aktuellen Datum liegen. Die Service-Prüfung meldet: `Datum Datenschutz darf nicht in der Zukunft liegen`. Die weiteren genannten Felder sind optionale boolesche Werte im Update-DTO.

Diese Regel ist **bereichsspezifisch**; sie gilt nicht für Ehrungsdaten.

## 9. Chorkleidung

**Endpunkte:** `GET /api/members/{mitgliedsnummer}/chorkleidung`, `PUT /api/members/{mitgliedsnummer}/chorkleidung`.

| Feld / Konstellation | Regel |
|---|---|
| `ehemaligeStimme` | Maximal 50 Zeichen |
| `rueckgabeAm` und `uebergabeAm` | Falls beide vorhanden: Rückgabe nicht vor Übergabe |
| `sommerkleidungRueckgabe` und `sommerkleidungErhalten` | Falls beide vorhanden: Rückgabe nicht vor Erhalt |
| `kaufpreis` | Nicht negativ (`@DecimalMin("0.00")`), maximal acht Vorkommastellen und zwei Nachkommastellen (`@Digits`) |

Die fachlichen Fehlermeldungen lauten `Rückgabe darf nicht vor Übergabe liegen` bzw. `Sommerkleidung-Rückgabe darf nicht vor Erhalt liegen`. Die Datumsfelder sind optional. Weitere Felder des Chorkleidung-Requests werden hier nicht als Pflichtfelder ausgewiesen, da die geprüften DTO-Annotationen dafür keine entsprechende Regel enthalten.

## 10. Mitglied anlegen

**Endpunkt:** `POST /api/members`.

`CreateMemberRequest` verlangt ein `stammdaten`-Objekt und ein `mitgliedschaft`-Objekt (`@NotNull`). `kontakt` ist optional. Die Service-Validierung prüft bei den Stammdaten insbesondere die Person/Firma-Unterscheidung und die Pflichtangaben für Vor- und Nachname. Die Mitgliedschaft wird über `validateMitgliedschaft()` geprüft; die Prüfung von Lookup-IDs erfolgt, sofern diese gesetzt sind.

Die genaue Bean-Validation-Abdeckung der verschachtelten DTOs ist von deren Annotationen und der Kaskadierung abhängig. Der Katalog unterscheidet deshalb bewusst zwischen Service-Regeln und DTO-Annotationen.

## 11. Ehrungen – BL-009

### 11.1 Endpunkte

```http
GET    /api/members/{mitgliedsnummer}/honors
POST   /api/members/{mitgliedsnummer}/honors
PUT    /api/members/{mitgliedsnummer}/honors/{honorId}
DELETE /api/members/{mitgliedsnummer}/honors/{honorId}
GET    /api/lookups/honors
```

Die Ehrungsarten stammen aus `tblEhrungen_FT` (`IDEhrungen`, `Ehrungen`). Die zugeordneten Ehrungen stehen in `tblEhrungen` und werden über `Mitgliedsnummer` sowie `IDTabEhrungen` identifiziert.

### 11.2 Request-Felder

POST und PUT verwenden `CreateMemberHonorRequest`.

| Feld | Regel |
|---|---|
| `honorTypeId` | Pflichtfeld (`@NotNull`); muss in `tblEhrungen_FT` vorhanden sein |
| `honorDate` | Optional (`LocalDate`); vergangene, heutige und **zukünftige Daten sind ausdrücklich zulässig** |

Für `honorDate` existiert **keine** Prüfung gegen das heutige Datum. Ein vorhandenes Datum wird als `YYYY-MM-DD` übertragen. Ein fehlendes Datum kann gespeichert werden; bei PUT lässt es sich auch entfernen.

### 11.3 Fachliche Prüfungen

- Das Mitglied muss existieren; sonst `404`.
- Die Ehrungsart muss angegeben und als Lookup vorhanden sein; sonst `400`.
- Eine Ehrungsart darf demselben Mitglied nur **einmal** zugeordnet werden, unabhängig vom Ehrungsdatum; bei Doppelzuordnung `409`.
- Bei PUT darf die bisherige Ehrungsart unverändert bleiben. Eine andere Ehrungsart darf nur verwendet werden, wenn sie dem Mitglied noch nicht zugeordnet ist.
- Bei PUT und DELETE muss die Ehrung zur angegebenen Mitgliedsnummer gehören. Eine unbekannte oder fremde Ehrung wird mit `404` beantwortet.
- DELETE entfernt den Datensatz aus `tblEhrungen`; eine Archivierung ist nicht Bestandteil der implementierten Operation.

### 11.4 Erfolgs- und Fehlerantworten

| Operation | Erfolg | Typische fachliche Fehler |
|---|---|---|
| GET | `200 OK` | `404` unbekanntes Mitglied |
| POST | `201 Created` mit Ehrungsdatensatz | `400`, `404`, `409` |
| PUT | `200 OK` mit aktualisiertem Datensatz | `400`, `404`, `409` |
| DELETE | `204 No Content` | `404` |

Zusätzlich: `401` ohne Authentifizierung und `403` bei unzureichender Rolle. `ADMIN` und `EDITOR` dürfen schreiben und löschen; `VIEWER` darf nur lesen.

**Meldungen aus dem Service:**

| Fall | Status | Meldung |
|---|---:|---|
| Ehrungsart fehlt (Service-Prüfung) | 400 | `Die Ehrungsart muss angegeben werden.` |
| Ehrungsart existiert nicht | 400 | `Die angegebene Ehrungsart existiert nicht.` |
| Ehrungsart bereits zugeordnet | 409 | `Diese Ehrungsart ist dem Mitglied bereits zugeordnet.` |
| Mitglied fehlt | 404 | `Mitglied mit Nummer ... wurde nicht gefunden.` |
| Ehrung fehlt oder gehört anderem Mitglied | 404 | `Die Ehrung wurde für dieses Mitglied nicht gefunden.` |

**Hinweis zur Fehlermeldung bei `honorTypeId = null`:** Da das Request-DTO `@NotNull` verwendet und der Controller `@Valid`, kann die Bean Validation bereits vor der Service-Prüfung greifen. Dann wird ein feldbezogener `ApiErrorResponse` statt der oben angegebenen Service-Meldung erzeugt.

### 11.5 Nachgewiesene Tests für Datumsregeln

Im bereitgestellten Projekt existieren unter anderem:

- `MemberHonorCreateIntegrationTest.shouldCreateHonorWithFutureDate()`
- `MemberHonorUpdateIntegrationTest.shouldAllowFutureHonorDate()`
- `MemberHonorUpdateIntegrationTest.shouldRemoveHonorDate()`
- Ergänzende Service-Tests für zukünftige Ehrungsdaten

Damit ist die Zulässigkeit zukünftiger Ehrungsdaten beim Anlegen und Bearbeiten ausdrücklich getestet. Für BL-009 besteht hierzu keine offene fachliche Entscheidung.

## 12. Administrative Benutzerverwaltung

**Basis:** `/api/admin/users`; Zugriff nur mit Rolle `ADMIN`.

### 12.1 Benutzer anlegen

**Endpunkt:** `POST /api/admin/users`.

| Feld | Regel |
|---|---|
| `username` | Pflicht (`@NotBlank`), maximal 50 Zeichen, eindeutig |
| `password` | Pflicht (`@NotBlank`), mindestens acht Zeichen |
| `role` | Pflicht (`@NotBlank`), ausschließlich `ADMIN`, `EDITOR` oder `VIEWER` (`@Pattern`) |

Das Passwort wird vor dem Speichern gehasht. Die Neuanlage erfolgt gemäß Repository-Implementierung als aktiver Benutzer.

### 12.2 Rolle ändern

**Endpunkt:** `PUT /api/admin/users/{id}/role`. Die Rolle muss angegeben werden und einem der drei zulässigen Werte entsprechen. Der Benutzer muss existieren. Der letzte aktive Administrator darf nicht herabgestuft werden.

### 12.3 Aktivstatus ändern

**Endpunkt:** `PUT /api/admin/users/{id}/active`. Der Benutzer muss existieren. Der letzte aktive Administrator darf nicht deaktiviert werden.

`UpdateUserActiveRequest` verwendet ein primitives `boolean active` ohne `@NotNull`. Ein fehlender JSON-Wert ist deshalb **nicht** durch eine explizite Bean-Validation-Pflichtfeldregel abgesichert. Die Formulierung „Boolean Request erforderlich“ aus dem alten Katalog wäre dafür zu pauschal.

### 12.4 Passwort ändern

**Endpunkt:** `PUT /api/admin/users/{id}/password`. Das Passwort ist Pflicht und muss mindestens acht Zeichen lang sein. Weitere Zusammensetzungsregeln (etwa verpflichtende Sonderzeichen) sind in diesem Request-DTO nicht hinterlegt.

### 12.5 Fehlerverhalten

| Fall | Status |
|---|---:|
| Bean-Validation-Fehler | 400 |
| Benutzer nicht gefunden | 404 |
| Benutzername bereits vergeben | 409 |
| Letzten aktiven ADMIN deaktivieren oder herabstufen | 409 |
| Fehlende ADMIN-Berechtigung | 403 |

Erfolgreiche administrative Schreiboperationen liefern `204 No Content`.

## 13. Testbeispiele und Nachweise

| Testfall | Erwartung |
|---|---|
| Ungültiger Login | `401 Unauthorized`, neutrale Meldung |
| VIEWER versucht Stammdaten-Update | `403 Forbidden` |
| Ungültiger Mitgliederstatus | `400 Bad Request` |
| Unbekanntes Mitglied bei Ehrungs-GET | `404 Not Found` |
| Doppelte Ehrungsart bei demselben Mitglied | `409 Conflict` |
| Zukünftiges Ehrungsdatum bei POST oder PUT | Zulässig |
| Ehrungsdatum bei PUT entfernen | Zulässig (`null`) |
| EDITOR löscht Ehrung | `204 No Content` |
| VIEWER löscht Ehrung | `403 Forbidden` |
| Ehrung gehört anderem Mitglied | `404 Not Found` |
| Doppelt vergebener Benutzername | `409 Conflict` |

Zum Zeitpunkt der vorangegangenen BL-009-Abschlussprüfung wurden **29 Testklassen und 162 erfolgreiche Tests** gemeldet (`.\mvnw.cmd clean verify`, `BUILD SUCCESS`). Diese Zahlen sind eine dokumentierte Momentaufnahme, keine in dieser Dokumentationsbearbeitung erneut ausgeführte Testserie.

## 14. Regeln für Erweiterungen

Neue feldbezogene fachliche Validierungen sollten mit `BusinessValidationException` und einem `ValidationErrorDto` ausgegeben werden. Das bestehende Muster im `MemberService` lautet:

```java
throwValidationError("feldname", "Fehlermeldung");
```

Für allgemeine fachliche Fehler stehen unter anderem `BadRequestException` und `NotFoundException` zur Verfügung. Konflikte können mit einem passenden HTTP-Status, etwa über `ResponseStatusException`, beantwortet werden. Technische Ausnahmen werden zentral behandelt.

Neue Regeln müssen mit dem konkreten API-Verarbeitungspfad abgestimmt und durch Tests abgesichert werden. Eine fachliche Einschränkung darf nicht allein aus dem Datentyp `LocalDate` abgeleitet werden.

## 15. Offene technische Prüfpunkte und geplante Erweiterungen

### 15.1 Mitgliedschaft: Datumsprüfung beim Update

`MemberService.updateMitgliedschaft()` übergibt derzeit `null` für Eintritt und Austritt an `validateMitgliedschaft()`. Die Reihenfolgeprüfung greift deshalb beim Update auf Service-Ebene nicht. **Prüfpunkt:** Gewünschte Regel und Umsetzung für diesen Pfad separat klären; keine Codeänderung durch diesen Katalog.

### 15.2 Sicherheitsfunktionen

Im älteren Katalog genannte Themen wie Passwortwechsel beim Erstlogin, Initialpasswort, Passwort-Reset-Prozess, Fehlversuchszähler und temporäre Sperre sind gesondert anhand ihrer jeweiligen Anforderungen zu verfolgen. Die **Mindestpasswortlänge von acht Zeichen** und der **Session Timeout** sind dagegen bereits implementiert und werden nicht mehr pauschal als fehlende Funktionen aufgeführt.

### 15.3 Datumsregeln bleiben bereichsspezifisch

- Ehrungen: zukünftige Daten erlaubt und getestet.
- Datenschutz: zukünftiges Datum derzeit verboten.
- Mitgliedschaft: Reihenfolgeprüfung mit der oben beschriebenen Update-Einschränkung.

Eine pauschale Backend-Regel „keine Datumswerte in der Zukunft“ existiert nicht und wird hier nicht eingeführt.

---

**Dokumentationshinweis:** Dieser Katalog ist eine Bestandsbeschreibung des bereitgestellten Codes. Er stellt weder eine Freigabe neuer fachlicher Regeln noch einen Nachweis eines erneuten Builds dar. Weiterführende Projekt- und Betriebsunterlagen befinden sich bis zur geplanten Konsolidierung in EMC-DOKUMENTATION und EMC-INFRA.
