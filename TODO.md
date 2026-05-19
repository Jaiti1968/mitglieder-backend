# TODO – EMC Mitgliederverwaltung Backend

## Priorität Hoch

### Security Hardening (Phase 4)

- [ ] Passwortregeln verschärfen
    - Mindestlänge > 8
    - Groß-/Kleinbuchstaben
    - Zahlen
    - Sonderzeichen
    - sichere Passwortvalidierung Backend

- [ ] Initialpasswort automatisch generieren

- [ ] Passwortwechsel beim ersten Login erzwingen

- [ ] Passwort Reset Workflow statt direktem Passwortsetzen durch Admin

- [ ] Schutz letzter aktiver ADMIN
    - letzter ADMIN darf nicht deaktiviert werden
    - letzter ADMIN darf nicht auf andere Rolle gesetzt werden

- [ ] Session Timeout / Auto Logout

- [ ] Session Invalidierung bei Rollenänderung / Deaktivierung

- [ ] Fehlversuchszähler für Login

- [ ] temporäre Kontosperre bei mehrfach falscher Anmeldung

- [ ] Recovery-Konzept für Admin-Zugang
    - mindestens 2 ADMIN Benutzer
    - Notfallstrategie bei Passwortverlust

---

## Priorität Mittel

### API / Architektur

- [ ] PATCH statt PUT für echte Teilupdates prüfen

- [ ] Swagger / OpenAPI Dokumentation

- [ ] Integration Tests für kritische End-to-End Backend-Flows
    - Auth
    - Security
    - Mitglieder CRUD
    - Benutzerverwaltung

- [ ] Docker Healthcheck

- [ ] Flyway Migrationen vollständig etablieren
  (statt manueller DB-Strukturpflege)

---

## Priorität Mittel / Fachlichkeit

### Nachvollziehbarkeit / Historie

- [ ] Audit Logging
    - wer hat was geändert
    - wann wurde geändert
    - welcher Benutzer

- [ ] Änderungsverlauf / Historisierung
    - Mitgliederdaten
    - Benutzerverwaltung
    - sicherheitsrelevante Änderungen

---

## Später / Erweiterungen

### Fachliche Features

- [ ] Ehrungen

- [ ] Funktionen / Rollen im Chor

- [ ] Verteiler / Gruppen

- [ ] Berichte / Auswertungen

- [ ] Exportfunktionen

- [ ] Mailversand

---

## Erledigt

- [x] Backend DTOs auf LocalDate harmonisieren

- [x] Session-basierte Authentifizierung

- [x] Rollenmodell (ADMIN / EDITOR / VIEWER)

- [x] Login / Logout / Session Restore

- [x] Admin Benutzerverwaltung Backend

- [x] Security Tests Grundgerüst

- [x] neutrales Login-Fehlerverhalten