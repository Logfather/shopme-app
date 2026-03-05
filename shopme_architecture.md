# ShopMe – Architektur v2 (Skalierbare Architektur für Shared Lists, Offline Sync und Realtime Collaboration)

## Überblick

Diese Architektur beschreibt die **Weiterentwicklung der aktuellen ShopMe-App**, um folgende Funktionen skalierbar zu unterstützen:

* Shared Shopping Lists
* Offline-Nutzung
* Realtime Collaboration
* Konfliktauflösung bei parallelen Änderungen
* zukünftige Erweiterungen (Analytics, Smart Suggestions, Family Accounts)

Die Architektur baut **evolutionär auf der bestehenden MVVM-Struktur** auf und ersetzt sie nicht.

---

# 1. Architekturprinzip

Aktueller Stand:

```
UI (Compose)
↓
ViewModel
↓
Repository
↓
Firestore
```

Diese Struktur funktioniert gut für:

* Single-User Nutzung
* einfache CRUD-Operationen

Für Offline-Support und Kollaboration wird eine **lokale Datenquelle mit Synchronisationsmechanismus** ergänzt.

Neue Architektur:

```
UI (Compose)
        │
        ▼
ViewModel (StateFlow)
        │
        ▼
Repository
        │
 ┌──────┴─────────┐
 │                │
 ▼                ▼
Local DB      Remote Source
(Room)        (Firestore)
        │
        ▼
     Sync Engine
```

---

# 2. Gesamtarchitektur

```
              UI (Jetpack Compose)
                      │
                      │ collectAsState
                      ▼
              ShoppingViewModel
                      │
                      ▼
               Domain Layer
           (UseCases optional)
                      │
                      ▼
                 Repository
                      │
        ┌─────────────┴─────────────┐
        │                           │
        ▼                           ▼
     Local Database             Remote API
        Room                    Firestore
        │                           │
        └─────────────┬─────────────┘
                      ▼
                  Sync Engine
```

---

# 3. Neue Komponenten

## Local Database

Empfohlene Technologie:

```
Room Database
```

Vorteile:

* Offline-Verfügbarkeit
* schneller Zugriff für UI
* stabile Datenbasis für Synchronisation
* Konfliktlösung möglich

### Entities

```
ListEntity
ItemEntity
MemberEntity
InviteEntity
```

Diese spiegeln das Firestore-Datenmodell lokal wider.

---

## Sync Engine

Die Sync Engine synchronisiert Daten zwischen **Local Database** und **Firestore**.

Synchronisationsrichtung:

```
Firestore → Local DB
Local DB → Firestore
```

### Schreibfluss (Offline First)

```
User Action
↓
Local DB Update
↓
UI sofort aktualisiert
↓
Background Sync → Firestore
```

### Lesefluss

```
Firestore Snapshot Listener
↓
Local DB Update
↓
UI recomposition
```

Dieses Muster wird als **Offline-First Architecture** bezeichnet.

---

# 4. Firestore Datenmodell (Shared Lists)

Aktuell existiert:

```
lists
```

Erweitertes Modell:

```
lists
lists/{listId}
lists/{listId}/items
lists/{listId}/members
lists/{listId}/invites
```

---

## List Dokument

```
lists/{listId}

{
 name: "Rewe Einkauf",
 ownerId: uid,
 storeTypes: ["REWE"],
 createdAt: timestamp,
 updatedAt: timestamp
}
```

---

## Items

```
lists/{listId}/items/{itemId}

{
 name: "Milch",
 quantity: 1,
 category: "Milchprodukte",
 isChecked: false,
 version: 0,
 deletedAt: null,
 createdAt: timestamp,
 updatedAt: timestamp
}
```

---

## Mitglieder

```
lists/{listId}/members/{userId}

{
 role: "owner" | "editor" | "viewer",
 joinedAt: timestamp
}
```

---

## Einladungen

```
lists/{listId}/invites/{inviteId}

{
 createdBy: uid,
 createdAt: timestamp,
 consumed: false,
 role: "editor"
}
```

---

# 5. Realtime Collaboration

Firestore bietet native Realtime-Updates über:

```
SnapshotListener
```

Ablauf:

```
User A → Item hinzufügen
↓
Firestore Update
↓
SnapshotListener
↓
User B erhält Update
↓
UI wird automatisch aktualisiert
```

Damit können mehrere Nutzer gleichzeitig an einer Liste arbeiten.

---

# 6. Konfliktlösung

Bei parallelen Änderungen muss entschieden werden, welche Änderung gültig ist.

Empfohlene Strategie:

```
Last Write Wins
```

Unterstützt durch Felder:

```
updatedAt
version
```

---

## Item Versionierung

```
Item
{
 name
 quantity
 version
 updatedAt
}
```

Bei Updates:

```
version = version + 1
updatedAt = serverTimestamp
```

Der aktuellste Stand gewinnt.

---

# 7. Synchronisationsstrategie

### Schreiboperation

```
User Action
↓
Local DB Update
↓
UI sofort aktualisiert
↓
Sync Worker sendet Änderungen zu Firestore
```

### Leseoperation

```
Firestore Listener
↓
Local DB Update
↓
ViewModel Flow Update
↓
UI recomposition
```

---

# 8. Repository Struktur (v2)

Das Repository wird intern aufgeteilt:

```
ShoppingRepository
```

besteht aus:

```
LocalDataSource
RemoteDataSource
SyncManager
```

Beispielstruktur:

```
ShoppingRepository
 ├── LocalListDataSource
 ├── LocalItemDataSource
 ├── RemoteListDataSource
 ├── RemoteItemDataSource
 └── SyncManager
```

---

# 9. Vorteile dieser Architektur

| Feature         | Ergebnis |
| --------------- | -------- |
| Offline Nutzung | ✔        |
| Realtime Sync   | ✔        |
| Shared Lists    | ✔        |
| Skalierbarkeit  | ✔        |
| Konfliktlösung  | ✔        |
| Performante UI  | ✔        |

---

# 10. Zukünftige Features

Diese Architektur ermöglicht einfache Erweiterungen:

### Shared Shopping Lists

```
Nutzer zur Liste einladen
```

### Familienlisten

```
Mehrere Nutzer bearbeiten Liste gleichzeitig
```

### Smart Suggestions

```
KI-basierte Einkaufsvorschläge
```

### Analytics

```
häufig gekaufte Produkte erkennen
```

---

# 11. Empfohlene Migrationsstrategie

Die Architektur kann schrittweise eingeführt werden.

### Phase 1

```
Aktuelle Firestore Architektur stabilisieren
```

### Phase 2

```
Room Local Database hinzufügen
```

### Phase 3

```
Sync Engine implementieren
```

### Phase 4

```
Shared Lists und Membership System
```

---

# 12. Aktuelle Bewertung der ShopMe Architektur

Die bestehende App besitzt bereits eine solide Grundlage:

```
MVVM
Reactive StateFlow
Firestore Realtime
Compose UI
```

Diese Struktur eignet sich sehr gut als Basis für die **ShopMe Architektur v2**.

---

# Fazit

Mit dieser Architektur wird ShopMe zu einer:

```
Offline-first
Realtime
Collaborative
skalierbaren Einkaufslisten-App
```

vergleichbar mit modernen Produktivitäts-Apps wie:

* Todoist
* AnyList
* Notion
* Things
