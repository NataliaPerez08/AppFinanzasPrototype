# PULSO — Security & Insets Implementation Report

## Root cause (insets)

`MainActivity` called `enableEdgeToEdge()`, but `styles.xml` also declared
opaque system-bar colors:

```xml
<item name="android:statusBarColor">#F4F3F1</item>
<item name="android:navigationBarColor">#F4F3F1</item>
```

The opaque theme colors fight edge-to-edge: the system painted an opaque bar
band while the app *also* reserved space with `navigationBarsPadding()` in
`FinanceBottomNavigation` and `safeDrawing` in `FinanceScreen`. Result: a
duplicated/oversized empty block at the bottom and an inconsistent status-bar
area. There was no root `Scaffold`, so the fix was to (a) remove the opaque bar
colors so `enableEdgeToEdge` owns them, and (b) make inset ownership explicit
and non-overlapping.

## Changes

| File | Change |
|------|--------|
| `res/values/styles.xml` | Removed opaque `statusBarColor`/`navigationBarColor`; kept `windowLightStatusBar`. |
| `MainActivity.kt` | `enableEdgeToEdge` scrims set to the PULSO surface color (safe on API 26–28); extends `FragmentActivity` (biometric); lifecycle observer for auto-lock. |
| `FinanceScreen.kt` | Single owner of top/horizontal safe-drawing **and IME** insets (`imePadding` moved here). |
| `FinanceBottomNavigation.kt` | Single owner of the bottom nav inset; top-only divider (`drawBehind`) instead of a full border; compact 60 dp content height. |
| `AddInvestmentScreen.kt` / `AddTransactionScreen.kt` | Removed per-screen `imePadding()` (now centralized) to avoid double IME padding. |

Ownership rule: **status/horizontal/IME = `FinanceScreen`; navigation bar = `FinanceBottomNavigation`.** No layering applies the same inset twice.

## App Lock architecture

```
security/
├── LockState.kt              Loading / Disabled / Locked / Unlocked + UnlockResult
├── LockPolicy.kt             auto-lock options + progressive cooldown (pure)
├── PinHasher.kt              PBKDF2, salt, versioned credential, constant-time verify
├── PinRepository.kt          interface
├── SharedPreferencesPinRepository.kt   local-only persistence
├── AppLockManager.kt         state machine + lifecycle + attempts
└── BiometricAuthenticator.kt androidx.biometric availability

ui/security/
├── UnlockScreen.kt / UnlockViewModel.kt
├── SetupPinScreen.kt / SetupPinViewModel.kt    (create + change modes)
├── SecuritySettingsScreen.kt / SecuritySettingsViewModel.kt
├── ForgotPinScreen.kt / ForgotPinViewModel.kt
└── PinKeypad.kt              custom 4-digit keypad + dots (accessible)
```

The gate lives in `FinanzasApp` (not a route), so it cannot be bypassed via
back navigation or deep links:

```kotlin
when (lockState) {
    Loading            -> SecureStartupSurface()
    Disabled, Unlocked -> PulsoContent()
    Locked             -> LockedGate()   // UnlockScreen / ForgotPinScreen
}
```

**No-flash:** `AppLockManager.initialize()` resolves synchronously inside
`AppContainer.initialize()` (SharedPreferences is synchronous), before
`setContent`. A `Loading` surface is still handled for safety. Financial data is
never composed while `Locked`.

## Security

- **KDF:** `PBKDF2WithHmacSHA256`, 120 000 iterations, 256-bit output, random
  16-byte salt, `algorithmVersion = 1` (versioned for future migration).
  `PBEKeySpec.clearPassword()` is called after derivation.
- **Persistence:** `SharedPreferences("pulso_security")` stores only
  `algorithmVersion`, `iterations`, `keyLength`, Base64 `salt`, and Base64
  `credential`. The PIN is never stored, logged, or placed in `Bundle`/`Room`.
