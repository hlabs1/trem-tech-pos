# Trem-Tech POS V1.15 — Cloud Build

This version is prepared for a cloud build, so the 4 GB Dell does not need to compile Android locally.

## Recommended route: GitHub Actions

1. Create a GitHub repository.
2. Upload the contents of this project (not the ZIP itself).
3. Open the **Actions** tab.
4. Run **Trem-Tech POS Android Build** manually, or push to `main`.
5. Wait for the build to finish.
6. Open the workflow run and download the `trem-tech-pos-debug-apk` artifact.
7. Transfer the APK to the Android phone and install it for QA.

The workflow installs JDK 17, Android SDK 35 and Gradle 8.9 in the cloud, then builds the debug APK.

## Alternative: Android Studio Cloud / Firebase Studio

The project can also be opened in a browser-based Android Studio Cloud/Firebase Studio workspace. This avoids requiring Android Studio to run locally.

## Important

- This package does not contain a signing key.
- The first APK should be a debug/test APK.
- Do not enter real customer/business data until QA passes.
- A signed release APK comes after the debug build and physical-phone QA.
