# Technical Design: Contact List

## Overview

When permission is granted, `loadContacts()` queries the device contacts via
`ContentResolver` and fills two parallel lists: `contactNames` and `contactNumbers`.
An `ArrayAdapter` binds `contactNames` to the `ListView`. Both lists are saved
in `onSaveInstanceState` so rotation restores them instead of re-querying.

## Data Structure

Two parallel `ArrayList<String>` fields at the class level:

```
contactNames:   ["Alice", "Bob", "Carol"]
contactNumbers: ["+1234", "+5678", "+9012"]
```

`contactNames[i]` and `contactNumbers[i]` always refer to the same contact.
This pairing is needed in Phase 3 to show details when a name is tapped.

## Data Flow

```
onCreate(savedInstanceState)
  ├─ savedInstanceState != null?
  │     YES → restore contactNames + contactNumbers from bundle
  │           → rebind ArrayAdapter (no re-query)
  │     NO  → (lists start empty)
  │
  └─ checkSelfPermission → GRANTED?
        YES → loadContacts() only if list is empty
        NO  → show no-permission UI (Phase 1 handles this)

loadContacts()
  └─ contentResolver.query(ContactsContract.Phone.CONTENT_URI)
       └─ cursor iterates → fills contactNames + contactNumbers
            └─ ArrayAdapter notified → ListView updates
```

## Key Android APIs

| API | Purpose |
|---|---|
| `contentResolver.query(...)` | Opens a cursor over the contacts database |
| `ContactsContract.CommonDataKinds.Phone.CONTENT_URI` | The URI for contacts with phone numbers |
| `ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME` | Column name for the contact's display name |
| `ContactsContract.CommonDataKinds.Phone.NUMBER` | Column name for the phone number |
| `cursor.getColumnIndexOrThrow(columnName)` | Gets the column index safely (throws if missing) |
| `ArrayAdapter(context, android.R.layout.simple_list_item_1, list)` | Built-in single-line list adapter |
| `onSaveInstanceState(outState)` | Save data before rotation destroys the Activity |
| `savedInstanceState?.getStringArrayList(key)` | Restore data after rotation |

## Sorting

Pass `ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME` as the `sortOrder`
argument to `contentResolver.query()` — the database sorts for you, no manual sort needed.

## Rotation Prevention Pattern

```kotlin
// Save before rotation
override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    // save both lists here
}

// Restore after rotation — in onCreate
// if savedInstanceState != null → restore and rebind
// if null → fresh start, load from ContentResolver
```

> Your implementation will differ. This is the pattern only.

## What NOT to Do

- ❌ Do NOT call `loadContacts()` unconditionally in `onCreate` — causes reload on rotation
- ❌ Do NOT use `cursor.getColumnIndex()` without checking for -1 — use `getColumnIndexOrThrow`
- ❌ Do NOT forget to `close()` the cursor after iterating
- ❌ Do NOT hardcode any strings
