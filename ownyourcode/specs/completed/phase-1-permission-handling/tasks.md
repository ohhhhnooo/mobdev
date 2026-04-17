# Implementation Tasks: Phase 1 — Permission Handling

> Work through these IN ORDER. Each step is small and testable.

## Before You Start

- [ ] Read spec.md
- [ ] Read design.md
- [ ] Run `/own:advise` for any pre-work tips

---

## Step 1: Declare the permission in the Manifest

**File:** `app/src/main/AndroidManifest.xml`

Add this line ABOVE the `<application>` tag:
```xml
<uses-permission android:name="android.permission.READ_CONTACTS" />
```

✅ Checkpoint: Build the project — it should compile with no errors.

---

## Step 2: Add strings to strings.xml

**File:** `app/src/main/res/values/strings.xml`

Add a string for the "no permission" message. Choose your own wording.

✅ Checkpoint: `strings.xml` has at least 2 entries (app_name + your new one).

---

## Step 3: Update the layout

**File:** `app/src/main/res/layout/activity_main.xml`

Replace the current content with a layout that has:
- A `TextView` with `id="tvNoPermission"` — shows the no-permission message
- A `ListView` with `id="lvContacts"` — will show contacts later
- Both should be `match_parent` width/height
- Set `TextView` visibility to `gone` initially (or `visible` — you'll control it in code)
- Set `ListView` visibility to `gone` initially

Use `@string/your_key` for the TextView text — no hardcoded strings.

You can use `ConstraintLayout` or switch to a simpler `LinearLayout` — your choice.

✅ Checkpoint: Layout opens in Android Studio preview with no errors.

---

## Step 4: Set up the permission launcher in MainActivity

**File:** `app/src/main/java/dontdoitno/phonecontactlist/MainActivity.kt`

You need to:
1. Register the launcher with `registerForActivityResult` BEFORE `setContentView`
2. Inside the launcher callback: show/hide the two views based on `isGranted`
3. In `onCreate` after `setContentView`: check `ContextCompat.checkSelfPermission()`
   - If GRANTED → show list, hide message
   - If DENIED → launch the permission dialog

Refer to design.md for the API pattern.

✅ Checkpoint: Run the app on emulator.
- First launch → permission dialog appears
- Deny → "no permission" text visible, ListView hidden
- Grant → ListView visible (empty for now), message hidden

---

## Step 5: Test rotation

Run the app, grant permission, then rotate the screen.
- List should stay visible
- No permission dialog should re-appear

Run the app, deny permission, then rotate.
- "No permission" message should stay visible
- App should not crash

✅ Checkpoint: Rotation works correctly in both states.

---

## Completion

- [ ] All acceptance criteria in spec.md pass
- [ ] No hardcoded strings anywhere
- [ ] App does not crash on any flow
- [ ] Run `/own:done` when ready for code review

## Progress

| Step | Status |
|---|---|
| 1 — Manifest permission | ✅ Complete |
| 2 — strings.xml | ✅ Complete |
| 3 — Layout | ✅ Complete |
| 4 — MainActivity logic | ✅ Complete |
| 5 — Rotation test | ✅ Complete |
