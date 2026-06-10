# Web/Mobile Coverage Baseline Toward 1.0

## Source

- Mobile source: `D:\Website\Octopus-Mobile`
- Web/server reference: `D:\tmp\hureru-octopus-dev\octopus-dev`
- Upstream GitHub fallback: `bestruirui/octopus`, default branch `dev`

The local Web/server reference is the active parity baseline because it contains the newer site, proxy-pool, group health, group preset, auto-group, and outlier-retirement modules.

## API Coverage

Normalized `/api/v1` path comparison:

- Web paths found: 84
- Mobile paths found: 97
- Web-only paths after parameter normalization: 0

Mobile currently covers every Web API path found in the active Web reference. Extra Mobile paths are caused by explicit Retrofit declarations for dynamic helper paths, direct export/import endpoints, and log streaming.

## 1.0 Parity Track

The remaining work is not primarily API coverage. It is user-facing behavior parity:

- Setting semantics: Web exposes named controls for site automation, Responses WebSocket mode, SSE heartbeat, projected-channel auto-group, and outlier retirement. Mobile must avoid fallback raw keys and validate server-supported values.
- Operation feedback: Web shows focused toast/status feedback for save, sync, check-in, import/export, and destructive operations. Mobile should keep tightening loading, success, and failure states across modules.
- View options: Web has richer toolbar/search/view-option state in several modules. Mobile has search/filter coverage in many places but still needs a full per-module behavior audit.
- Release flow: 0.4.0 added signed release support; 0.5.0 starts GitHub Release automation.

## 0.5.0 Slice

First 0.5.0 Web parity slice:

- Add Mobile labels for all Web setting keys.
- Treat boolean Web settings as switches where the server expects `true` / `false`.
- Validate projected-channel auto-group modes: `0`, `1`, `2`, `3`, `true`, `false`.
- Validate Responses WebSocket mode: `off`, `transform`, `passthrough`.
- Validate SSE heartbeat values and outlier-retirement numeric ranges against server constraints.

## Recurring Verification

- `git diff --check`
- `.\gradlew.bat testReleaseUnitTest`
- `.\gradlew.bat assembleRelease`
