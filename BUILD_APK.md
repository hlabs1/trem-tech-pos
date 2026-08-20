# Trem-Tech POS V1.14 — APK Build Guide

## Prerequisites
- Android Studio with a compatible Android SDK
- JDK compatible with the project's Android Gradle Plugin
- Android SDK Platform/Build Tools installed
- A USB-connected Android phone with Developer Options/USB debugging enabled

## Build
1. Open this project in Android Studio.
2. Allow Gradle sync to finish.
3. Run **Build > Make Project**.
4. Fix any compile errors before continuing.
5. Run **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6. Install the debug APK on the test phone.
7. Execute `QA_CHECKLIST.md`.
8. For production, configure a private release keystore and build a signed release APK.

## Important
Never put a real signing keystore/password into source control. Keep signing credentials outside the project.

## First physical-device test
- Login as admin.
- Create a test cashier.
- Receive one serialized phone and one ordinary hardware item.
- Sell both.
- Test receipt sharing.
- Test credit/layby.
- Test refund.
- Create and validate a backup.
- Restore the backup only with test data.
- Verify stock, balances and audit records afterward.