- **Verification:** re-derive + `MessageDigest.isEqual` (constant time).
- **Android Keystore:** not used. A PBKDF2 credential is not reversible; the
  remaining offline-brute-force risk (4-digit PIN) is bounded by iterations.
  Wrapping the credential with a Keystore key remains a documented pending
  decision (see below).
- **Cooldown:** 1–4 failures none; 5 → 30 s; 6 → 1 min; 7 → 5 min; 8+ → 15 min.
  Persisted (`failedAttempts`, `lastFailureAt`) so it survives restart.
  Failed attempts never delete financial data.
- **Biometric:** `androidx.biometric` via `BiometricPrompt` (`BIOMETRIC_WEAK`).
  Success unlocks; cancel/error keeps `Locked`. PIN is always the fallback.
  The toggle is only offered when App Lock is on and hardware is available.
- **Auto-lock:** options Inmediatamente / 30 s / 1 min (default) / 5 min / 15 min.
  `onStop` records the timestamp, `onStart` re-locks if `elapsed >= timeout`.
- **Screenshots:** allowed; no `FLAG_SECURE`. No backend, no accounts.

## Reset (Forgot PIN)

`AppContainer.resetAllData()`:
1. `AppDatabase.clearAllTables()` (institutions, investments, transactions,
   preferences, price history).
2. Clears `SharedPreferences("pulso_security")` (credential, salt, biometric,
   auto-lock, attempts).
3. `AppLockManager.resetToDisabled()` → gate shows the normal empty app.

No PIN-only reset path exists. Confirmation requires two destructive steps.

## Tests

- **Unit: 263 passed, 0 failures** (whole project), including:
  - `PinHasherTest`: same PIN+salt → same credential; different salt →
    different; correct/wrong PIN; versioned parameters; algorithm-version
    rejection; 4-digit rule.
  - `AppLockManagerTest`: disabled/cold-start-locked, correct/incorrect PIN,
    biometric success, immediate & timed auto-lock, progressive cooldown,
    change PIN, disable, reset.
  - `LockPolicyTest`: cooldown boundaries and auto-lock thresholds.
  - Projection Phases 11/12 tests (scenarios, edge cases, 300-position volume).
- **Instrumented:** the suite (43 tests incl. migrations and E2E flows) passed
  earlier in this session. The final connected run could **not** be executed —
  the USB device disconnected and did not re-enumerate. Security UI
  instrumentation tests are not yet added (see debt).

## Builds

- `:app:assembleDebug` — OK.
- `:app:assembleRelease` — OK (R8 minify + resource shrink + lint vital).
- Signed beta: `app/build/outputs/apk/release/PULSO-beta-1.0.0.apk`
  - Signature: **APK Signature Scheme V2**, cert `CN=PULSO Beta`.
  - SHA-256: `2c3e2f128f055b90bfbe4460c05b5bdb91c63e491dfa58a86d971b67546379ce`.
- Keystore and `keystore.properties` live outside version control
  (`$HOME/pulso-beta.keystore`, ignored by `.gitignore`). **Back this up; losing
  it prevents signing updates with the same identity.**

## Backups

`android:allowBackup="false"` plus full exclude rules (`backup_rules.xml`,
`data_extraction_rules.xml`) — Room and security preferences are not backed up
or transferred. No policy change made.

## Technical debt (out of scope)

- Cosmetic `Investment.historyCsv` still feeds the detail price chart.
- Per-asset probability ranges are portfolio-level only.
- Android Keystore wrapping of the stored credential.
- Instrumentation tests for the security UI (cold-start gate, PIN entry,
  biometric mock) are not automated.

## Pending decisions

- Enable Android Keystore wrapping (adds protection against offline credential
  extraction, at the cost of key-invalidation edge cases on some devices).
- If App Lock should survive app **uninstall-free** OS migration, revisit the
  backup policy — currently intentionally disabled.
