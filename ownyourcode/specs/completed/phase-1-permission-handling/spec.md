---
status: completed
completed_at: 2026-04-17
---

# Feature: Permission Handling

> Phase 1 of 3. This is the foundation — nothing else works without it.

## User Story

As a user, I want the app to request access to my contacts so that it can display them, and clearly tell me when permission is missing instead of showing nothing.

## Acceptance Criteria

When these all pass, Phase 1 is DONE:

- [ ] App requests `READ_CONTACTS` permission on first launch
- [ ] If permission is already granted on launch: proceed to contacts (no dialog shown)
- [ ] If permission is denied: a message is visible explaining there is no permission (not an empty screen)
- [ ] If permission is permanently denied (user ticked "don't ask again"): message still shows, app does not crash
- [ ] App does not crash on any permission flow
- [ ] No hardcoded strings — all text is in `strings.xml`

## Edge Cases

| Scenario | Expected Behavior |
|---|---|
| User opens app for the first time | Permission dialog appears |
| User grants permission | Contacts list shown (Phase 2) |
| User denies permission | "No permission" TextView is shown, ListView is hidden |
| User denies permanently | Same as deny — show message, no crash |
| User rotates screen after deny | "No permission" message still shows, no re-request |
| User rotates screen after grant | Contact list still shows, no reload |

## Out of Scope (Phase 1)

- Actually loading contacts (Phase 2)
- Contact detail screen (Phase 3)
- Sending the user to Settings to re-enable permission manually

## Files to Touch

| File | Change |
|---|---|
| `AndroidManifest.xml` | Add `READ_CONTACTS` permission |
| `app/build.gradle.kts` | No change needed |
| `res/layout/activity_main.xml` | Replace placeholder with `ListView` + "no permission" `TextView` |
| `res/values/strings.xml` | Add all string resources |
| `MainActivity.kt` | Permission launcher + check + show/hide logic |
