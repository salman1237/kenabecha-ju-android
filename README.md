# KenaBecha JU — Android

Native Android client for [KenaBecha JU](https://kenabechaju.salmandev.io), the Jahangirnagar University student marketplace. Talks to the same FastAPI backend as the web app (`../kenabecha-ju`) — no separate backend, no rewrite.

See **[PLAN.md](PLAN.md)** for the full phased implementation plan and progress log.

**Stack**: Kotlin, Jetpack Compose (Material 3), Hilt, Retrofit + OkHttp + kotlinx.serialization, Navigation Compose, DataStore, Coil.
**Requirements**: Android Studio, JDK 17+ (the bundled Android Studio JBR works), Android SDK with `compileSdk 37` / `build-tools 36.0.0` installed.

## Building

```
./gradlew assembleDebug
```

`local.properties` (gitignored) needs `sdk.dir` pointing at your local Android SDK, Java-properties-escaped, e.g.:

```
sdk.dir=C\:\\Users\\you\\AppData\\Local\\Android\\Sdk
```
