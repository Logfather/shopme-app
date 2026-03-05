# ShopMe

ShopMe ist eine moderne Android-App zur Verwaltung von Einkaufslisten mit Fokus auf **Mehrfachlisten, Supermarktzuordnung und einfache Bedienung**.
Die App ist vollständig mit **Jetpack Compose**, **MVVM** und **Firebase Firestore** umgesetzt und verwendet eine **reaktive Architektur mit StateFlow**.

---

# Features

## Mehrere Einkaufslisten

* beliebig viele Einkaufslisten pro Benutzer
* jede Liste kann einem Supermarkt zugeordnet werden

Beispiele:

* Edeka Einkauf
* Rewe Einkauf
* dm Einkauf

---

## Multi-Store Listen-Erstellung

ShopMe unterstützt einen **Multi-Store Setup Flow**.

Der Nutzer kann mehrere Supermärkte auswählen und für jeden automatisch eine Liste erstellen.

Beispiel:

```
Edeka
Rewe
dm
```

→ erzeugt automatisch drei Einkaufslisten.

---

## Artikelverwaltung

Artikel besitzen folgende Eigenschaften:

* Name
* Kategorie
* Menge
* Checkstatus
* Versionierung
* Zeitstempel

Beispiel:

```
Milch
Tomaten
Butter
```

Die Artikel werden automatisch nach Kategorien gruppiert.

---

## Swipe Interactions

Listen und Einträge können per **Swipe-Geste** entfernt werden.

* Swipe nach links
* sofortige Entfernung
* keine Bestätigungsdialoge

Die Implementierung basiert auf:

```
SwipeToDismissBox
```

---

## Spracheingabe

ShopMe unterstützt Spracheingabe zum schnellen Hinzufügen von Artikeln.

Beispiel:

```
"Milch"
"Bananen"
"Butter"
```

Die Spracheingabe wird automatisch in Artikelnamen umgewandelt.

---

## Realtime Synchronisation

Die App nutzt **Firebase Firestore Snapshot Listener**, wodurch Änderungen in Echtzeit synchronisiert werden.

Beispiel:

```
Artikel hinzufügen
→ Firestore Update
→ UI aktualisiert sich automatisch
```

---

# Architektur

Die App folgt einer klaren **MVVM Architektur**.

```
UI (Compose)
↓
ViewModel
↓
Repository
↓
Firestore
```

---

## Single Source of Truth

Der komplette UI-State wird im `ShoppingViewModel` verwaltet.

Wichtige StateFlows:

```
screenState
selectedStores
userLists
activeList
groupedItems
```

---

# Screen State Machine

Die Navigation innerhalb der App wird über einen **ScreenState** gesteuert.

```
sealed class ScreenState
```

Zustände:

```
Loading
Normal
MultiSelect
MultiReview
MultiOverview
```

---

## State Flow Diagramm

```
Loading
   │
   ▼
MultiOverview
   │
   ├── Neue Liste → MultiSelect
   │
   └── Liste öffnen → Normal

MultiSelect
   │
   ▼
MultiReview
   │
   ├── createAllLists
   │
   ▼
MultiOverview
```

---

# UI Architektur

Die zentrale UI-Komponente ist:

```
ShopMeApp.kt
```

Diese enthält:

* TopBar
* FloatingActionButton
* Hauptcontent
* BottomSheet für Listenwechsel

---

## TopBar

Die TopBar zeigt:

* Store Logo
* Listenname
* Artikelanzahl

Beispiel:

```
[Edeka Logo]  Edeka Einkauf
              12 Artikel
```

---

## Floating Action Button

Der FAB erstellt neue Einkaufslisten.

```
+ Neue Liste
```

Der Button wird nur angezeigt, wenn bereits Listen existieren.

---

# MultiOverview Screen

Dieser Screen zeigt alle vorhandenen Einkaufslisten.

Features:

* Swipe zum Löschen
* Bearbeiten einer Liste
* Active List Highlight
* Empty State bei fehlenden Listen

---

## Empty State

Wenn keine Listen existieren:

```
🛒

Noch keine Listen vorhanden

[ Neue Liste erstellen ]
```

---

# MultiReview Screen

Dieser Screen zeigt die ausgewählten Supermärkte vor dem Erstellen der Listen.

Beispiel:

```
Edeka
Rewe
dm
```

Swipe nach links entfernt einen Store aus der Auswahl.

---

# Datenmodell

Firestore Collections:

```
lists
```

Listenstruktur:

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

## Artikel

```
lists/{listId}/items/{itemId}
```

Beispiel:

```
{
 name: "Milch",
 quantity: 1,
 category: "Milchprodukte",
 isChecked: false,
 version: 0,
 deletedAt: null,
 createdAt,
 updatedAt
}
```

---

# Firestore Security Rules

Die App verwendet ein **Owner-only Modell**.

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

# Technologiestack

* Kotlin
* Jetpack Compose
* Material 3
* StateFlow
* Coroutines
* Firebase Firestore
* Firebase Authentication

---

# Projektstruktur

```
de.shopme

data
 └ FirestoreShoppingRepository

domain
 └ models

presentation
 └ ShoppingViewModel

ui
 ├ ShopMeApp
 ├ components
 └ screens
```

---

# Entwicklungsstatus

Der aktuelle Stand der App umfasst:

* Multi-Store Einkaufslisten
* Reaktive UI
* Firestore Synchronisation
* Swipe Interaktionen
* Spracheingabe
* moderne Compose UI

Die Architektur ist vorbereitet für zukünftige Erweiterungen wie:

* Shared Lists
* Offline Sync
* Realtime Collaboration
* Familienlisten
* KI-basierte Einkaufsvorschläge

---

# Lizenz

Dieses Projekt ist aktuell in aktiver Entwicklung und noch nicht öffentlich veröffentlicht.
