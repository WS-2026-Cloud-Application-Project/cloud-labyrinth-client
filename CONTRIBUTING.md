# Mitarbeit am Labyrinth-Client

Für dieses Repo gelten die Qualitätsrichtlinien und das GitHub-Konzept aus dem Server-Repo:

- **[QUALITY.md](https://github.com/WS-2026-Cloud-Application-Project/cloud-labyrinth-server/blob/dev/QUALITY.md)** – Git-Workflow, Definition of Done, Reviews
- **[GitHub-Konzept](https://github.com/WS-2026-Cloud-Application-Project/cloud-labyrinth-server/blob/dev/docs/github-konzept.md)** – Branches, Versionierung, Releases

Kurzfassung:

- Arbeits-Branch immer von `dev` abzweigen (`feature/…`, `fix/…`, `chore/…`, `docs/…`)
- Commit-Messages und PR-Titel nach Conventional Commits (lokal per Hook geprüft); PR nach `dev`, Titel (`feat: …`, `fix: …`) – bestimmt die Version
- 2 Approvals, Squash-Merge; nach `main` nur Release-PRs aus `dev`

Einrichtung (einmalig pro Person):

```bash
pip install pre-commit
pre-commit install --hook-type pre-commit --hook-type commit-msg
pre-commit run --all-files   # erster Testlauf
```

Architekturentscheidungen (ADRs), Protokolle und Nachweise liegen im Server-Repo unter `docs/`.
