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

The remaining work after the API comparison is user-facing behavior parity:

- Setting semantics: Web exposes named controls for site automation, Responses WebSocket mode, SSE heartbeat, projected-channel auto-group, and outlier retirement. Mobile must avoid fallback raw keys and validate server-supported values.
- Operation feedback: Web shows focused toast/status feedback for save, sync, check-in, import/export, and destructive operations. Mobile should keep tightening loading, success, and failure states across modules.
- View options: Web has richer toolbar/search/view-option state in several modules. Mobile has search/filter coverage in many places but still needs a full per-module behavior audit.
- Projection behavior: Web SiteChannel loads request history by default and offers quick filters for attention, request history, and disabled models. Mobile now mirrors these list-level flows and keeps the remaining gap focused on bulk model operations and deeper account-panel preferences.
- Release flow: 0.4.0 added signed release support; 0.5.0 starts GitHub Release automation.

## 1.0 Final Audit

The final 1.0 audit rechecked the active Web Log module against Mobile:

- Web `log.ts` exposes `request_api_key_name`, `attempts`, `total_attempts`, cache token details, and WebSocket diagnostic fields on `RelayLog`.
- Mobile now parses those fields in `RelayLog` instead of ignoring them.
- Mobile Log rows now surface API key name, WebSocket state, and total attempt count.
- Mobile Log detail now surfaces input token breakdown and merged channel attempt history.
- Existing Mobile Log actions already cover server-side filters, live stream connection/reconnect, detail loading, log clear, site-action target lookup, and disable-site-model confirmation.

With this slice, there are no known Web-only API paths or critical Web-only management workflows remaining in the active local reference.

## 0.5.0 Slice

First 0.5.0 Web parity slice:

- Add Mobile labels for all Web setting keys.
- Treat boolean Web settings as switches where the server expects `true` / `false`.
- Validate projected-channel auto-group modes: `0`, `1`, `2`, `3`, `true`, `false`.
- Validate Responses WebSocket mode: `off`, `transform`, `passthrough`.
- Validate SSE heartbeat values and outlier-retirement numeric ranges against server constraints.

Second 0.5.0 Web parity slice:

- Load SiteChannel projection data with `include_history=true`, matching the Web default.
- Add Mobile SiteChannel filters for attention, request history, and disabled models.
- Add Mobile SiteChannel sorting by name, model count, and attention priority.
- Cover SiteChannel filter/search/sort behavior with unit tests.

## Recurring Verification

- `git diff --check`
- `.\gradlew.bat testReleaseUnitTest`
- `.\gradlew.bat assembleRelease`
