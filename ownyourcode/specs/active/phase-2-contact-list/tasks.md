# Implementation Tasks: Phase 2 — Contact List

> Work through these IN ORDER.

---

## Step 1: Add class-level fields to MainActivity

**File:** `MainActivity.kt`

Add two `ArrayList<String>` fields inside the class, before `permissionLauncher`:

- one for contact names
- one for contact numbers

These must be class-level (not inside a function) so they survive between method calls.

✅ Checkpoint: The fields exist and compile.

---

## Step 2: Write the `loadContacts()` function

**File:** `MainActivity.kt`

Create a `private fun loadContacts()` that:
1. Queries `contentResolver` using `ContactsContract.CommonDataKinds.Phone.CONTENT_URI`
2. Requests two columns: `DISPLAY_NAME` and `NUMBER`
3. Sorts by `DISPLAY_NAME`
4. Iterates the cursor and adds each name/number to your two lists
5. Closes the cursor

Refer to design.md for the API table.

✅ Checkpoint: Function exists and compiles (can't test yet).

---

## Step 3: Bind the ListView with ArrayAdapter

**File:** `MainActivity.kt`

Create a `private fun bindList()` that:
1. Creates an `ArrayAdapter<String>` using `contactNames` and `android.R.layout.simple_list_item_1`
2. Sets it on `findViewById<ListView>(R.id.lvContacts)`

✅ Checkpoint: Function exists and compiles.

---

## Step 4: Call load and bind from `showContacts()`

**File:** `MainActivity.kt`

Update `showContacts()` to:
1. Call `loadContacts()` — fills the lists
2. Call `bindList()` — connects data to ListView
3. Then show the ListView (already there from Phase 1)

✅ Checkpoint: Run the app, grant permission → list shows contact names.

---

## Step 5: Prevent reload on rotation

**File:** `MainActivity.kt`

Override `onSaveInstanceState(outState: Bundle)` to save both lists into the bundle.

Then in `onCreate`, before the permission check:
- If `savedInstanceState != null` → restore both lists from the bundle, call `bindList()`
- If `savedInstanceState == null` → do nothing (fresh start)

Also update the permission check in `onCreate`: only call `showContacts()` if the lists are empty (don't reload if already restored).

✅ Checkpoint: Grant permission → rotate screen → list stays, no reload.

---

## Step 6: Add contacts to the emulator

If your emulator has no contacts, the list will be empty.
Open the Contacts app on the emulator and add 2-3 test contacts with phone numbers.

✅ Checkpoint: Names appear in the list after permission is granted.

---

## Completion

- [ ] Contact names appear in ListView after permission granted
- [ ] Rotation does not reload the list
- [ ] No hardcoded strings added
- [ ] Cursor is closed after use
- [ ] Run `/own:done` when ready

## Progress

| Step | Status |
|---|---|
| 1 — Class-level fields | Not Started |
| 2 — loadContacts() | Not Started |
| 3 — bindList() | Not Started |
| 4 — showContacts() wired up | Not Started |
| 5 — Rotation prevention | Not Started |
| 6 — Test data on emulator | Not Started |
