# Cloud-Labyrinth – Client

[![pr-checks](https://github.com/WS-2026-Cloud-Application-Project/cloud-labyrinth-client/actions/workflows/pr-checks.yml/badge.svg)](https://github.com/WS-2026-Cloud-Application-Project/cloud-labyrinth-client/actions/workflows/pr-checks.yml)
<!-- TODO DevOps: Badges für ci.yml und release-deploy.yml ergänzen, sobald die Workflows existieren -->
![Java](https://img.shields.io/badge/Java-17-blue)
![JavaFX](https://img.shields.io/badge/JavaFX-21-orange)

JavaFX-Desktop-Client für **„Das verrückte Labyrinth“**. Er verbindet sich per WebSocket mit dem
[Labyrinth-Server](https://github.com/WS-2026-Cloud-Application-Project/cloud-labyrinth-server), der im Rahmen des
*Cloud Application Project* (MCI, WS 2026/27, Gruppe 3) auf Apache CloudStack betrieben wird.
Ausgangspunkt ist der Client aus dem Advanced Integrative Project (AIP), Stand `40ffff1`.

> **Lebendes Dokument:** Bei jeder Änderung an Build, Start oder Konfiguration wird diese README im selben PR angepasst.
> Regeln, Konzepte und Projektdoku liegen zentral im Server-Repo (siehe [CONTRIBUTING.md](CONTRIBUTING.md)).

---

## Inhalt

1. [Überblick](#überblick)
2. [Schnellstart](#schnellstart)
3. [Mit dem Server verbinden](#mit-dem-server-verbinden)
4. [Konfiguration](#konfiguration)
5. [Entwicklung](#entwicklung)
6. [Tests](#tests)
7. [Releases](#releases)
8. [Offene Punkte](#offene-punkte)

---

## Überblick

- Screen-Flow: Serverauswahl → Lobby → Spiel → Ergebnis
- Server-Discovery über die Server-Registry (mit Fallback auf manuelle Eingabe)
- Kommunikation per WebSocket (`/game`, JSON-Nachrichten nach [labyrinth-interface](https://github.com/thomasklammer/labyrinth-interface))
- Optionaler KI-Modus (benötigt OpenAI-API-Key)

Der Server ist die *Single Source of Truth*: Der Client sendet Aktionen, der Server prüft sie und verteilt den Spielzustand.

**Aktuelle Version:** _siehe [Releases](https://github.com/WS-2026-Cloud-Application-Project/cloud-labyrinth-client/releases)_

## Schnellstart

### Voraussetzungen

- JDK 17
- Maven 3.9 (ein Maven Wrapper fehlt noch, siehe [Offene Punkte](#offene-punkte))

### Starten

```bash
cd app
mvn -Djavafx.platform=win javafx:run           # Windows
mvn -Djavafx.platform=mac-aarch64 javafx:run   # macOS Apple Silicon
mvn -Djavafx.platform=mac javafx:run           # macOS Intel
mvn -Djavafx.platform=linux javafx:run         # Linux
```

## Mit dem Server verbinden

1. Der Serverauswahl-Screen lädt die verfügbaren Server aus der Registry (`https://mgmt.dvl.spalx.dev/servers`).
2. Server auswählen, Benutzernamen eingeben.
3. In der Lobby auf weitere Spieler warten – der erste Spieler ist Admin und startet das Spiel.

Falls die Discovery nicht funktioniert, die Adresse manuell eingeben:

| Ziel | WebSocket-Adresse |
|---|---|
| Lokaler Server | `ws://localhost:<PORT>/game` (Port steht im Server-Log, 8397–8405) |
| Lokaler Server (Docker) | `ws://localhost:8397/game` |
| Testserver (`development`) | `ws://<TODO>:8397/game` |
| Produktion | `ws://<TODO>:8397/game` |

## Konfiguration

| Einstellung | Wo | Bedeutung |
|---|---|---|
| `OPENAI_API_KEY` | `app/.env` (Vorlage: [`app/.env.example`](app/.env.example)) oder Umgebungsvariable | nur für den KI-Modus |
| Registry-Adresse | `app/src/main/resources/application.properties` | Server-Discovery |

Echte Werte gehören **nie** ins Repo – `.env` ist per `.gitignore` ausgeschlossen.

## Entwicklung

Es gelten die Regeln aus dem Server-Repo ([QUALITY.md](https://github.com/WS-2026-Cloud-Application-Project/cloud-labyrinth-server/blob/dev/QUALITY.md),
[GitHub-Konzept](https://github.com/WS-2026-Cloud-Application-Project/cloud-labyrinth-server/blob/dev/docs/github-konzept.md)). Kurzfassung:

1. Einmalig: `pip install pre-commit` und `pre-commit install --hook-type pre-commit --hook-type commit-msg`
2. Branch von `dev` abzweigen: `feature/<issue>-<kurzname>`, `fix/…`, `chore/…`, `docs/…`
3. Commits und PR-Titel nach Conventional Commits (`feat: …`, `fix: …`)
4. PR nach `dev`, 2 Approvals, Squash-Merge; Release-PR `dev` → `main`
5. Änderungen am Nachrichtenformat immer mit dem Server-Repo abstimmen

Codestyle: [app/CODESTYLE.md](app/CODESTYLE.md)

## Tests

```bash
cd app
mvn test
```

| Testart | Status |
|---|---|
| Unit-Tests (JUnit, Mockito) | ✅ vorhanden |
| UI-Tests (TestFX) | ✅ vorhanden, lokal mit Bildschirm |
| UI-Tests ohne Bildschirm in der CI (Monocle) | ⬜ geplant |
| Client gegen echten Server-Container | ⬜ geplant |

## Releases

Der Client ist eine Desktop-Anwendung und wird nicht auf die Server deployt.
Die CI soll ausführbare Pakete bauen und an die GitHub-Releases hängen:

| Branch | Release-Art | verbindet sich standardmäßig mit |
|---|---|---|
| `dev` | Pre-Release (`vX.Y.Z-dev.N`) | Testserver |
| `main` | Release (`vX.Y.Z`) | Produktion |

Details: [GitHub-Konzept, Abschnitt 7](https://github.com/WS-2026-Cloud-Application-Project/cloud-labyrinth-server/blob/dev/docs/github-konzept.md#7-deploy)

## Offene Punkte

<!-- Laufend pflegen; Erledigtes entfernen -->
- [ ] Maven Wrapper hinzufügen (`mvn wrapper:wrapper`)
- [ ] Doppelte JUnit-/Mockito-Abhängigkeiten in der `pom.xml` bereinigen
- [ ] TestFX ohne Bildschirm (Monocle) für die CI
- [ ] Server-Adresse pro Umgebung konfigurierbar machen
- [ ] Ausführbare Pakete (jpackage) für Windows, macOS, Linux
- [ ] `app/.env.example` und `app/README.md` aktualisieren (verweisen noch auf `ENV_TEMPLATE` bzw. das alte AIP-Repo)

## Weitere Dokumentation

- Details zur Anwendung (aus AIP): [app/README.md](app/README.md)
- Projektdoku, ADRs, Protokolle: [Server-Repo `docs/`](https://github.com/WS-2026-Cloud-Application-Project/cloud-labyrinth-server/tree/dev/docs)

---

Studienprojekt im Kurs *Cloud Application Project*, MCI Management Center Innsbruck.
