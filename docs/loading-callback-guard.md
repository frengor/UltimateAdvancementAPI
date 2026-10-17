# Stale player loading callback guard

REQ-LOAD-001: When a delayed player loading completion executes, the database
manager shall notify and update only the online Player instance currently
registered by Bukkit whose cached TeamProgression is the same valid instance.
It shall recheck after PlayerLoadingCompletedEvent listeners return, before
team notifications, advancement updates or scheduling unredeemed rewards.
Normal current-player completion and persistence semantics shall remain unchanged.

This preserves behavior identified in the owner's historical
UltimateAdvancementAPI-Plugin-2.8.1-loading-guard-test.1.jar (SHA-256
2b5442cd50178631c7484cefd10553078e0ab1813192f637c8283b3df1b95ed9).
Its original build commit is unknown. Compared with the official 2.8.1 API,
only DatabaseManager and its two nested class byte arrays differ in common API
classes. Disassembly identifies isCurrentLoadingPlayer and two callback checks.
This is source reconstruction of verified behavior, not a claim to recover the
original build provenance. Existing upstream connection/version handling is retained.

SPEAR: source inspected at canonical main/dev dfbcbb4c. Project has no EARS or
state helpers; this manual requirement/test/evidence record is used. Nine tests
exercise the actual delayed callback, including reentrant event-listener invalidation.
After correcting the test fixture's NMS-dependent event construction, the old
callback failed eight of nine cases (zero test errors). The restored guard
passes all nine. The complete Common reactor passes 31 tests (6 NMS Common,
25 Common), zero failures/errors/skips, on Java 25 with Javadocs skipped.
Common verification is also configured in GitHub Actions; this job is explicitly
not full-plugin verification. Tests mock only event construction and async
dispatch, and run the actual compiled delayed callback and listener ordering.
No public API, data format, permission, reward amount or schema changes are intended.
The check does not claim to cancel asynchronous work already dispatched or solve
all connection-load races. Full multi-version packaging and controlled client
acceptance are separate gates. No server mutation is part of local verification.

Full canonical `mvn -B -ntp -Dmaven.javadoc.skip=true clean verify` was attempted
on the removal-fix candidate and stops at missing provided Spigot 1.15.2 server
artifact. This machine lacks BuildTools-generated server inputs for the supported
NMS matrix. No module was removed, precompiled class inserted, dependency version
mislabelled, or test disabled to present a partial build as an installable plugin.
The original historical JAR was not replaced.
