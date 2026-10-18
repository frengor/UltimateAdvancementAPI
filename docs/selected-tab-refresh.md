# Stable advancement inspection

REQ-TAB-001: WHEN a visible tab refreshes with unchanged advancement definitions THE SYSTEM SHALL send progress only without removing definitions or selecting a tab.
REQ-TAB-002: WHEN definitions or visibility change THE SYSTEM SHALL retain the existing full refresh path; hide, quit, disposal and retry must discard cached display snapshots.
REQ-TAB-003: WHEN a packet cannot be constructed THE SYSTEM SHALL retain the previous sent snapshot for retry and SHALL NOT report an unsent snapshot as delivered.

SPEAR: source is the owner fork current main eccfa16c, containing current upstream history and the merged removal/loading fixes. No EARS/state tools exist in this repository; this record and automated tests provide manual task/evidence tracking. Scope is packet presentation only; persistence, rewards, permissions and supported adapters remain unchanged. A real AdvsUpdateRunnable regression must fail before the engine edit. All 24 native adapters and the full default build must compile after it. Live client acceptance and hosted review remain distinct.

Status: local proof complete; hosted review and live client acceptance pending.

Corrected regression against unchanged eccfa16c: TabRefreshTest ran two cases with one expected failure, zero errors. The actual update runnable sent [select, remove, definitions, select] instead of [progress]. Earlier fixture setup/compile errors were corrected before this behavioral red result; they are not proof of the engine bug.

Engine: cache successfully sent wrapper snapshots per player. Use a progress-only packet when wrappers are unchanged; retain full refresh for first display, visibility or definition replacement. Clear snapshots on hide, quit and disposal. Keep previous state when packet construction fails. Existing public packet constructors and all persisted/reward semantics are unchanged.

Refine: five actual runnable tests cover progression, unchanged refresh, replaced definitions, hide/reshow and packet-factory failure/retry. Java 25 canonical `mvn -B -ntp clean verify` passes 46 tests, 0 failures, 0 errors, 0 skips, Javadocs, both distributions and all 24 native adapters. Test evidence stays separate from a real client holding another tab open during refresh. No project-local EARS/state tool exists.


Upstream PR121 adaptation: cherry-picked only the selected-tab fix onto the existing removal-fix branch, excluding the separately reviewed loading-callback PR122. Exact upstream branch API-only clean verify passed 32 tests and Javadocs. The full 46-test/all-adapter result above belongs to the owner-fork integration.
