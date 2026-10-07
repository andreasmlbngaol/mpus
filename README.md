# mpus

A cat-lover app. Photograph the street cats you meet, give them a name, and see what
everyone else called the same cat. The backend does cat re-identification, so two people
who photograph the same cat can end up on the same page without ever meeting.

<div align="center">

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Compose%20BOM-2026.09.00-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203%20Expressive-1.5.0--alpha29-6750A4?style=for-the-badge&logo=materialdesign&logoColor=white)](https://m3.material.io/)
[![Gradle](https://img.shields.io/badge/Gradle-9.8.0-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org/)
[![Android](https://img.shields.io/badge/Android-SDK%2037%20%2F%20min%2029-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Koin](https://img.shields.io/badge/Koin-4.2.2-F9A03C?style=for-the-badge)](https://insert-koin.io/)
[![OkHttp](https://img.shields.io/badge/OkHttp-5.5.0-000000?style=for-the-badge)](https://square.github.io/okhttp/)
[![Coil](https://img.shields.io/badge/Coil-3.6.3-3B82F6?style=for-the-badge)](https://coil-kt.github.io/coil/)
[![osmdroid](https://img.shields.io/badge/osmdroid-6.1.20-7EB43C?style=for-the-badge)](https://github.com/osmdroid/osmdroid)
[![Firebase](https://img.shields.io/badge/Firebase%20Messaging-34.19.0-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com/)

</div>

---

## Table of Contents

- [What it is](#what-it-is)
- [What you can do](#what-you-can-do)
- [How the cat matching works](#how-the-cat-matching-works)
- [App rules](#app-rules)
- [The screens](#the-screens)
- [Getting started](#getting-started)
- [For developers](#for-developers)
- [License](#license)

---

## What it is

mpus is a map of the cats of a neighbourhood, built by the people who walk past them
every day. You snap a cat, the app asks "is this one of ours?", and you either attach your
photo to a cat that already exists or start a new one and name it.

Over time a cat collects sightings from different people, a leaderboard of names, photos
from every angle, and reviews. The most-liked name becomes the cat's display name.

This repo is the Android app. The server lives in a separate repo (the Rust backend); the
app talks to it over HTTPS.

## What you can do

| Feature | What it does |
|---|---|
| **Map** | Shows every cat snapped in the area you are looking at, refreshed every 30 seconds. A blue dot marks where you are, with a button to jump back to your GPS fix. |
| **Snap** | Take a photo of a cat, get a list of the cats it might be, then link your photo to one of them or start a new cat and name it. |
| **Cat detail** | A big hero photo, the names leaderboard with likes, a grid of every photo of that cat, and reviews. |
| **Names** | Suggest a name for a cat. Names are ranked by likes, and the top name is what everyone sees. |
| **Reviews** | Rate a cat from 0 to 10 and write what you really think. Other people can like your review. |
| **You** | Your identity card, your avatar (view, upload, camera, delete, with a 1:1 crop editor), your profile, a grid of your sightings, and your stats. |
| **Notifications** | Push alerts when someone likes your name, names a cat you found, reviews it, or wants to merge a cat you own. There is also an inbox in the app. |
| **Merges** | When two people snap the same cat as two separate cats, one can ask to merge them. The owner of the other cat approves or declines. |
| **Reports** | Flag a name or review that looks wrong. Three different people reporting the same thing hides it automatically for review. |
| **Two languages** | English and Indonesian, following the device language. |

## How the cat matching works

You do not tag anything. The server does it.

Every photo you upload is run through a cat re-identification model (MegaDescriptor, a
vision model trained to tell cats apart). The model turns the photo into a list of 768
numbers, a kind of fingerprint for that cat. When you snap a new cat, the server compares
your fingerprint to every other one and hands back the closest matches. Those are the
candidates you see on the Snap screen. Pick one to link your photo, or ignore them all and
make a new cat.

Nothing is linked automatically. The app always asks, because a model can be wrong and you
are standing right there looking at the cat.

## App rules

These are on purpose, not bugs:

- Naming a cat automatically likes your own name, so a brand new name starts with one like.
- You can like only one name per cat. Liking a different name moves your like to it. Liking
  the name you already like takes your like back.
- A cat's display name is the name with the most likes. If two names tie, the older one wins.
- You can post one review per cat. Posting again replaces your old review.
- You can like as many reviews as you want. Tapping the same one twice takes the like back.
- You can only name a cat you have snapped yourself. That is the point: you have to actually
  meet the cat.

## The screens

- **Map**: the main view. Pins are cat faces with the display name under them. Tap a pin to
  get a card with the cat's name, how many times it has been seen, and a link to its page.
- **Snap**: the camera. Snap a cat, watch it find candidates, then link or create.
- **You**: your profile, your avatar, your sightings, and your stats.
- **Cat page**: everything about one cat. Hero photo, names, photos, reviews.

## Getting started

1. Sign up with an email and a password, and pick a username and a nickname.
2. Check your inbox for a 6-digit code and type it in. Until you confirm it you can look
   around, but you cannot name, review, or upload anything.
3. Open **Snap**, give the app camera and location permission, and photograph a cat.
4. Pick a candidate if the cat already exists, or start a new cat and name it.
5. Find it again later on the **Map**.

---

## For developers

The app is a single-activity Jetpack Compose project. This section is only about building
and understanding the Android side; the server is documented in its own repo.

### Tech stack

| Component | Version |
|---|---|
| AGP | 9.4.1 |
| Gradle | 9.8.0 |
| Kotlin | 2.4.20 |
| Compose BOM | 2026.09.00 |
| material3 | 1.5.0-alpha29 (overrides the BOM to get the public M3 Expressive API) |
| compileSdk / targetSdk / minSdk | 37 / 37 / 29 |
| Java | 25 |
| Koin | 4.2.2 + compiler plugin 1.2.1 |
| OkHttp | 5.5.0 |
| Coil | 3.6.3 |
| osmdroid | 6.1.20 |
| kotlinx.serialization | 1.11.0 |
| navigation-compose | 2.10.2 |
| Firebase Messaging | BOM 34.19.0 |
| smooth-corner-rect | com.github.racra:smooth-corner-rect-android-compose v1.0.0 (JitPack) |
| material-icons-extended | camera, gallery, visibility, and rate-review glyphs |

### How it is put together

- **UI**: Jetpack Compose, one activity (`MainActivity`) into `MpusApp()`.
- **DI**: Koin with the compiler plugin. `@KoinApplication` on `MpusApplication` generates
  the typed `startKoin`, and `@Module @ComponentScan("id.andreasmlbngaol.mpus.ui")` in
  `di/AppModule.kt` auto-registers every `@Single`, `@Factory`, and `@KoinViewModel`.
  ViewModels come in with `koinViewModel()`.
- **Networking**: a hand-rolled OkHttp client (`data/ApiClient.kt`), no Retrofit. JSON via
  kotlinx.serialization, with the `{success, message?, data?}` envelope.
- **Session**: `data/SessionStore.kt` keeps the token and cached user in SharedPreferences
  (`mpus_session`). The token goes out as `Authorization: Bearer <token>`.
- **State**: each screen has a ViewModel exposing a `StateFlow<UiState>`. One-shot messages
  go through a `Channel<UiText>` shown as snackbars. `UiText` is a resource id or a raw
  server string, so ViewModels never touch `Context`.
- **Navigation**: navigation-compose `NavHost` in `ui/App.kt`, three tabs plus `cat/{id}`.
- **Push**: `MpusMessagingService` handles token rotation and foreground messages, and a
  notification tap deep-links into the cat page.

### Project structure

```text
mpus/
├─ app/
│  ├─ src/main/java/id/andreasmlbngaol/mpus/
│  │  ├─ data/       ApiClient.kt  Models.kt  SessionStore.kt
│  │  ├─ di/         AppModule.kt
│  │  ├─ ui/         App.kt  UiText.kt
│  │  │  ├─ auth/    AuthScreen.kt  AuthViewModel.kt  VerifyEmailScreen.kt  VerifyEmailViewModel.kt
│  │  │  ├─ cat/     CatScreen.kt  CatViewModel.kt
│  │  │  ├─ map/     MapScreen.kt  MapViewModel.kt
│  │  │  ├─ profile/ ProfileScreen.kt  ProfileViewModel.kt  AvatarCropDialog.kt
│  │  │  ├─ sighting/ SightingsScreen.kt  SightingsViewModel.kt
│  │  │  ├─ components/ ExpressiveField.kt  ExpressiveState.kt  MpusTopBar.kt
│  │  │  └─ theme/   Color.kt  Shape.kt  Theme.kt  Transitions.kt  Type.kt
│  │  ├─ util/       CaptureFiles.kt  LocationProvider.kt
│  │  └─ MainActivity.kt  MpusApplication.kt  MpusMessagingService.kt  PushRegistrar.kt
│  ├─ src/main/res/values/strings.xml        # English
│  ├─ src/main/res/values-in/strings.xml     # Indonesian
│  ├─ src/main/assets/                       # the gflex_variable font
│  └─ build.gradle.kts
├─ gradle/libs.versions.toml
└─ CLAUDE.md                                 # conventions and the traps that bit us
```

### Building

You need JDK 25 and the Android SDK for API 37.

```shell
./gradlew :app:assembleDebug          # full debug APK
./gradlew :app:compileDebugKotlin     # fast syntax check
./gradlew :app:testDebugUnitTest      # unit tests
```

The debug APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

Configuration is environment-driven, nothing is hardcoded. Copy `.env.example` to `.env`
and edit it, or set real environment variables (those win over `.env`):

| Variable | Purpose | Default |
|---|---|---|
| `API_BASE_URL` | Backend base URL baked into `BuildConfig.API_BASE_URL` | `https://mpus.booroong.online` |
| `MPUS_KEYSTORE_FILE` | Path to the release keystore | unset (release builds are unsigned locally) |
| `MPUS_KEYSTORE_PASSWORD` | Keystore password | unset |
| `MPUS_KEY_ALIAS` | Key alias | unset |
| `MPUS_KEY_PASSWORD` | Key password | unset |

`app/google-services.json` is a secret (it carries an API key) and is gitignored. In CI it
is written from the `GOOGLE_SERVICES_JSON` secret before the build.

### Releases and CI

`.github/workflows/release.yml` builds and publishes the APK on a tag. Pushing a tag `vX.Y.Z`
runs it, and the workflow refuses to run unless `X.Y.Z` equals `versionName` in
`app/build.gradle.kts`, so bump `versionName` (and `versionCode`) before tagging. Release
builds are minified by R8 and have resource shrinking on, and the workflow uploads the R8
mapping file as a build artifact so a minified crash stack trace can be read back.

Repository secrets the workflow needs:

| Secret | Contents |
|---|---|
| `GOOGLE_SERVICES_JSON` | The whole `google-services.json` file |
| `MPUS_KEYSTORE_BASE64` | `base64 -w0 mpus-release.jks` |
| `MPUS_KEYSTORE_PASSWORD` | Keystore password |
| `MPUS_KEY_ALIAS` | Key alias |
| `MPUS_KEY_PASSWORD` | Key password |

And one repository variable (optional, overrides the default base URL):

| Variable | Purpose |
|---|---|
| `API_BASE_URL` | Backend base URL for the released APK |

### Design system

The app is Material 3 Expressive, and it is deliberate, not stock Material:

- The palette is a fixed calico theme (warm cream, ginger orange, charcoal). Dynamic colour
  is off on purpose.
- Everything is a squircle, from `ShapeCache.*` in `ui/theme/Shape.kt`. Do not invent raw
  `RoundedCornerShape`.
- Type is Google Sans Rounded, shipped as the variable font `gflex_variable`.
- Motion is shared-axis, slide plus cross-fade, 380 ms. Back is slide-right plus fade only,
  no scale.
- Bottom sheets are a stack of individual squircle `Surface` cards, each with a circular
  icon badge and its own colour.

`CLAUDE.md` has the full conventions and the list of traps that already bit us.

---

## License

This project is licensed under the **PolyForm Noncommercial License 1.0.0**. You may read,
run, and modify it for any noncommercial purpose. Commercial use is not allowed.

See the [LICENSE](./LICENSE) file for the exact terms.
