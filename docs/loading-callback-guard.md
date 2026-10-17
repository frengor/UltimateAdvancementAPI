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
state helpers; this manual requirement/test/evidence record is used. Eleven tests
exercise the actual delayed callback, including reentrant event-listener invalidation.
After correcting the test fixture's NMS-dependent event construction, the initial
old callback failed eight of nine cases (zero test errors). Refinement added
newly registered team notification and listener-unload cases. The final eleven
tests fail nine cases on unmodified dev and pass all eleven with the guard.
The complete Common reactor passes 33 tests (6 NMS Common, 27 Common), zero
failures/errors/skips, on Java 25 with Javadocs skipped.
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

Release refinement, 17 October 2026: the combined fork candidate at 1ef44837
passes the complete canonical `mvn -B -ntp clean verify` build on Java 25,
with Javadocs enabled and all 24 native adapters retained. All 41 tests pass,
zero failures/errors/skips. Both installable plugin variants retain all 406
classes from the official 2.8.2 plugin plus the package-private removal selector;
the loading guard is present in packaged bytecode. All 109 shared API/library
public signatures match stock 2.8.2; all existing signatures/classes from the
108 shared 2.8.1 API classes remain available. All 44 renderer tests pass against
the rebuilt API. Public CI now verifies API packaging, tests and Javadocs, and
labels its API-library artifact as not an installable server plugin.

BuildTools-generated inputs were built from recorded official version refs.
The latest 1.21.11 alias produces R0.2, so exact official revision 4583 was used
for the requested R0.1 artifact; no dependency was relabelled or NMS module
removed. This closes the initial local packaging gap above. Upstream PRs and
the owner-fork main integration are separate states. No server upload/restart
or real player/client acceptance is implied by these build results.
