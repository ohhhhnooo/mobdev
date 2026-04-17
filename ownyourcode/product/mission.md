# Project Mission

## The Problem

A university lab requires a working Android app that reads the device's contacts
and displays them with proper permission handling — without crashing, hardcoded strings,
or UI overlapping the system bars.

## Who Is This For?

University grader. This is a lab submission.

## Definition of Done

- [ ] App requests READ_CONTACTS permission on first launch
- [ ] If permission denied: show "no permission" message (not an empty list)
- [ ] If permission granted: show a scrollable list of contact names
- [ ] Tapping a contact shows additional info (phone number, etc.)
- [ ] App does not crash under any flow
- [ ] All strings are in `strings.xml` (no hardcoded text)
- [ ] No repeated list loading on screen rotation
- [ ] No `LazyColumn` or `RecyclerView` used (professor's requirement — use `ListView`)
- [ ] UI does not overlap status bar or navigation bar
- [ ] Package is NOT `com.example.*` ✓ (`dontdoitno.phonecontactlist`)
