# Changelog

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
