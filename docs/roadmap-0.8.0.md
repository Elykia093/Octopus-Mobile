# Octopus-Mobile 0.8.0 Roadmap

## Focus

- Align API Key management with the Web admin form, starting with supported model restrictions derived from Group names.
- Keep create and edit flows compatible with the existing comma-separated `supported_models` API contract.
- Preserve manual editing for custom restrictions that are not present in the current Group list.

## Candidate Follow-ups

- Replace raw expiration timestamp entry with a date/time picker equivalent to the Web form.
- Add API Key usage stats overlay parity from the Web setting panel.
- Review API Key list sorting and created-key visibility against Web behavior.

## Verification

- API Key model restriction parsing and candidate derivation unit coverage.
- Release unit test gate before every commit in the 0.8.x line.
