# Technical Design: Permission Handling

## Overview

On launch, `MainActivity` checks if `READ_CONTACTS` is already granted using
`ContextCompat.checkSelfPermission()`. If not, it fires a permission launcher
built with `ActivityResultContracts.RequestPermission()`. The result (granted/denied)
controls which view is visible: the `ListView` or the "no permission" `TextView`.
State survives rotation automatically because the check runs in `onCreate`.

## Layout Structure

```
ConstraintLayout (root, id: main)
  ├── TextView  (id: tvNoPermission)  — VISIBLE when denied
  └── ListView  (id: lvContacts)      — VISIBLE when granted
```

Only one is visible at a time. The other is `View.GONE`.

## Data Flow

```
onCreate()
  └─ checkSelfPermission(READ_CONTACTS)
        ├─ GRANTED → hide tvNoPermission, show lvContacts → (Phase 2 loads contacts)
        └─ DENIED  → permissionLauncher.launch(READ_CONTACTS)
                          ├─ User grants → hide tvNoPermission, show lvContacts
                          └─ User denies → show tvNoPermission, hide lvContacts
```

## Key Android APIs

| API | Purpose | Notes |
|---|---|---|
| `ContextCompat.checkSelfPermission()` | Check current permission state | Returns `PERMISSION_GRANTED` or `PERMISSION_DENIED` |
| `ActivityResultContracts.RequestPermission()` | Launch system permission dialog | Modern replacement for deprecated `requestPermissions()` |
| `registerForActivityResult()` | Register the launcher (must be in `onCreate`, before `setContentView`) | Returns an `ActivityResultLauncher<String>` |
| `launcher.launch(Manifest.permission.READ_CONTACTS)` | Show the system dialog | Call this when permission is not granted |

## Permission Launcher Pattern

```kotlin
// Register launcher BEFORE setContentView
val permissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    // isGranted = true or false
    // update UI here
}
```

> Your implementation will differ — this is the pattern only.

## State & Rotation

No special handling needed. Because the check runs in `onCreate()`, rotation
naturally re-runs the check. If permission was granted, `checkSelfPermission`
returns `GRANTED` immediately and no dialog appears. No manual state saving required.

## Strings Required (all in strings.xml)

| Key | Value |
|---|---|
| `no_permission_message` | "Contact permission is not granted" (or similar) |

The `app_name` key already exists.

## What NOT to Do

- ❌ Do NOT use `requestPermissions()` — deprecated
- ❌ Do NOT use any third-party permission library (EasyPermissions etc.)
- ❌ Do NOT hardcode any string in Kotlin or XML
- ❌ Do NOT load contacts inside the permission callback (that's Phase 2)
