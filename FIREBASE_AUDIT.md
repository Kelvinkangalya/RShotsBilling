# Firebase Implementation Audit

## 1. Firebase libraries currently installed

| Library Name | Version | Source (SDK) | Platform(s) |
| :--- | :--- | :--- | :--- |
| `firebase-auth` | `2.5.0` | GitLive Firebase Kotlin SDK | `commonMain` |
| `firebase-firestore` | `2.5.0` | GitLive Firebase Kotlin SDK | `commonMain` |
| `firebase-auth` | `33.10.0` (BOM) | Native Android Firebase SDK | `app` module |
| `firebase-firestore` | `33.10.0` (BOM) | Native Android Firebase SDK | `app` module |
| `firebase` (JS) | `11.1.0` | Official Firebase JS SDK | `jsMain` |

## 2. Which platform uses each library

- **Common (Shared)**: Uses GitLive Firebase Kotlin SDK (`dev.gitlive:firebase-auth` and `dev.gitlive:firebase-firestore`).
- **Android (`app` module)**: Uses Native Android Firebase SDK via Google Services plugin and BOM.
- **Android (`composeApp` module)**: Uses GitLive Firebase (which wraps Native Android SDK).
- **Desktop (JVM)**: Uses GitLive Firebase, but currently hitting a STUB implementation that throws `IllegalStateException`.
- **Web (JS)**: Uses official Firebase JS SDK via npm, wrapped by GitLive.

## 3. How Android initializes Firebase

Android initialization is currently redundant and fragmented:
1.  **Native Plugin**: The `app` module uses `com.google.gms.google-services` plugin which auto-initializes Firebase using `google-services.json`.
2.  **Manual Initialization**: In `MainApplication.onCreate()`, `initializeFirebase()` is called.
3.  **Implementation**: `shared/src/androidMain/.../FirebaseConfig.kt`'s `initializeFirebase()` attempts to build `FirebaseOptions` manually and calls `FirebaseApp.initializeApp(context, options)` AND `Firebase.initialize(context)`.

## 4. How Desktop initializes Firebase

Desktop initialization is currently **broken/incomplete**:
- `Main.kt` has `initializeFirebase()` commented out.
- `App.kt` (common) calls `initializeFirebase()` in a `LaunchedEffect`.
- `shared/src/jvmMain/.../FirebaseConfig.kt` has a STUB `initializeFirebase()` that just sets a boolean flag and logs a message.
- `PlatformFirebase.firestore` in `jvmMain` throws an `IllegalStateException`.

## 5. Whether Desktop initialization is correct

**No.** Desktop initialization is not correct because:
- It uses a stub that does not actually connect to Firebase.
- GitLive Firebase on JVM requires the Firebase Java Admin SDK or a similar bridge to work, which is not currently configured.
- The `IllegalStateException` in `PlatformFirebase.firestore` prevents any Firestore access on Desktop.

## 6. Whether the shared module incorrectly initializes Firebase

**Yes.**
- The shared module's `commonMain` should not be responsible for the *mechanism* of initialization, but it is calling `initializeFirebase()`.
- The `androidMain` implementation is trying to manually initialize Firebase even when the `google-services` plugin might already be doing it, leading to potential "already initialized" logs or conflicts.
- Redundant `Firebase.initialize(context)` (GitLive) and `FirebaseApp.initializeApp` (Native) calls.

## 7. Whether repositories access Firestore before initialization

**Potentially.**
- `CompanyRepository` and others use `FirestoreManager.db` via `by lazy`.
- `FirestoreManager.db` calls `provideDb()`, which accesses `PlatformFirebase.firestore`.
- If a repository is instantiated and its `db` property is accessed before the `LaunchedEffect` in `App.kt` finishes `initializeFirebase()`, it might fail (especially on Desktop where it throws).
- On Android, `MainApplication` initializes it early, so it's safer there, but still not architecturally sound.

## 8. Recommended architecture

1.  **Unified Initialization**: Move Firebase initialization to the platform-specific entry points (Application for Android, Main for Desktop).
2.  **Desktop Support**: Replace the JVM stub with an actual implementation. For Desktop, GitLive Firebase supports JVM via the Firebase Admin SDK or by using the REST API internally if configured correctly. However, GitLive's JVM target often expects a service account for Admin-like access, or it might be intended to work with the Firebase JS SDK if using Compose HTML. For a Desktop app, the Firebase Java SDK (Admin) is the standard, but GitLive's multiplatform support for JVM is sometimes limited.
3.  **Dependency Injection**: Use a DI framework (like Koin) to provide the `FirebaseFirestore` instance. This ensures that the instance is not accessed before it's ready and allows for better mocking/testing.
4.  **Remove Redundancy**: If using `google-services.json` on Android, don't manually build `FirebaseOptions` unless absolutely necessary (e.g., dynamic multi-project support).
5.  **State Management**: `App.kt` should observe a `FirebaseState` (e.g., `Uninitialized`, `Initializing`, `Ready`, `Error`) rather than just checking `isFirebaseReady()` in a loop.
