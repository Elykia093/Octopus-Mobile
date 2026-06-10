# Octopus Mobile Roadmap to 1.0

## Objective

Bring Octopus-Mobile to functional parity with the active Web management console while keeping each release independently verifiable and publishable.

## Baseline

- Active Web/server reference: `D:\tmp\hureru-octopus-dev\octopus-dev`
- Mobile source: `D:\Website\Octopus-Mobile`
- API path comparison: Mobile covers all normalized Web `/api/v1` paths found in the active reference.
- Remaining parity work is primarily interaction behavior, operational feedback, batch workflows, and release safety.

## Release Slices

### 0.5.x

- Release automation: reusable GitHub Release helper, release checklist, version preparation, and artifact naming.
- Setting semantics: named controls and validation for newer Web setting keys.
- Projection usability: SiteChannel history loading, quick filters, sorting, and first-pass behavior parity.

### 0.6.x

- Projection bulk operations: batch route switching, batch enable/disable, and clearer pending-operation feedback.
- Projection account panels: account-level search, group scoping, route summaries, and advanced projected-channel settings polish.
- Site workflows: Web parity audit for site sync, check-in, import, archived sites, proxy mode, and account editing.
- Current 0.6.0 progress: group-level Projection batch route/enable/disable actions, account-level group scoping/search/sort, and Projection param override JSON validation.

### 0.7.x

- Home and health workflows: group health summary, run-all/full-probe behavior, status drilldown, and stale/failed state recovery.
- Channel workflows: Web parity audit for channel batch operations, model sync, key handling, proxy configuration, and operation feedback.
- Log workflows: close remaining gaps around jump targets, site-action target feedback, stream fallback, and destructive actions.

### 0.8.x

- Group workflows: preset, auto-group, projected source handling, pinning, sparse update safeguards, and parity regression tests.
- API Key workflows: batch actions, hidden key handling, create-result visibility, model and group restriction editing.
- Model workflows: filtering, pricing metadata, sync feedback, and high-volume list ergonomics.

### 0.9.x

- Cross-module polish: consistent loading, success, failure, empty, and stale states across all management surfaces.
- Accessibility and mobile ergonomics: long text, dense lists, dialog scroll bounds, and destructive-action clarity.
- Release hardening: signed release rehearsal, rollback notes, release notes completeness, and CI gates.

### 1.0.0

- Full Web/Mobile parity audit with current reference source.
- No known Web-only API or critical user-facing workflow gaps.
- Verified release build, changelog, tag, GitHub Release, and published APK artifacts.

## Recurring Gates

- Update `CHANGELOG.md` for each slice.
- Update the relevant roadmap or coverage document when parity scope changes.
- Run `git diff --check`.
- Run `.\gradlew.bat testReleaseUnitTest`.
- Run `.\gradlew.bat assembleRelease` for UI, resource, release, or integration-sensitive changes.
- Commit and push each completed slice.
