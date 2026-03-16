# ShopMe Data Flow

ShopMe follows **Unidirectional Data Flow**.

Data always moves in a predictable direction.

---

# UI Event Flow

```mermaid
sequenceDiagram

actor User

User ->> UI: Interaction

UI ->> ViewModel: dispatch(Action)

ViewModel ->> Reducer: reduce(state, action)

Reducer ->> ViewModel: new UiState

ViewModel ->> UI: StateFlow emit

UI ->> UI: Compose recomposition
```

---

# Example: Toggle Store

```
RadioButton Click
   ↓
ToggleStore Action
   ↓
Reducer
   ↓
MultiSelect(selectedStores updated)
   ↓
UI recomposition
```

---

# Side Effect Flow

Side effects are executed after state transitions.

```mermaid
flowchart TD

Action[ShoppingAction] --> Reducer

Reducer --> NewState[New UiState]

NewState --> UI

Action --> SideEffects

SideEffects --> UseCases

UseCases --> Repository

Repository --> Firestore
```

Example:

```
ConfirmStores
 ↓
Reducer → MultiOverview
 ↓
createListUseCase()
 ↓
Firestore
```

---

# Firestore Realtime Flow

```mermaid
flowchart TD

Firestore --> Listener[SnapshotListener]

Listener --> Repository

Repository --> FlowState[Flow<List<ShoppingList>>]

FlowState --> ViewModel

ViewModel --> ViewState

ViewState --> ComposeUI
```

---

# Item Update Flow

```mermaid
flowchart TD

User --> AddItemEvent[ShopEvent.AddItem]

AddItemEvent --> ViewModel

ViewModel --> Repository

Repository --> Firestore

Firestore --> SnapshotListener

SnapshotListener --> Repository

Repository --> ViewModel

ViewModel --> UI
```

---

# Future Offline-First Flow

```mermaid
flowchart TD

UI --> ViewModel

ViewModel --> Repository

Repository --> RoomCache[Room Database]

Repository --> SyncQueue

SyncQueue --> Firestore

RoomCache --> Flow

Flow --> UI
```

Offline-first behavior:

```
UI writes → Room
Room emits → UI updates instantly
SyncQueue → Firestore
```
