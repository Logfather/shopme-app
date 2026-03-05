🚀 **Projekt-Seed: ShopMe App – Architekturstand März 2026 (UX & State stabilisiert)**

## Projektüberblick

**ShopMe** ist eine Android-App zum Verwalten mehrerer Einkaufslisten pro Benutzer.
Technologie-Stack:

* **Android**
* **Jetpack Compose**
* **MVVM**
* **Kotlin Coroutines / StateFlow**
* **Firebase Firestore**
* **Owner-only Security Rules**

Ziel der App:

* mehrere Einkaufslisten pro Nutzer
* Listen sind optional einem Supermarkt zugeordnet (Edeka, Rewe, dm usw.)
* Artikel werden kategorisiert angezeigt
* Spracheingabe möglich
* Multi-Store Setup für parallele Listen

---

# Architekturprinzipien

### 1️⃣ MVVM

```
UI (Compose)
   ↓
ViewModel (StateFlow)
   ↓
Repository
   ↓
Firestore
```

---

### 2️⃣ Single Source of Truth

Der UI-State wird vollständig im **ShoppingViewModel** verwaltet.

```
ShoppingViewModel
```

enthält u.a.:

```
screenState
selectedStores
userLists
activeList
groupedItems
```

---

### 3️⃣ Reactive UI

Die UI nutzt ausschließlich:

```
collectAsStateWithLifecycle()
```

für:

```
activeList
screenState
selectedStores
userLists
groupedItems
```

---

# Screen State Machine

```
sealed class ScreenState {
    object Loading
    object Normal
    object MultiSelect
    object MultiReview
    object MultiOverview
}
```

---

# State Transition Diagram

```
Loading
   │
   ▼
MultiOverview (EmptyState möglich)

MultiOverview
   │
   ├── Neue Liste → MultiSelect
   │
   └── Liste öffnen → Normal

MultiSelect
   │
   ├── confirmStoreSelection → MultiReview
   └── cancel → MultiOverview

MultiReview
   │
   ├── createAllLists → MultiOverview
   └── addMoreStores → MultiSelect

Normal
   │
   └── startMultiStoreCreation → MultiSelect
```

---

# UX Flow (Onboarding)

### App Start

```
Loading
   ↓
MultiOverview
```

Wenn **keine Listen existieren**:

```
Empty State Card
```

UI:

```
🛒
Noch keine Listen vorhanden

[ Neue Liste erstellen ]
```

Der Button öffnet:

```
MultiSelect
```

Dialog:

```
Wo willst du heute einkaufen?
```

---

# MultiStore Setup Flow

```
MultiSelect
   ↓
MultiReview
   ↓
createAllLists()
   ↓
MultiOverview
```

Beispiel:

```
Edeka
Rewe
dm
```

→ erstellt 3 Listen.

---

# Firestore Datenmodell

Collection:

```
lists
```

Dokument:

```
lists/{listId}
```

Beispiel:

```
{
 name: "Rewe Einkauf",
 ownerId: uid,
 storeTypes: ["REWE"],
 createdAt,
 updatedAt
}
```

---

Subcollection:

```
lists/{listId}/items
```

Item:

```
{
 name
 quantity
 category
 isChecked
 deletedAt
 version
 createdAt
 updatedAt
}
```

---

# Firestore Security Rules

Owner-only Modell:

```
match /lists/{listId} {

  allow create:
    if request.auth != null
    && request.resource.data.ownerId == request.auth.uid;

  allow read, update, delete:
    if request.auth != null
    && resource.data.ownerId == request.auth.uid;

  match /items/{itemId} {

    allow read, create, update, delete:
      if request.auth != null
      && get(/databases/$(database)/documents/lists/$(listId))
         .data.ownerId == request.auth.uid;
  }
}
```

---

# Repository Architektur

```
FirestoreShoppingRepository
```

verwaltet:

```
_currentListId
observeListsForUser()
observeItems()
createList()
deleteList()
addItem()
updateItem()
softDelete()
clearAll()
createInvite()
```

Reactive Streams:

```
Flow<List<ShoppingListEntity>>
Flow<List<ShoppingItemEntity>>
```

---

# Active List Mechanismus

```
_currentListId : StateFlow<String?>
```

ActiveList wird berechnet durch:

```
combine(
 repository.currentListId,
 userLists
)
```

---

# UI Architektur

Zentrale UI:

```
ShopMeApp.kt
```

Scaffold enthält:

```
TopBar
FloatingActionButton
Content
```

---

# TopBar

Zeigt:

```
Store Logo
Listenname
Artikelanzahl
Dropdown zum Wechseln der Liste
```

Listenwechsel erfolgt über:

```
ModalBottomSheet
```

---

# MultiOverview Screen

Zeigt alle Listen.

Features:

```
SwipeToDismissBox → deleteList()
Edit → editList()
ActiveList Highlight
```

Empty State wenn keine Listen existieren.

---

# MultiReview Screen

Zeigt ausgewählte Stores:

```
Edeka
Rewe
dm
```

Swipe nach links:

```
toggleStore(store)
```

entfernt Store aus Auswahl.

Buttons:

```
Alle Listen erstellen
Auswahl ergänzen
```

---

# Swipe Verhalten

Verwendet:

```
SwipeToDismissBox
```

Für MultiReview:

```
toggleStore(store)
```

Für MultiOverview:

```
deleteList(list)
```

Stable key wurde ergänzt:

```
key(store.name)
```

um Compose Recomposition Fehler zu vermeiden.

---

# Visuelles Design

Theme basiert auf:

```
Material3
```

Brand Color:

```
BrandOlive
```

UI Elemente:

```
RoundedCornerShape(16–24dp)
ExtendedFloatingActionButton
Card Layout
Store Logos
Background Image + Overlay
```

---

# Aktueller Entwicklungsstatus

Stabil implementiert:

* MultiStore Setup
* Listenverwaltung
* Firestore Synchronisation
* Swipe Interaction
* Empty State UX
* State Machine Navigation
* ActiveList Management

---

# Offene / zukünftige Erweiterungen

Mögliche nächste Schritte:

1️⃣ UI Animationen (AnimatedContent / Motion)

2️⃣ Store Logo System erweitern

3️⃣ Pagination / Lazy loading

4️⃣ Offline Support optimieren

5️⃣ List Sharing erweitern

6️⃣ Performance Optimierung (LazyColumn Keys)

7️⃣ Verbesserte TopBar UX

---

# Erwartung an den nächsten Chat

Der Chat soll:

* Änderungen **schrittweise** durchführen
* **vor Codeänderungen nach aktuellem Code fragen**
* Architektur nicht ohne Rücksprache refactoren
* Fokus zuerst auf **UX Stabilität**, danach **Architekturverbesserung**

---
