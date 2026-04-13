# Octopus Mobile

Android native manager for `octopus`, built with Kotlin, Jetpack Compose and `miuix`.

## Tech stack

- Kotlin + Jetpack Compose
- Hilt
- Retrofit + OkHttp + Kotlinx Serialization
- DataStore + AndroidX Security
- `miuix-ui`, `miuix-preference`, `miuix-icons`

## Reference source

- `D:\Website\octopus\web\src\route\config.tsx`
- `D:\Website\octopus\web\src\api\client.ts`
- `D:\Website\octopus\internal\server\handlers\*.go`

## Current scope

- Server connection flow
- User login flow
- Main navigation: Home / Channel / Group / Model / Log / Setting
- Read-oriented first pass for major modules
- Delete support for channel/group and clear support for logs
- Update/version, settings list and API key overview in settings

## Build locally

```powershell
./gradlew.bat assembleDebug
./gradlew.bat assembleRelease
```

## GitHub packaging

The repository includes a GitHub Actions workflow that builds both debug and release APKs and uploads them as artifacts.
