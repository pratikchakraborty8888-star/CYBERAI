# Cyber AI — repaired Flutter project

## What was repaired / added
- Moved the Dart entry point into Flutter's required `lib/main.dart` location.
- Moved the Android manifest and Kotlin bridge into the Android source tree expected by Flutter.
- Replaced the unreliable build workflow with a Flutter/Java 17 APK workflow.
- Added an AI website-generation action: say “create a website for …” and Cyber asks Gemini for a standalone responsive HTML page, then opens an in-app preview. The prompt encourages unique designs and tasteful CSS 3D depth rather than one fixed template.
- Added a music search bridge that tries common YouTube Music/ReVanced package IDs and falls back to the Android app chooser/default handler.
- Updated the Gemini model identifier to `gemini-2.5-flash`.

## Important limitations
- I could not run a real Flutter/Gradle build in this environment, so the project still needs a build/test on a machine or GitHub Actions.
- Website output is generated HTML in an in-app preview, not a deployed website. AI-generated sites should be reviewed before publishing.
- ReVanced package IDs vary by patch/build. The integration tries several known IDs, but your installed YouTube Music ReVanced package may use another ID. If so, add that exact package ID in `MainActivity.kt`.
- Android does not permit silent arbitrary app installation or unrestricted phone control. Users must grant permissions, and some actions (call control, background microphone/wake-word listening) are restricted by Android version and device policy.
- Always-on wake-word listening is not guaranteed when the app is backgrounded or the screen is off. A foreground service and its notification/permissions are needed for robust background operation.
- The Gemini API key is stored in app preferences, which is convenient for personal testing but is not secure against someone with access to the device. For a distributed app, proxy AI requests through a backend and keep the key server-side.

## Build an installable APK
### Option A — GitHub Actions (no local Flutter setup)
1. Create a private GitHub repository and upload these project files.
2. Push to `main` (or open **Actions → Build Cyber AI APK → Run workflow**).
3. Wait for the workflow to finish.
4. Open the workflow run and download the `cyber-ai-release-apk` artifact.
5. Transfer `app-release.apk` to your Android phone and install it. If Android asks, allow installation from the file manager/browser you used.

### Option B — build locally
Install Flutter SDK and Android SDK/Android Studio, then from this folder run:
```bash
flutter create --platforms=android --org com.example --project-name cyber_ai .
flutter pub get
flutter build apk --release
```
The APK is created at `build/app/outputs/flutter-apk/app-release.apk`.

For a phone install, transfer that APK to the phone and open it. Keep Play Protect/security warnings in mind and only install builds you trust.
