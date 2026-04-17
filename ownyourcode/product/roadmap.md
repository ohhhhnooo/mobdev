# Project Roadmap

## Current Status

Fresh project. Only default `MainActivity.kt` exists.

---

## Phase 1: Permission Handling

Priority: HIGH — nothing else works without this

- [x] Add `READ_CONTACTS` permission to `AndroidManifest.xml`
- [x] Set up `ActivityResultContracts.RequestPermission()` launcher in `MainActivity`
- [x] On launch: check if permission is already granted
- [x] If not granted: request it
- [x] If denied: show a "no permission" message + button to re-request
- [x] If granted: proceed to load contacts

---

## Phase 2: Contact List

Priority: HIGH

- [ ] Add `ListView` to `activity_main.xml`
- [ ] Query contacts using `ContentResolver` + `ContactsContract`
- [ ] Store contact names (and numbers) in a list — load ONCE, not on every rotation
- [ ] Bind the list to `ListView` using `ArrayAdapter`
- [ ] Handle the case where the device has zero contacts

---

## Phase 3: Contact Details + Polish

Priority: MEDIUM — needed for full grade

- [ ] Set click listener on `ListView` items
- [ ] On tap: show contact name + phone number in an `AlertDialog`
- [ ] Move ALL strings to `res/values/strings.xml`
- [ ] Apply window insets so UI doesn't overlap status/nav bars
- [ ] Test: rotate screen → list should NOT reload
- [ ] Test: deny permission → should show "no permission", not crash
- [ ] Test: grant permission → list appears correctly
