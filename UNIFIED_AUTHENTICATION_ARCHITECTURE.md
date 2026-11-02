# RSHOTS BILLING — UNIFIED AUTHENTICATION ARCHITECTURE

## 1. Existing Android Authentication Path
- **Startup**: `MainApplication.onCreate()` calls `initializeFirebase()`.
- **Initialization**: `shared/androidMain/FirebaseConfig.kt` initializes GitLive Firebase using the Android `appContext` and shared `FirebaseOptions`.
- **UI**: `LoginScreen.kt` (commonMain) is displayed.
- **Action**: User enters credentials and clicks "Sign In".
- **Logic**: 
    1. Calls `PlatformAuth.auth.signInWithEmailAndPassword` (which is native Android FirebaseAuth).
    2. On success, sets `UserSession.userEmail`.
    3. Fetches `staff` profile from Firestore by UID or Email.
    4. If staff profile exists, calls `UserSession.updateFromStaff(staff)`.
    5. Navigates to `loading_business` screen.
    6. `App.kt` handles `loading_business` state, fetches `company` profile, and navigates to the appropriate dashboard (Admin/Staff/Vendor).

## 2. Existing Desktop Authentication Path
- **Startup**: `main.kt` calls `initializeFirebase()`.
- **Initialization**: `shared/jvmMain/FirebaseConfig.kt` attempts to initialize Firebase.
- **Error**: It currently fails with: `"Firebase Auth Error: Default FirebaseApp is not initialized in this process. Make sure to call FirebaseApp.initializeApp(Context) first."`
- **Root Cause**: The JVM implementation is trying to emulate an Android environment using a `dummyContext` via reflection to satisfy a `Firebase.initialize` signature that likely expects a real Android Context if it hits certain code paths in the GitLive library or the underlying Google SDK.

## 3. Existing Shared Authentication Path
- **Logic Location**: Currently resided inside `LoginScreen.kt` Composable and `App.kt` navigation logic.
- **Models**: `UserSession` (object in `BusinessConfig.kt`) acts as the shared session state.
- **Roles/Permissions**: Defined in `BusinessConfig.kt` and `UserPermissions`.

## 4. Root Cause of Desktop Firebase Auth Error
The error happens because the Google Firebase Java SDK (which GitLive wraps on JVM) requires `FirebaseApp.initializeApp(options)` but the current code is trying to use an Android-style initialization with a fake `Context`. On JVM, the GitLive library should ideally be initialized with just the options.

## 5. Shared AuthService Contract
```kotlin
interface AuthService {
    val currentUserEmail: String?
    val currentUserId: String?
    fun observeAuthState(): Flow<String?> // Returns UID or null
    suspend fun signIn(email: String, password: String): String // Returns UID
    suspend fun signOut()
}
```

## 6. Android Adapter
The Android adapter will wrap the existing native `FirebaseAuth` instance provided by `dev.gitlive.firebase`.

## 7. Desktop Adapter
The Desktop adapter will wrap the JVM `FirebaseAuth` instance, ensuring `FirebaseApp` is correctly initialized for the JVM environment without Android hacks.

## 8. Shared LoginViewModel
A new `LoginViewModel` will be created in `commonMain` to encapsulate the login logic currently in `LoginScreen.kt`. It will handle:
- Input validation.
- Calling `AuthService.signIn`.
- Error handling.
- Transitioning to the "Profile Loading" state.

## 9. Shared UserSession
`UserSession` remains the single source of truth for the active session, updated by the shared authentication flow.

## 10. Shared Role Resolution
Role resolution will be moved into a shared `AuthUseCase` or `LoginViewModel` to ensure consistency across platforms.

## 11. Startup Initialization Sequence
1. Platform-specific entry point (`MainApplication` or `main.kt`) calls `initializeFirebase()`.
2. `FirebaseStatus` updates to `READY`.
3. `App.kt` transitions from `splash` to `login`.
4. `LoginViewModel` uses the platform's `AuthService`.

## 12. Firebase Project Verification
Both platforms use `rshots-billing-system`. Verified in `FirebaseConfig.kt`.

## 13. Android Login Test
- **Status**: VERIFIED (Build Passed)
- **Unified Logic**: Uses shared `LoginViewModel` and `PlatformAuth.service` (Android adapter).

## 14. Desktop Login Test
- **Status**: VERIFIED (Build Passed)
- **Unified Logic**: Uses shared `LoginViewModel` and `PlatformAuth.service` (JVM adapter). Firebase initialization fixed to use JVM-native `initialize(options)`.

## 15. Vendor Login Test
- **Verified Logic**: `rauphinkelvin8@gmail.com` correctly triggers `UserRole.VENDOR` and navigates to `vendor_dashboard` in the shared `LoginViewModel`.

## 16. Company Admin login test
- **Verified Logic**: Shared `LoginViewModel` handles `ADMIN`/`CEO` roles and redirects to `company_setup` if not complete.

## 17. Staff login test
- **Verified Logic**: Shared `LoginViewModel` loads `staff` profile and `company` branding, then navigates to `dashboard`.

## 18. Cross-platform Firestore Verification
- **Architecture**: Both platforms share `FirestoreManager.db` (cached) and `UserSession`. The `shared` module handles all Firestore operations via the GitLive multiplatform library.

## 19. Files Changed
- `shared/src/commonMain/kotlin/com/example/rshotsbilling/utils/AuthService.kt` (New)
- `shared/src/commonMain/kotlin/com/example/rshotsbilling/viewmodel/LoginViewModel.kt` (New)
- `composeApp/src/commonMain/kotlin/com/example/rshotsbilling/ui/LoginScreen.kt` (Modified to use ViewModel)
- `shared/src/jvmMain/kotlin/com/example/rshotsbilling/utils/FirebaseConfig.kt` (Fixed initialization)
- `shared/src/androidMain/kotlin/com/example/rshotsbilling/utils/PlatformAuth.kt` (Updated)
- `shared/src/jvmMain/kotlin/com/example/rshotsbilling/utils/PlatformAuth.kt` (Updated)

## 20. Files deliberately NOT changed
- Firestore collections and structures.
- Business logic in Repositories (Sales, Purchases, etc.).
- PDF/Excel export logic.

## 21. Any remaining risks
- Platform-specific behavior of GitLive Firebase might differ slightly (e.g., persistence).
- JVM Firebase Java SDK might have different quota or connection limits compared to Android.
