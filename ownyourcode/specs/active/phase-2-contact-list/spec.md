# Feature: Contact List

> Phase 2 of 3.

## User Story

As a user with contacts permission granted, I want to see a list of contact names so that I can find who I'm looking for.

## Acceptance Criteria

- [ ] Contact names are displayed in the `ListView` after permission is granted
- [ ] The list is sorted alphabetically
- [ ] If the device has zero contacts, the list is empty but the app does not crash
- [ ] Rotating the screen does NOT reload contacts from the device (no repeated loading penalty)
- [ ] Phone numbers are also stored alongside names (needed for Phase 3 details)

## Edge Cases

| Scenario | Expected Behavior |
|---|---|
| Device has no contacts | Empty ListView, no crash |
| Contact has no phone number | Skip it (only show contacts with numbers) |
| Contact has multiple numbers | Show the contact once (any one number) |
| Screen rotated after loading | Same list shown, no re-query |
| Permission granted mid-session (via button) | List loads correctly |

## Out of Scope (Phase 2)

- Showing contact details (Phase 3)
- Search/filter
- Contact photos

## Dependencies

- [x] Phase 1 complete — permission flow working
- [x] `ListView` with `id="lvContacts"` exists in layout
