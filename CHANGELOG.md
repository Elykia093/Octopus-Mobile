# Changelog

## [0.7.0] - 2026-06-10

### Added

- Added a Site check-in overview and status filters aligned with the Web admin CheckinPanel.
- Added Site check-in status derivation, summary, search, and filter unit coverage.
- Added richer Site account sync and check-in operation messages using server status, result details, and check-in rewards.
- Added explicit Site account row messages for unsupported or disabled check-in.

### Changed

- Site search and batch scope now follow the visible accounts produced by check-in status filters.
- Failed Site account sync and check-in results now surface as operation errors instead of generic success messages.
- Site account rows now hide manual check-in actions when the platform or account settings do not allow check-in.

## [0.6.0] - 2026-06-10

### Added

- Added SiteChannel batch model route updates and batch model enable/disable actions for Projection groups.
- Added account-level Projection group scoping and in-account model search.
- Added account-level Projection model sorting by model name, group name, route type, and recent request.
- Added Projection advanced settings validation for param override JSON objects.

### Verification

- SiteChannel batch model request, account filter/sort, and projected settings validation unit coverage.

## [0.5.0] - 2026-06-10

### Added

- Added a PowerShell release helper that extracts version notes from `CHANGELOG.md`, creates a GitHub Release through GitHub CLI, and uploads the generated APK.
- Added the 0.5.0 roadmap focused on release automation and safer version iteration.
- Added Mobile setting labels, switch handling, and validation for Web setting keys including site automation, Responses WebSocket, SSE heartbeat, projected channel auto-group, and outlier retirement.
- Added SiteChannel projection filters and sorting for attention, request history, disabled models, model count, and attention-first review flows.
- Added a Web/Mobile coverage baseline for the 1.0 parity track.
- Added a release preparation helper for updating Gradle version metadata and changelog release dates.
- Added an Android Release GitHub Actions workflow for manual versioned release builds, tag creation, and GitHub Release publishing.
- Added versioned release APK output names that distinguish signed and unsigned artifacts.

### Changed

- Aligned the Mobile SiteChannel projection list with the Web default by loading request history with `include_history=true`.
- Hardened the GitHub Release helper with checks for a clean git tree, existing tag, Gradle version match, dated changelog entry, asset path, and GitHub CLI availability.
- Updated the GitHub Release helper to auto-discover the versioned release APK when `-AssetPath` is omitted.

### Verification

- `scripts/create-github-release.ps1 -Version 0.5.0 -DryRun`
- `scripts/prepare-release.ps1 -Version 0.5.0 -DryRun`
- `.\gradlew.bat testReleaseUnitTest`
- `.\gradlew.bat assembleRelease`
- GitHub Actions release workflow YAML parse check.
- SiteChannel filter and sort unit coverage.

## [0.4.0] - 2026-06-10

### Added

- Added optional release signing configuration through Gradle properties or environment variables without committing signing secrets.
- Added GitHub Actions release keystore restoration from secrets for signed CI release builds.

### Changed

- Updated release artifact upload to collect the generated release APK whether the build is signed or unsigned.
- Documented local and CI release signing inputs for formal distribution builds.

### Verification

- `git diff --check`
- GitHub Actions workflow YAML parse check
- `.\gradlew.bat testReleaseUnitTest`
- `.\gradlew.bat assembleRelease`
- Partial release signing configuration fails with a clear Gradle error.

## [0.3.0] - 2026-06-09

### Added

- Added group pinning on mobile, including pinned group metadata parsing, pin and unpin actions, and pinned-first ordering.
- Added group preset management, including listing, saving the current group as a preset, creating blank presets, cloning, activating, editing, and deleting presets.
- Added group auto-group configuration, including global projected mode, per-channel auto-group overrides, channel search, save, run, and save-and-run flows.
- Added repository contract coverage for group pinning, group presets, and auto-group request and response contracts.

### Changed

- Extended the mobile group screen to expose the remaining Web group management flows from the group list.
- Improved group cards with pinned and active-preset badges so server-side group state is visible on mobile.

### Verification

- `.\gradlew.bat testReleaseUnitTest`
- `git diff --check`
- `.\gradlew.bat assembleRelease`

## [0.2.0] - 2026-06-09

### Added

- Added proxy pool management, including create, edit, enable or disable, delete, reference lookup, and proxy test flows.
- Added proxy pool selection for site accounts and channels, with explicit clearing of pool references when switching back to direct or system proxy modes.
- Added live log streaming, log detail viewing, and server-side log filtering by status, keyword scope, keyword match mode, time range, and channel.
- Added channel-aware and time-aware client-side filtering for incoming live log events so filtered views stay consistent while the stream is connected.

### Changed

- Improved log search so keyword filtering is sent to the server instead of filtering only the currently loaded page.
- Improved release verification coverage for proxy pool integration and log filtering request contracts.

### Verification

- `.\gradlew.bat testReleaseUnitTest`
- `.\gradlew.bat assembleRelease`
