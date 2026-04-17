# Technology Stack

## Stack

| Layer       | Technology                   | Version       | Source       | Purpose                              |
|-------------|------------------------------|---------------|--------------|--------------------------------------|
| Language    | Kotlin                       | —             | package.json | Primary language                     |
| UI          | XML Layouts                  | —             | project      | Traditional Android Views            |
| List        | ListView + ArrayAdapter      | built-in      | Android SDK  | Contact list (avoids RecyclerView penalty) |
| Permissions | ActivityResultContracts      | built-in      | AndroidX     | Modern permission request API        |
| Data        | ContentResolver + ContactsContract | built-in | Android SDK | Read device contacts                 |
| Details     | AlertDialog                  | built-in      | AndroidX     | Show contact details on tap          |
| Insets      | WindowInsetsCompat           | built-in      | AndroidX     | Prevent UI overlapping system bars   |

## Key Files

| File                          | Purpose                              |
|-------------------------------|--------------------------------------|
| `AndroidManifest.xml`         | Declare READ_CONTACTS permission     |
| `MainActivity.kt`             | Permission flow + list display       |
| `res/layout/activity_main.xml`| Root layout with ListView            |
| `res/values/strings.xml`      | All user-facing strings              |

## Why These Choices?

- **ListView** instead of RecyclerView: professor penalises RecyclerView (–10%)
- **ActivityResultContracts**: modern replacement for deprecated `onRequestPermissionsResult`
- **AlertDialog** for contact details: simplest approach, no extra Activity needed
- **XML layouts**: consistent with student's only prior experience (calculator app)

## What NOT to Use

- ❌ `RecyclerView` or `LazyColumn` (–10% penalty)
- ❌ Hardcoded strings in code or XML (–10% penalty)
- ❌ `com.example.*` package (mandatory fail)
- ❌ Reloading contacts on every rotation (–10% penalty)
