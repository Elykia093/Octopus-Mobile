# Octopus-Mobile 1.0.0 Roadmap

## Objective

Ship the first parity-complete Mobile management release against the active local Web/server reference.

## Scope

- Confirm Mobile covers every normalized Web `/api/v1` path found in `D:\tmp\hureru-octopus-dev\octopus-dev`.
- Close the remaining Log workflow parity gap by showing Web diagnostic fields on Mobile.
- Keep release evidence formal: changelog, tests, release build, tag, GitHub Release, and APK asset.

## Completed Slice

- Parsed Web Log diagnostic fields on Mobile: WebSocket mode, WebSocket execution mode, recovery action, cache token details, and total attempts.
- Added Log list diagnostics for API key name, WebSocket state, and attempt count.
- Added Log detail diagnostics for input token breakdown and merged channel attempt history.
- Added tests for diagnostic field parsing and attempt-summary display behavior.

## Release Gates

- `git diff --check`
- `.\gradlew.bat testReleaseUnitTest`
- `.\gradlew.bat assembleRelease`
