# Das verrückte Labyrinth – Client

![Build Status](https://github.com/AIP-Labyrinth-Group3/labyrinth-client/workflows/Client%20CI/badge.svg)
![Java](https://img.shields.io/badge/Java-17-blue)

JavaFX Desktop-Client für **„Das verrückte Labyrinth“** (MCI AIP Projekt WS 2025/26).
Der Client verbindet sich per WebSocket mit dem Game-Server und zeigt den Spielzustand UI-seitig an (Server ist **Single Source of Truth**).

## Status

**Client implementiert (Version 17 that's used in Quick Start 17.01.2026)**: Serverauswahl → Lobby → Spiel → Ergebnis, WebSocket-Client, optionaler KI-Modus.
**Server-Discovery**: Fallback/Mock aktiv, wenn Discovery-API nicht erreichbar.

## Features

- JavaFX Desktop GUI (Screen-Flow: Serverauswahl → Lobby → Spiel → Ergebnis)
- Server Discovery (mit Mock-Fallback)
- WebSocket Communication (`/game`, JSON Messages)
- Interactive Game Board
- AI Mode (optional, benötigt OpenAI API Key)

## Setup

### Prerequisites
- JDK 17+
- Maven 3.6+ (Client-Repo **hat keinen Maven Wrapper**)

### Client starten

```bash
cd labyrinth-client
mvn -Djavafx.platform=mac javafx:run     # macOS Intel
mvn -Djavafx.platform=mac-aarch64 javafx:run  # macOS Apple Silicon (M1/M2/M3)
mvn -Djavafx.platform=linux javafx:run   # Linux
mvn -Djavafx.platform=win javafx:run     # Windows
```

### Verbindung herstellen
1. Serverauswahl-Screen öffnet sich (Discovery lädt verfügbare Server automatisch)
2. Server auswählen + Username eingeben
3. Lobby-Screen: auf weitere Spieler warten
4. Spiel starten (erster Spieler ist Admin und wählt u. a. Boardgröße & Schatzkarten/Spieler)

**Server-Discovery URL** (vom Client verwendet):
- `https://mgmt.dvl.spalx.dev/servers`

Falls Discovery nicht funktioniert:
- WebSocket-URL manuell eingeben: `ws://localhost:<PORT>/game`
  (`<PORT>` steht in den Server-Logs, typischerweise dynamisch 8397–8405, oder fix via `server.port`)

## AI Mode (optional)

1. `ENV_TEMPLATE` kopieren:
```bash
cd labyrinth-client
cp ENV_TEMPLATE .env
```

2. `.env` befüllen:
```env
OPENAI_API_KEY=sk-proj-...
```

Der Client liest `.env` automatisch, falls `OPENAI_API_KEY` nicht ohnehin als Environment-Variable gesetzt ist.

## Protokoll (Kurzüberblick)

### WebSocket Endpoint
- `ws://localhost:<PORT>/game`

## Build & Test

### Compile
```bash
mvn clean compile
```

### Tests
```bash
mvn test
```

## Packaging

Aktuell wird der Client primär über `mvn javafx:run` gestartet.
`mvn package` erzeugt zwar ein JAR, dieses ist jedoch ohne JavaFX-Runtime-Dependencies nicht direkt ausführbar (Native Packaging via jpackage/GraalVM ist nicht konfiguriert).

## Projektstruktur (Client)
```text
labyrinth-client/
├── .env                  # optional (lokal), enthält OPENAI_API_KEY
├── ENV_TEMPLATE          # Vorlage für .env
├── pom.xml
└── src/
    └── main/
        ├── java/com/uni/labyrinth/
        │   ├── Main.java
        │   ├── domain/
        │   │   ├── commands/     # CONNECT, PUSH_TILE, MOVE_PAWN, ...
        │   │   └── events/       # LOBBY_STATE, GAME_STARTED, ...
        │   ├── enums/
        │   ├── model/            # LobbyState, GameState, PlayerState
        │   ├── screens/          # ServerList, LobbyWaiting, Game, Result
        │   ├── service/          # Discovery/WS/AI/Simulation
        │   └── ui/               # JavaFX Components (Board, Tiles, Dialoge)
        └── resources/
            ├── images/
            └── fonts/
```

## CI/CD
- **CI Pipeline**: Build/Validation bei Push/PR auf `main` und `develop`
- **CD Pipeline**: Läuft bei Push auf `main`

## Codestyle

Siehe [CODESTYLE.md](CODESTYLE.md) für Codestyle Guidelines.

## Team

**Gruppe 3**
- Clemens Siebers
- Rene Stockinger
- Andreas Rofner
- Mario Gottwald
- Simon Raass
- Manuel Kirchebner
- David Strauß

## Related Repositories

- [Server Repository](https://github.com/AIP-Labyrinth-Group3/labyrinth-server)
- [API Specification](https://github.com/thomasklammer/labyrinth-interface)

## License

Educational project for Advanced Integrative Project at MCI Innsbruck.
